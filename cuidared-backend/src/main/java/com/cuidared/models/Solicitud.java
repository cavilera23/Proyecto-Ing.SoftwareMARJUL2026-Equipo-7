package com.cuidared.models;

import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * Clase que representa una Solicitud de cuidado en CuidaRed.
 */
@Entity
@Table(name = "solicitudes")
public class Solicitud {

    private LocalDate fecha;
    private LocalTime horaInicio;
    private int duracionHoras; // 1-12 horas

    @Id
    private String id;
    private String padreId;
    // Nombre del padre que creó la solicitud (denormalizado al crearla),
    // para mostrar "quién la creó" sin tener que volver a consultar el usuario.
    private String padreNombre;
    private String cuidadorId;

    @Enumerated(EnumType.STRING)
    private EstadoSolicitud estado;

    @Enumerated(EnumType.STRING)
    private TipoAsistencia tipo;

    @Embedded
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

    public String getPadreNombre() {
        return padreNombre;
    }

    public void setPadreNombre(String padreNombre) {
        this.padreNombre = padreNombre;
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

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraInicio() {
        return horaInicio;
    }

    public void setHoraInicio(LocalTime horaInicio) {
        this.horaInicio = horaInicio;
    }

    public int getDuracionHoras() {
        return duracionHoras;
    }

    public void setDuracionHoras(int duracionHoras) {
        this.duracionHoras = duracionHoras;
    }
}
