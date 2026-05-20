package com.cuidared.repositories;

import com.cuidared.models.Solicitud;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de persistencia de Solicitudes en formato JSON.
 */
@Repository
public class SolicitudRepository extends JsonRepository<Solicitud> {

    @Autowired
    public SolicitudRepository(@Value("${cuidared.data.solicitudes:data/solicitudes.json}") String filePath, ObjectMapper objectMapper) {
        super(filePath, objectMapper, Solicitud.class);
    }

    @Override
    protected String getId(Solicitud entity) {
        return entity.getId();
    }
}
