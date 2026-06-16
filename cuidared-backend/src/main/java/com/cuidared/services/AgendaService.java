package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.exceptions.SolapamientoHorarioException;
import com.cuidared.models.Cuidador;
import com.cuidared.models.Horario;
import com.cuidared.models.Solicitud;
import com.cuidared.models.Usuario;
import com.cuidared.repositories.SolicitudRepository;
import com.cuidared.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la agenda, horarios y solapamientos.
 */
@Service
public class AgendaService {

    private final SolicitudRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    @Autowired
    public AgendaService(SolicitudRepository solicitudRepository, UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Horario registrarHorarioDisponible(String cuidadorId, Horario nuevoHorario) {

        if (cuidadorId == null || cuidadorId.isBlank()) {
            throw new ReglaNegocioException("El ID del cuidador es obligatorio.");
        }

        if (nuevoHorario == null) {
            throw new ReglaNegocioException("El horario es obligatorio.");
        }

        if (nuevoHorario.getFechaInicio() == null || nuevoHorario.getFechaFin() == null) {
            throw new ReglaNegocioException("La fecha de inicio y fin son obligatorias.");
        }

        if (!nuevoHorario.getFechaInicio().isBefore(nuevoHorario.getFechaFin())) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        Optional<Usuario> userOpt = usuarioRepository.findById(cuidadorId);

        if (userOpt.isEmpty()) {
            throw new ReglaNegocioException("El usuario con ID suministrado no existe.");
        }

        Usuario usuario = userOpt.get();

        if (!(usuario instanceof Cuidador)) {
            throw new ReglaNegocioException(
                    "El usuario existe, pero no fue cargado como Cuidador. Revisa que en usuarios.json tenga \"tipoUsuario\": \"CUIDADOR\"."
            );
        }

        Cuidador cuidador = (Cuidador) usuario;

        for (Horario horarioExistente : cuidador.getHorariosDisponibles()) {
            if (horarioExistente.seSolapaCon(nuevoHorario)) {
                throw new SolapamientoHorarioException(
                        "El horario seleccionado choca con un bloque de disponibilidad ya existente."
                );
            }
        }

        if (!verificarDisponibilidadCuidador(cuidadorId, nuevoHorario)) {
            throw new SolapamientoHorarioException(
                    "El horario seleccionado choca con un servicio ya agendado y aceptado."
            );
        }

        cuidador.addHorarioDisponible(nuevoHorario);
        usuarioRepository.save(cuidador);

        return nuevoHorario;
    }

    /**
     * Modifica un bloque de disponibilidad existente de un cuidador (Sprint 2 - versión básica).
     * El bloque se identifica por su posición (índice) en la lista de horarios disponibles.
     */
    @Transactional
    public Horario modificarHorario(String cuidadorId, int indice, Horario nuevoHorario) {
        Cuidador cuidador = obtenerCuidador(cuidadorId);
        List<Horario> horarios = cuidador.getHorariosDisponibles();

        if (indice < 0 || indice >= horarios.size()) {
            throw new ReglaNegocioException("El horario indicado no existe.");
        }

        if (nuevoHorario == null || nuevoHorario.getFechaInicio() == null || nuevoHorario.getFechaFin() == null) {
            throw new ReglaNegocioException("La fecha de inicio y fin son obligatorias.");
        }

        if (!nuevoHorario.getFechaInicio().isBefore(nuevoHorario.getFechaFin())) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        // Validar que no choque con OTROS bloques (se excluye el que se está modificando)
        for (int i = 0; i < horarios.size(); i++) {
            if (i != indice && horarios.get(i).seSolapaCon(nuevoHorario)) {
                throw new SolapamientoHorarioException(
                        "El horario seleccionado choca con un bloque de disponibilidad ya existente.");
            }
        }

        if (!verificarDisponibilidadCuidador(cuidadorId, nuevoHorario)) {
            throw new SolapamientoHorarioException(
                    "El horario seleccionado choca con un servicio ya agendado y aceptado.");
        }

        horarios.set(indice, nuevoHorario);
        usuarioRepository.save(cuidador);

        return nuevoHorario;
    }

    /**
     * Elimina un bloque de disponibilidad de un cuidador (Sprint 2 - versión básica).
     * El bloque se identifica por su posición (índice) en la lista de horarios disponibles.
     */
    @Transactional
    public void eliminarHorario(String cuidadorId, int indice) {
        Cuidador cuidador = obtenerCuidador(cuidadorId);
        List<Horario> horarios = cuidador.getHorariosDisponibles();

        if (indice < 0 || indice >= horarios.size()) {
            throw new ReglaNegocioException("El horario indicado no existe.");
        }

        horarios.remove(indice);
        usuarioRepository.save(cuidador);
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
     * Busca los cuidadores disponibles para una franja horaria solicitada por el usuario.
     * Un cuidador es candidato si:
     *  - está marcado como disponible,
     *  - tiene un bloque de disponibilidad que cubre por completo la franja solicitada, y
     *  - no tiene un servicio ya aceptado que se solape con esa franja.
     *
     * @param solicitado Franja horaria que el usuario necesita cubrir
     * @return Lista de cuidadores que pueden atender sin conflictos
     */
    public List<Cuidador> buscarCuidadoresDisponibles(Horario solicitado) {
        if (solicitado == null || solicitado.getFechaInicio() == null || solicitado.getFechaFin() == null) {
            throw new ReglaNegocioException("Debe especificar la fecha y hora de inicio y fin de su necesidad.");
        }

        if (!solicitado.getFechaInicio().isBefore(solicitado.getFechaFin())) {
            throw new ReglaNegocioException("La hora de inicio debe ser anterior a la hora de fin.");
        }

        return usuarioRepository.findAll().stream()
                .filter(u -> u instanceof Cuidador)
                .map(u -> (Cuidador) u)
                .filter(Cuidador::isDisponible)
                .filter(c -> tieneBloqueQueCubre(c, solicitado))
                .filter(c -> verificarDisponibilidadCuidador(c.getId(), solicitado))
                .collect(Collectors.toList());
    }

    private boolean tieneBloqueQueCubre(Cuidador cuidador, Horario solicitado) {
        return cuidador.getHorariosDisponibles().stream()
                .anyMatch(bloque -> bloque.contiene(solicitado));
    }

    public List<Horario> obtenerHorariosCuidador(String cuidadorId) {
        if (cuidadorId == null || cuidadorId.isBlank()) {
            throw new ReglaNegocioException("El ID del cuidador es obligatorio.");
        }
        Optional<Usuario> userOpt = usuarioRepository.findById(cuidadorId);
        if (userOpt.isEmpty() || !(userOpt.get() instanceof Cuidador)) {
            throw new ReglaNegocioException("Cuidador no encontrado.");
        }
        List<Horario> horarios = ((Cuidador) userOpt.get()).getHorariosDisponibles();
        return horarios != null ? horarios : Collections.emptyList();
    }

    public boolean verificarDisponibilidadCuidador(String cuidadorId, Horario nuevoHorario) {

        if (cuidadorId == null || cuidadorId.isBlank()) {
            throw new ReglaNegocioException("El ID del cuidador es obligatorio.");
        }

        if (nuevoHorario == null) {
            throw new ReglaNegocioException("El horario es obligatorio.");
        }

        List<Solicitud> solicitudesCuidador = solicitudRepository.findAll().stream()
                .filter(s -> cuidadorId.equals(s.getCuidadorId()))
                .filter(s -> s.getEstado() != null)
                .filter(s -> "ACEPTADA".equals(s.getEstado().name()))
                .toList();

        for (Solicitud solicitud : solicitudesCuidador) {
            if (solicitud.getHorario() != null && solicitud.getHorario().seSolapaCon(nuevoHorario)) {
                return false;
            }
        }

        return true;
    }
}