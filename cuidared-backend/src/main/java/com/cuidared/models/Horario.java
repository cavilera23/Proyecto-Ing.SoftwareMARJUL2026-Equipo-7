package com.cuidared.models;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;

public class Horario {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaInicio;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fechaFin;

    public Horario() {
    }

    public Horario(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean seSolapaCon(Horario otro) {
        if (
                this.fechaInicio == null ||
                this.fechaFin == null ||
                otro == null ||
                otro.getFechaInicio() == null ||
                otro.getFechaFin() == null
        ) {
            return false;
        }

        return this.fechaInicio.isBefore(otro.getFechaFin())
                && this.fechaFin.isAfter(otro.getFechaInicio());
    }
}