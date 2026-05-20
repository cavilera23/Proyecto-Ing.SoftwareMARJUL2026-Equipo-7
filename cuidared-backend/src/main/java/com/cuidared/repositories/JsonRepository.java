package com.cuidared.repositories;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio base genérico para manejar la persistencia en archivos JSON.
 * @param <T> Tipo de entidad a persistir
 */
public abstract class JsonRepository<T> {

    private final File jsonFile;
    protected final ObjectMapper objectMapper;
    private final Class<T> typeParameterClass;

    public JsonRepository(String filePath, ObjectMapper objectMapper, Class<T> typeParameterClass) {
        this.jsonFile = new File(filePath);
        this.objectMapper = objectMapper;
        this.typeParameterClass = typeParameterClass;
        
        // Crear el archivo si no existe
        if (!jsonFile.exists()) {
            try {
                jsonFile.getParentFile().mkdirs();
                jsonFile.createNewFile();
                objectMapper.writeValue(jsonFile, new ArrayList<T>());
            } catch (IOException e) {
                throw new RuntimeException("Error inicializando el archivo JSON: " + filePath, e);
            }
        }
    }

    /**
     * Obtiene todos los registros del archivo JSON.
     * @return Lista de entidades
     */
    public List<T> findAll() {
        try {
            CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, typeParameterClass);
            List<T> data = objectMapper.readValue(jsonFile, listType);
            return data != null ? data : new ArrayList<>();
        } catch (IOException e) {
            throw new RuntimeException("Error leyendo el archivo JSON: " + jsonFile.getPath(), e);
        }
    }

    /**
     * Guarda la lista completa de entidades en el archivo JSON.
     * @param entities Lista a guardar
     */
    protected void saveAll(List<T> entities) {
        try {
            objectMapper.writeValue(jsonFile, entities);
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo en el archivo JSON: " + jsonFile.getPath(), e);
        }
    }

    /**
     * Guarda o actualiza una entidad.
     * @param entity Entidad a guardar
     * @return La entidad guardada
     */
    public T save(T entity) {
        List<T> entities = findAll();
        String id = getId(entity);
        
        boolean found = false;
        for (int i = 0; i < entities.size(); i++) {
            if (getId(entities.get(i)).equals(id)) {
                entities.set(i, entity);
                found = true;
                break;
            }
        }
        
        if (!found) {
            entities.add(entity);
        }
        
        saveAll(entities);
        return entity;
    }

    /**
     * Busca una entidad por su ID.
     * @param id Identificador
     * @return Optional con la entidad si se encuentra
     */
    public Optional<T> findById(String id) {
        return findAll().stream()
                .filter(entity -> getId(entity).equals(id))
                .findFirst();
    }

    /**
     * Método abstracto para obtener el ID de la entidad genérica.
     * @param entity Entidad
     * @return ID como String
     */
    protected abstract String getId(T entity);
}
