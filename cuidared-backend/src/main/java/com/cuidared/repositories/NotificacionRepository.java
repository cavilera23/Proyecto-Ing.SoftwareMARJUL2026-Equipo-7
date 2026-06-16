package com.cuidared.repositories;

import com.cuidared.models.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la gestión de persistencia de Notificaciones en PostgreSQL.
 */
@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, String> {
}
