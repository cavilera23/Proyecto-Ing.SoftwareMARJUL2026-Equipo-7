package com.cuidared.repositories;

import com.cuidared.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio JPA para la gestión de persistencia de Usuarios en PostgreSQL.
 * Spring Data provee automáticamente findAll(), findById(), save(), deleteById(), etc.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    // Usado en el login: busca por correo (Spring deriva la consulta del nombre).
    Optional<Usuario> findByCorreo(String correo);
}
