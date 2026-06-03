package com.cuidared.repositories;

import com.cuidared.models.Calificacion;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de persistencia de Calificaciones en formato JSON.
 */
@Repository
public class CalificacionRepository extends JsonRepository<Calificacion> {

    @Autowired
    public CalificacionRepository(@Value("${cuidared.data.calificaciones:data/calificaciones.json}") String filePath, ObjectMapper objectMapper) {
        super(filePath, objectMapper, Calificacion.class);
    }

    @Override
    protected String getId(Calificacion entity) {
        return entity.getId();
    }
}
