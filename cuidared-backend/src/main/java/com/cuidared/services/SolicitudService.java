package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;
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

    @Autowired
    public SolicitudService(SolicitudRepository solicitudRepository, UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
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
        Solicitud guardada = solicitudRepository.save(solicitud);
        padre.addSolicitudId(guardada.getId());
        usuarioRepository.save(padre);
        
        return guardada;
    }

    public List<Solicitud> obtenerTodas() {
        return solicitudRepository.findAll();
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