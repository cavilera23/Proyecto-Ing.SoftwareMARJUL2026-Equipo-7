package com.cuidared.services;

import com.cuidared.models.Cuidador;
import com.cuidared.models.Horario;
import com.cuidared.models.Solicitud;
import com.cuidared.repositories.SolicitudRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Servicio para gestionar la agenda y horarios.
 */
@Service
public class AgendaService {

    private final SolicitudRepository solicitudRepository;

    @Autowired
    public AgendaService(SolicitudRepository solicitudRepository) {
        this.solicitudRepository = solicitudRepository;
    }

    /**
     * Verifica si un cuidador tiene disponibilidad en el horario dado.
     * @param cuidadorId ID del cuidador
     * @param nuevoHorario Horario a verificar
     * @return true si está disponible, false si hay solapamiento
     */
    public boolean verificarDisponibilidadCuidador(String cuidadorId, Horario nuevoHorario) {
        List<Solicitud> solicitudesCuidador = solicitudRepository.findAll().stream()
                .filter(s -> cuidadorId.equals(s.getCuidadorId()))
                .filter(s -> s.getEstado().name().equals("ACEPTADA"))
                .toList();

        for (Solicitud s : solicitudesCuidador) {
            if (s.getHorario().seSolapaCon(nuevoHorario)) {
                return false;
            }
        }
        
        return true;
    }
}
