package com.cuidared.models;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * Clase que representa un Horario en CuidaRed.
 * Contiene lógica para redondear bloques a 30 minutos y manejar la zona horaria America/Caracas.
 */
public class Horario {

    private String id;
    private ZonedDateTime fechaInicio;
    private ZonedDateTime fechaFin;

    private static final ZoneId ZONA_CARACAS = ZoneId.of("America/Caracas");

    public Horario() {
        this.id = UUID.randomUUID().toString();
    }

    public Horario(ZonedDateTime fechaInicio, ZonedDateTime fechaFin) {
        this.id = UUID.randomUUID().toString();
        this.fechaInicio = redondearA30Minutos(fechaInicio.withZoneSameInstant(ZONA_CARACAS));
        this.fechaFin = redondearA30Minutos(fechaFin.withZoneSameInstant(ZONA_CARACAS));
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ZonedDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(ZonedDateTime fechaInicio) {
        this.fechaInicio = redondearA30Minutos(fechaInicio.withZoneSameInstant(ZONA_CARACAS));
    }

    public ZonedDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(ZonedDateTime fechaFin) {
        this.fechaFin = redondearA30Minutos(fechaFin.withZoneSameInstant(ZONA_CARACAS));
    }

    /**
     * Redondea un ZonedDateTime al bloque de 30 minutos más cercano o siguiente.
     * @param zdt ZonedDateTime a redondear
     * @return ZonedDateTime redondeado
     */
    private ZonedDateTime redondearA30Minutos(ZonedDateTime zdt) {
        if (zdt == null) return null;
        int minuto = zdt.getMinute();
        if (minuto == 0 || minuto == 30) {
            return zdt.withSecond(0).withNano(0);
        } else if (minuto < 30) {
            return zdt.withMinute(30).withSecond(0).withNano(0);
        } else {
            return zdt.plusHours(1).withMinute(0).withSecond(0).withNano(0);
        }
    }

    /**
     * Verifica si este horario se solapa con otro horario.
     * @param otro Horario a comparar
     * @return true si hay solapamiento
     */
    public boolean seSolapaCon(Horario otro) {
        if (otro == null || this.fechaInicio == null || this.fechaFin == null || otro.fechaInicio == null || otro.fechaFin == null) {
            return false;
        }
        return this.fechaInicio.isBefore(otro.fechaFin) && this.fechaFin.isAfter(otro.fechaInicio);
    }
}
