package com.cuidared.repositories;

import com.cuidared.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio JPA para la gestión de persistencia de Usuarios en PostgreSQL.
 * Spring Data provee automáticamente findAll(), findById(), save(), deleteById(), etc.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {
}
