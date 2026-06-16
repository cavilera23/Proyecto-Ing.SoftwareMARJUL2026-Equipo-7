package com.cuidared.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Clase que representa una Notificación dirigida a un usuario de CuidaRed.
 */
@Entity
@Table(name = "notificaciones")
public class Notificacion {

    @Id
    private String id;
    private String usuarioId;
    private String titulo;
    private String mensaje;
    private String tipo;        // INFO, SOLICITUD, CALIFICACION, etc.
    private boolean leida;
    private boolean silenciada;
    private boolean disparada; // true = ya fue avisada/activa; false = recordatorio aún pendiente

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaCreacion;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaProgramada;

    public Notificacion() {
        this.id = UUID.randomUUID().toString();
        this.fechaCreacion = LocalDateTime.now();
        this.leida = false;
        this.silenciada = false;
        this.disparada = true;
        this.tipo = "INFO";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isLeida() {
        return leida;
    }

    public void setLeida(boolean leida) {
        this.leida = leida;
    }

    public boolean isSilenciada() {
        return silenciada;
    }

    public void setSilenciada(boolean silenciada) {
        this.silenciada = silenciada;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaProgramada() {
        return fechaProgramada;
    }

    public void setFechaProgramada(LocalDateTime fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    public boolean isDisparada() {
        return disparada;
    }

    public void setDisparada(boolean disparada) {
        this.disparada = disparada;
    }
}
