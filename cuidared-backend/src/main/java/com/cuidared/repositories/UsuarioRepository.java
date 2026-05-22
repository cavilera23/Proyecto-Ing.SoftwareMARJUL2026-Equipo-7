package com.cuidared.repositories;

import com.cuidared.models.Usuario;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para la gestión de persistencia de Usuarios en formato JSON.
 */
@Repository
public class UsuarioRepository extends JsonRepository<Usuario> {

    @Autowired
    public UsuarioRepository(
            @Value("${cuidared.data.usuarios:data/usuarios.json}") String filePath,
            ObjectMapper objectMapper
    ) {
        super(filePath, objectMapper, Usuario.class);
    }

    @Override
    protected String getId(Usuario entity) {
        return entity.getId();
    }
}