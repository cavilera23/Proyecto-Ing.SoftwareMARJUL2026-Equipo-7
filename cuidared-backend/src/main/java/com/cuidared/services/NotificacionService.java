package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.models.Notificacion;
import com.cuidared.repositories.NotificacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para manejar la lógica de negocio de las notificaciones.
 */
@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    @Autowired
    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Registra (crea) una nueva notificación para un usuario.
     */
    public Notificacion registrarNotificacion(Notificacion notificacion) {
        if (notificacion.getUsuarioId() == null || notificacion.getUsuarioId().isBlank()) {
            throw new ReglaNegocioException("La notificación debe estar dirigida a un usuario.");
        }
        if (notificacion.getMensaje() == null || notificacion.getMensaje().isBlank()) {
            throw new ReglaNegocioException("La notificación debe tener un mensaje.");
        }
        return notificacionRepository.save(notificacion);
    }

    /**
     * Atajo para registrar una notificación desde la lógica interna del sistema.
     */
    public Notificacion registrarNotificacion(String usuarioId, String titulo, String mensaje, String tipo) {
        Notificacion n = new Notificacion();
        n.setUsuarioId(usuarioId);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        if (tipo != null && !tipo.isBlank()) {
            n.setTipo(tipo);
        }
        return registrarNotificacion(n);
    }

    /**
     * Obtiene las notificaciones de un usuario ordenadas de más reciente a más antigua.
     * @param incluirSilenciadas si es false, omite las notificaciones silenciadas
     */
    public List<Notificacion> obtenerPorUsuario(String usuarioId, boolean incluirSilenciadas) {
        return notificacionRepository.findAll().stream()
                .filter(n -> usuarioId != null && usuarioId.equals(n.getUsuarioId()))
                .filter(n -> incluirSilenciadas || !n.isSilenciada())
                .sorted(Comparator.comparing(
                        Notificacion::getFechaCreacion,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Programa una notificación para una fecha y hora futura (Sprint 2).
     * El recordatorio queda PENDIENTE (disparada = false) hasta que el
     * planificador lo dispare al llegar la fecha, hora y minuto indicados.
     */
    public Notificacion programarNotificacion(Notificacion notificacion) {
        if (notificacion.getFechaProgramada() == null) {
            throw new ReglaNegocioException("Debe indicar la fecha y hora programada.");
        }
        if (notificacion.getFechaProgramada().isBefore(java.time.LocalDateTime.now())) {
            throw new ReglaNegocioException("La fecha programada no puede ser en el pasado.");
        }
        notificacion.setDisparada(false);
        return registrarNotificacion(notificacion);
    }

    /**
     * Dispara todos los recordatorios cuya fecha/hora/minuto programada ya llegó.
     * Un recordatorio pendiente (disparada = false) se "activa": se marca como
     * disparado y se refresca su fecha de creación para que aparezca como un
     * aviso nuevo en la bandeja del usuario.
     *
     * @return cantidad de notificaciones disparadas en esta ejecución
     */
    public int dispararNotificacionesPendientes() {
        java.time.LocalDateTime ahora = java.time.LocalDateTime.now();

        List<Notificacion> pendientes = notificacionRepository.findAll().stream()
                .filter(n -> !n.isDisparada())
                .filter(n -> n.getFechaProgramada() != null)
                .filter(n -> !n.getFechaProgramada().isAfter(ahora))
                .collect(Collectors.toList());

        for (Notificacion n : pendientes) {
            n.setDisparada(true);
            n.setFechaCreacion(ahora); // aparece como aviso recién llegado
            notificacionRepository.save(n);
        }

        return pendientes.size();
    }

    /**
     * Elimina una notificación (Sprint 2 - versión básica).
     */
    public void eliminarNotificacion(String id) {
        obtenerOError(id);
        notificacionRepository.deleteById(id);
    }

    /**
     * Silencia una notificación (deja de mostrarse en el listado por defecto).
     */
    public Notificacion silenciarNotificacion(String id) {
        Notificacion n = obtenerOError(id);
        n.setSilenciada(true);
        return notificacionRepository.save(n);
    }

    /**
     * Reactiva una notificación previamente silenciada.
     */
    public Notificacion activarNotificacion(String id) {
        Notificacion n = obtenerOError(id);
        n.setSilenciada(false);
        return notificacionRepository.save(n);
    }

    private Notificacion obtenerOError(String id) {
        Optional<Notificacion> opt = notificacionRepository.findById(id);
        if (opt.isEmpty()) {
            throw new ReglaNegocioException("La notificación no existe.");
        }
        return opt.get();
    }
}
