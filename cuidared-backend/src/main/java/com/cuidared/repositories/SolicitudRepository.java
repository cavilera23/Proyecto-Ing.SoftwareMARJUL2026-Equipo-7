package com.cuidared.repositories;

import com.cuidared.models.Solicitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la gestión de persistencia de Solicitudes en PostgreSQL.
 */
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, String> {
}
