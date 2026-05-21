package com.cuidared.models;

import java.util.UUID;

/**
 * Clase que representa una Solicitud de cuidado en CuidaRed.
 */
public class Solicitud {

    private String id;
    private String padreId;
    private String cuidadorId;
    private EstadoSolicitud estado;
    private TipoAsistencia tipo;
    private Horario horario;
    private String descripcion;

    public Solicitud() {
        this.id = UUID.randomUUID().toString();
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    public Solicitud(String padreId, TipoAsistencia tipo, Horario horario, String descripcion) {
        this.id = UUID.randomUUID().toString();
        this.padreId = padreId;
        this.tipo = tipo;
        this.horario = horario;
        this.descripcion = descripcion;
        this.estado = EstadoSolicitud.PENDIENTE;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPadreId() {
        return padreId;
    }

    public void setPadreId(String padreId) {
        this.padreId = padreId;
    }

    public String getCuidadorId() {
        return cuidadorId;
    }

    public void setCuidadorId(String cuidadorId) {
        this.cuidadorId = cuidadorId;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public TipoAsistencia getTipo() {
        return tipo;
    }

    public void setTipo(TipoAsistencia tipo) {
        this.tipo = tipo;
    }

    public Horario getHorario() {
        return horario;
    }

    public void setHorario(Horario horario) {
        this.horario = horario;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}