package com.cuidared.repositories;

import com.cuidared.models.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la gestión de persistencia de Calificaciones en PostgreSQL.
 */
@Repository
public interface CalificacionRepository extends JpaRepository<Calificacion, String> {
}
