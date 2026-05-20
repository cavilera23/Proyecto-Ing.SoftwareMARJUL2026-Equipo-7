package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;
import com.cuidared.models.Padre;
import com.cuidared.models.Solicitud;
import com.cuidared.models.Usuario;
import com.cuidared.repositories.SolicitudRepository;
import com.cuidared.repositories.UsuarioRepository;
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
        Optional<Usuario> userOpt = usuarioRepository.findById(solicitud.getPadreId());
        
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Padre)) {
            throw new ReglaNegocioException("El Padre con ID " + solicitud.getPadreId() + " no existe o no es de tipo Padre.");
        }

        Padre padre = (Padre) userOpt.get();
        
        // Contar solicitudes activas (Pendientes o Aceptadas)
        long activas = padre.getSolicitudesIds().stream()
            .map(solicitudRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(s -> s.getEstado().name().equals("PENDIENTE") || s.getEstado().name().equals("ACEPTADA"))
            .count();

        if (activas >= 3) {
            throw new ReglaNegocioException("Un padre no puede tener más de 3 solicitudes activas simultáneamente.");
        }

        // Validar solapamiento con otras solicitudes del mismo padre
        List<Solicitud> solicitudesPadre = padre.getSolicitudesIds().stream()
            .map(solicitudRepository::findById)
            .filter(Optional::isPresent)
            .map(Optional::get)
            .filter(s -> s.getEstado().name().equals("PENDIENTE") || s.getEstado().name().equals("ACEPTADA"))
            .toList();

        for (Solicitud s : solicitudesPadre) {
            if (s.getHorario().seSolapaCon(solicitud.getHorario())) {
                throw new SolapamientoHorarioException("El horario de la nueva solicitud se solapa con una solicitud activa.");
            }
        }

        Solicitud guardada = solicitudRepository.save(solicitud);
        padre.addSolicitudId(guardada.getId());
        usuarioRepository.save(padre); // Actualizar padre
        
        return guardada;
    }

    public List<Solicitud> obtenerTodas() {
        return solicitudRepository.findAll();
    }
}
