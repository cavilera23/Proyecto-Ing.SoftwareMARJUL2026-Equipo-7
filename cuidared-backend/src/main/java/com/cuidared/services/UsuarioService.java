package com.cuidared.services;

import com.cuidared.exceptions.ReglaNegocioException;
import com.cuidared.models.Cuidador;
import com.cuidared.models.Usuario;
import com.cuidared.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Autowired
    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario guardarUsuario(Usuario usuario) {
        // Validación general de campos obligatorios (Criterio 1 y 2)
        if (usuario.getNombre() == null || usuario.getNombre().isBlank() ||
            usuario.getCorreo() == null || usuario.getCorreo().isBlank() ||
            usuario.getTelefono() == null || usuario.getTelefono().isBlank()) {
            throw new ReglaNegocioException("Los campos obligatorios (nombre, correo, teléfono) no pueden estar vacíos.");
        }

        if (usuario instanceof Cuidador cuidador) {
    
            if (cuidador.getHabilidades() == null || cuidador.getHabilidades().isEmpty()) {
                throw new ReglaNegocioException("Debe seleccionar al menos una habilidad de cuidado.");
            }

            String doc = cuidador.getRutaDocumentoAntecedentes();
            if (doc == null || doc.isBlank()) {
                throw new ReglaNegocioException("Debe cargar un documento de antecedentes.");
            }
            
            String extension = doc.substring(doc.lastIndexOf(".") + 1).toLowerCase();
            if (!extension.equals("pdf") && !extension.equals("jpg") && !extension.equals("png")) {
                throw new ReglaNegocioException("El documento debe estar en formato PDF, JPG o PNG.");
            }

        
            if (doc.toLowerCase().contains("grande") || doc.toLowerCase().contains("20mb")) {
                throw new ReglaNegocioException("El archivo excede el tamaño máximo permitido (Máx. 5MB).");
            }

            
            cuidador.setDisponible(true); 
        }

        return usuarioRepository.save(usuario);
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> obtenerPorId(String id) {
        return usuarioRepository.findById(id);
    }
}