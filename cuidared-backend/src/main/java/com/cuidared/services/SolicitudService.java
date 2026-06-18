package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;
import com.cuidared.models.Cuidador;
import com.cuidared.models.Padre;
import com.cuidared.models.Solicitud;
import com.cuidared.models.Usuario;
import com.cuidared.repositories.SolicitudRepository;
import com.cuidared.repositories.UsuarioRepository;
import com.cuidared.models.EstadoSolicitud;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Servicio para manejar la lógica de negocio de las solicitudes.
 */
@Service
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionService notificacionService;

    @Autowired
    public SolicitudService(SolicitudRepository solicitudRepository,
                            UsuarioRepository usuarioRepository,
                            NotificacionService notificacionService) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionService = notificacionService;
    }

    /**
     * Crea una nueva solicitud para un Padre.
     * Valida que no exceda las 3 solicitudes activas.
     * @param solicitud Solicitud a crear
     * @return Solicitud guardada
     */
    public Solicitud crearSolicitud(Solicitud solicitud) {
        // 1. Validar que el padre existe
        Optional<Usuario> userOpt = usuarioRepository.findById(solicitud.getPadreId());
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Padre)) {
            throw new ReglaNegocioException("Complete su perfil primero");
        }
        
        Padre padre = (Padre) userOpt.get();
        
        // 2. Validar perfil completo (nombre, correo, teléfono no pueden estar vacíos)
        if (padre.getNombre() == null || padre.getNombre().isBlank() ||
            padre.getCorreo() == null || padre.getCorreo().isBlank() ||
            padre.getTelefono() == null || padre.getTelefono().isBlank()) {
            throw new ReglaNegocioException("Complete su perfil primero");
        }
        
        // 3. Validar dirección dentro de Caracas (OPCIONAL)
        // if (padre.getDireccion() == null || !padre.getDireccion().toLowerCase().contains("caracas")) {
        //     throw new ReglaNegocioException("Servicio solo disponible en Caracas");
        // }
        
        // 4. Validar fecha no pasada
        if (solicitud.getFecha() != null && solicitud.getFecha().isBefore(java.time.LocalDate.now())) {
            throw new ReglaNegocioException("La fecha no puede ser anterior a hoy");
        }
        
        // 5. Validar duración (1-12 horas)
        if (solicitud.getDuracionHoras() < 1 || solicitud.getDuracionHoras() > 12) {
            throw new ReglaNegocioException("La duración debe ser entre 1 y 12 horas");
        }
        
        // 6. Contar solicitudes activas del padre (máximo 3)
        List<Solicitud> todas = solicitudRepository.findAll();
        long activas = todas.stream()
            .filter(s -> s.getPadreId().equals(solicitud.getPadreId()))
            .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE || s.getEstado() == EstadoSolicitud.ACEPTADA)
            .count();
        
        if (activas >= 3) {
            throw new ReglaNegocioException("Límite de 3 solicitudes activas alcanzado");
        }
        
        // 7. Validar horario duplicado (misma fecha y hora)
        boolean horarioDuplicado = todas.stream()
            .filter(s -> s.getPadreId().equals(solicitud.getPadreId()))
            .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE || s.getEstado() == EstadoSolicitud.ACEPTADA)
            .anyMatch(s -> s.getFecha() != null && 
                          s.getFecha().equals(solicitud.getFecha()) && 
                          s.getHoraInicio() != null &&
                          s.getHoraInicio().equals(solicitud.getHoraInicio()));
        
        if (horarioDuplicado) {
            throw new SolapamientoHorarioException("Ya tienes una solicitud activa en ese horario");
        }
        
        // 8. Guardar solicitud
        solicitud.setEstado(EstadoSolicitud.PENDIENTE);
        // Guardamos el nombre del creador para poder mostrar luego quién la creó.
        solicitud.setPadreNombre(padre.getNombre());
        Solicitud guardada = solicitudRepository.save(solicitud);
        padre.addSolicitudId(guardada.getId());
        usuarioRepository.save(padre);

        // 9. Registrar una notificación para el padre confirmando la solicitud
        notificacionService.registrarNotificacion(
            padre.getId(),
            "Solicitud registrada",
            "Tu solicitud de cuidado fue registrada y está PENDIENTE de ser aceptada.",
            "SOLICITUD"
        );

        return guardada;
    }

    public List<Solicitud> obtenerTodas() {
        return solicitudRepository.findAll();
    }

    /**
     * Lista las solicitudes que un cuidador puede aceptar: las que están
     * PENDIENTES, todavía no tienen cuidador asignado y cuyo tipo de asistencia
     * coincide con alguna de sus habilidades.
     *
     * A propósito NO se filtra por la disponibilidad declarada del cuidador: la
     * idea es que pueda ver una solicitud aunque caiga fuera de su horario
     * publicado y decidir por sí mismo si la toma o no.
     */
    public List<Solicitud> obtenerSolicitudesDisponiblesPara(String cuidadorId) {
        Cuidador cuidador = obtenerCuidador(cuidadorId);

        return solicitudRepository.findAll().stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE)
                .filter(s -> s.getCuidadorId() == null || s.getCuidadorId().isBlank())
                .filter(s -> s.getTipo() != null && cuidador.getHabilidades().contains(s.getTipo()))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * Lista las solicitudes ya asignadas a un cuidador (las que aceptó),
     * separando lo que sigue activo (ACEPTADA) de lo que ya pasó al historial
     * (FINALIZADA / CANCELADA).
     */
    public java.util.Map<String, List<Solicitud>> obtenerSolicitudesDeCuidador(String cuidadorId) {
        obtenerCuidador(cuidadorId);

        List<Solicitud> delCuidador = solicitudRepository.findAll().stream()
                .filter(s -> cuidadorId.equals(s.getCuidadorId()))
                .collect(java.util.stream.Collectors.toList());

        List<Solicitud> activas = new java.util.ArrayList<>();
        List<Solicitud> historial = new java.util.ArrayList<>();
        for (Solicitud s : delCuidador) {
            if (s.getEstado() == EstadoSolicitud.ACEPTADA) {
                activas.add(s);
            } else {
                historial.add(s);
            }
        }

        java.util.Map<String, List<Solicitud>> resultado = new java.util.HashMap<>();
        resultado.put("activas", activas);
        resultado.put("historial", historial);
        return resultado;
    }

    /**
     * Un cuidador acepta una solicitud pendiente y queda asignado a ella.
     * Permite aceptar aunque la franja caiga fuera de su disponibilidad
     * declarada, pero bloquea si choca con otro servicio que ya tenga ACEPTADO
     * (evita doble reserva en el mismo horario).
     */
    public Solicitud aceptarSolicitud(String id, String cuidadorId) {
        Cuidador cuidador = obtenerCuidador(cuidadorId);

        Solicitud solicitud = solicitudRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La solicitud no existe."));

        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("Esta solicitud ya no está disponible para aceptar.");
        }

        if (solicitud.getCuidadorId() != null && !solicitud.getCuidadorId().isBlank()
                && !solicitud.getCuidadorId().equals(cuidadorId)) {
            throw new ReglaNegocioException("Esta solicitud ya fue tomada por otro cuidador.");
        }

        if (solicitud.getTipo() != null && !cuidador.getHabilidades().contains(solicitud.getTipo())) {
            throw new ReglaNegocioException("Esta solicitud requiere un tipo de asistencia que no ofreces.");
        }

        // Evitar doble reserva: que no choque con otra solicitud ya ACEPTADA.
        if (solicitud.getHorario() != null) {
            boolean choca = solicitudRepository.findAll().stream()
                    .filter(s -> cuidadorId.equals(s.getCuidadorId()))
                    .filter(s -> s.getEstado() == EstadoSolicitud.ACEPTADA)
                    .filter(s -> s.getHorario() != null)
                    .anyMatch(s -> s.getHorario().seSolapaCon(solicitud.getHorario()));
            if (choca) {
                throw new SolapamientoHorarioException(
                        "Ya tienes un servicio aceptado que se solapa con este horario.");
            }
        }

        solicitud.setCuidadorId(cuidadorId);
        solicitud.setEstado(EstadoSolicitud.ACEPTADA);
        Solicitud guardada = solicitudRepository.save(solicitud);

        // Avisar al padre que su solicitud fue aceptada.
        notificacionService.registrarNotificacion(
                solicitud.getPadreId(),
                "Solicitud aceptada",
                "El cuidador " + cuidador.getNombre() + " aceptó tu solicitud de cuidado.",
                "SOLICITUD"
        );

        return guardada;
    }

    /**
     * Obtiene un cuidador validando su existencia y tipo.
     */
    private Cuidador obtenerCuidador(String cuidadorId) {
        if (cuidadorId == null || cuidadorId.isBlank()) {
            throw new ReglaNegocioException("El ID del cuidador es obligatorio.");
        }
        Optional<Usuario> userOpt = usuarioRepository.findById(cuidadorId);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Cuidador)) {
            throw new ReglaNegocioException("Cuidador no encontrado.");
        }
        return (Cuidador) userOpt.get();
    }

    /**
     * Modifica una solicitud existente (Sprint 2 - versión básica).
     * Solo se permiten cambios mientras la solicitud siga PENDIENTE.
     * @param id ID de la solicitud a modificar
     * @param cambios Datos nuevos a aplicar
     * @return Solicitud actualizada
     */
    public Solicitud modificarSolicitud(String id, Solicitud cambios) {
        Solicitud existente = solicitudRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La solicitud no existe."));

        if (existente.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ReglaNegocioException("Solo se pueden modificar solicitudes pendientes.");
        }

        if (cambios.getFecha() != null) {
            if (cambios.getFecha().isBefore(java.time.LocalDate.now())) {
                throw new ReglaNegocioException("La fecha no puede ser anterior a hoy");
            }
            existente.setFecha(cambios.getFecha());
        }
        if (cambios.getHoraInicio() != null) {
            existente.setHoraInicio(cambios.getHoraInicio());
        }
        if (cambios.getDuracionHoras() > 0) {
            if (cambios.getDuracionHoras() < 1 || cambios.getDuracionHoras() > 12) {
                throw new ReglaNegocioException("La duración debe ser entre 1 y 12 horas");
            }
            existente.setDuracionHoras(cambios.getDuracionHoras());
        }
        if (cambios.getTipo() != null) {
            existente.setTipo(cambios.getTipo());
        }
        if (cambios.getDescripcion() != null) {
            existente.setDescripcion(cambios.getDescripcion());
        }
        if (cambios.getHorario() != null) {
            existente.setHorario(cambios.getHorario());
        }

        return solicitudRepository.save(existente);
    }

    /**
     * Cancela una solicitud existente (Sprint 2 - versión básica).
     * @param id ID de la solicitud a cancelar
     * @return Solicitud cancelada
     */
    public Solicitud cancelarSolicitud(String id) {
        Solicitud existente = solicitudRepository.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La solicitud no existe."));

        if (existente.getEstado() == EstadoSolicitud.FINALIZADA) {
            throw new ReglaNegocioException("No se puede cancelar una solicitud finalizada.");
        }
        if (existente.getEstado() == EstadoSolicitud.CANCELADA) {
            throw new ReglaNegocioException("La solicitud ya está cancelada.");
        }

        existente.setEstado(EstadoSolicitud.CANCELADA);
        Solicitud guardada = solicitudRepository.save(existente);

        notificacionService.registrarNotificacion(
            existente.getPadreId(),
            "Solicitud cancelada",
            "Tu solicitud de cuidado fue cancelada.",
            "SOLICITUD"
        );

        return guardada;
    }

    // --- LÓGICA DE JESÚS RECUPERADA (Separación de Historial y Citas Futuras) ---
    /**
     * Obtiene todas las solicitudes de un padre y las separa en historial y programación futura.
     * @param padreId ID del padre
     * @return Mapa con dos listas: "historial" y "futuras"
     */
    public java.util.Map<String, List<Solicitud>> obtenerHistorialYFuturasPorPadre(String padreId) {
        List<Solicitud> todas = solicitudRepository.findAll();
        
        List<Solicitud> delPadre = todas.stream()
                .filter(s -> s.getPadreId().equals(padreId))
                .collect(java.util.stream.Collectors.toList());

        List<Solicitud> historial = new java.util.ArrayList<>();
        List<Solicitud> futuras = new java.util.ArrayList<>();

        java.time.LocalDate hoy = java.time.LocalDate.now();
        java.time.LocalTime ahora = java.time.LocalTime.now();

        for (Solicitud s : delPadre) {
            boolean esPasada = false;
            
            // Si tiene estado FINALIZADA o CANCELADA, siempre va al historial
            if (s.getEstado() == EstadoSolicitud.FINALIZADA || s.getEstado() == EstadoSolicitud.CANCELADA) {
                esPasada = true;
            } else if (s.getFecha() != null) {
                // Verificar si la fecha ya pasó
                if (s.getFecha().isBefore(hoy)) {
                    esPasada = true;
                } else if (s.getFecha().isEqual(hoy) && s.getHoraInicio() != null && s.getHoraInicio().isBefore(ahora)) {
                    // Si es hoy, verificar si la hora ya pasó
                    esPasada = true;
                }
            }

            if (esPasada) {
                historial.add(s);
            } else {
                futuras.add(s);
            }
        }

        java.util.Map<String, List<Solicitud>> resultado = new java.util.HashMap<>();
        resultado.put("historial", historial);
        resultado.put("futuras", futuras);
        
        return resultado;
    }
}