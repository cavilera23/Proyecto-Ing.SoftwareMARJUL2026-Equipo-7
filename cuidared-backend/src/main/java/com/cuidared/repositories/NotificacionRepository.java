package com.cuidared.repositories;

import com.cuidared.models.Notificacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de persistencia de Notificaciones en formato JSON.
 */
@Repository
public class NotificacionRepository extends JsonRepository<Notificacion> {

    @Autowired
    public NotificacionRepository(@Value("${cuidared.data.notificaciones:data/notificaciones.json}") String filePath, ObjectMapper objectMapper) {
        super(filePath, objectMapper, Notificacion.class);
    }

    @Override
    protected String getId(Notificacion entity) {
        return entity.getId();
    }
}
