package com.cuidared.schedulers;

import com.cuidared.services.NotificacionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Planificador encargado de disparar los recordatorios programados en el
 * momento exacto (fecha, hora y minuto) en que deben avisar al usuario.
 */
@Component
public class NotificacionScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificacionScheduler.class);

    private final NotificacionService notificacionService;

    public NotificacionScheduler(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    /**
     * Se ejecuta al segundo 0 de cada minuto. Activa los recordatorios cuya
     * fecha/hora/minuto programada ya llegó.
     */
    @Scheduled(cron = "0 * * * * *")
    public void dispararRecordatorios() {
        int disparadas = notificacionService.dispararNotificacionesPendientes();
        if (disparadas > 0) {
            log.info("Se dispararon {} recordatorio(s) programado(s).", disparadas);
        }
    }
}
