package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.models.Calificacion;
import com.cuidared.models.Cuidador;
import com.cuidared.models.EstadoSolicitud;
import com.cuidared.models.Solicitud;
import com.cuidared.models.Usuario;
import com.cuidared.repositories.CalificacionRepository;
import com.cuidared.repositories.SolicitudRepository;
import com.cuidared.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para manejar la lógica de negocio de las calificaciones.
 */
@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public CalificacionService(CalificacionRepository calificacionRepository,
                               SolicitudRepository solicitudRepository,
                               UsuarioRepository usuarioRepository) {
        this.calificacionRepository = calificacionRepository;
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * Crea una calificación sobre un servicio finalizado.
     * Valida la puntuación, la solicitud y que no exista una calificación previa.
     * Actualiza el promedio del cuidador.
     */
    public Calificacion crearCalificacion(Calificacion calificacion) {
        // 1. Validar puntuación (1-5)
        if (calificacion.getPuntuacion() < 1 || calificacion.getPuntuacion() > 5) {
            throw new ReglaNegocioException("La puntuación debe estar entre 1 y 5 estrellas.");
        }

        // 2. Validar solicitud asociada
        if (calificacion.getSolicitudId() == null || calificacion.getSolicitudId().isBlank()) {
            throw new ReglaNegocioException("La calificación debe estar asociada a una solicitud.");
        }

        Optional<Solicitud> solicitudOpt = solicitudRepository.findById(calificacion.getSolicitudId());
        if (solicitudOpt.isEmpty()) {
            throw new ReglaNegocioException("La solicitud asociada no existe.");
        }

        Solicitud solicitud = solicitudOpt.get();

        // 3. Solo se puede calificar un servicio finalizado
        if (solicitud.getEstado() != EstadoSolicitud.FINALIZADA) {
            throw new ReglaNegocioException("Solo puedes calificar servicios finalizados.");
        }

        // 4. Evitar calificaciones duplicadas sobre la misma solicitud
        boolean yaCalificada = calificacionRepository.findAll().stream()
                .anyMatch(c -> calificacion.getSolicitudId().equals(c.getSolicitudId()));
        if (yaCalificada) {
            throw new ReglaNegocioException("Este servicio ya fue calificado.");
        }

        // 5. Completar datos a partir de la solicitud
        calificacion.setAutorId(solicitud.getPadreId());
        calificacion.setCuidadorId(solicitud.getCuidadorId());

        // 6. Guardar
        Calificacion guardada = calificacionRepository.save(calificacion);

        // 7. Recalcular el promedio del cuidador
        if (calificacion.getCuidadorId() != null) {
            actualizarPromedioCuidador(calificacion.getCuidadorId());
        }

        return guardada;
    }

    public List<Calificacion> obtenerTodas() {
        return calificacionRepository.findAll();
    }

    public List<Calificacion> obtenerPorCuidador(String cuidadorId) {
        return calificacionRepository.findAll().stream()
                .filter(c -> cuidadorId != null && cuidadorId.equals(c.getCuidadorId()))
                .collect(Collectors.toList());
    }

    public Optional<Calificacion> obtenerPorSolicitud(String solicitudId) {
        return calificacionRepository.findAll().stream()
                .filter(c -> solicitudId != null && solicitudId.equals(c.getSolicitudId()))
                .findFirst();
    }

    /**
     * Recalcula y persiste el promedio de calificaciones de un cuidador.
     */
    private void actualizarPromedioCuidador(String cuidadorId) {
        Optional<Usuario> userOpt = usuarioRepository.findById(cuidadorId);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Cuidador cuidador)) {
            return;
        }

        List<Calificacion> delCuidador = obtenerPorCuidador(cuidadorId);
        if (delCuidador.isEmpty()) {
            return;
        }

        double promedio = delCuidador.stream()
                .mapToInt(Calificacion::getPuntuacion)
                .average()
                .orElse(0.0);

        cuidador.setCalificacionPromedio(Math.round(promedio * 10.0) / 10.0);
        usuarioRepository.save(cuidador);
    }
}
