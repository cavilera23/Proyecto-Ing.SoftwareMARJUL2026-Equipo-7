package com.cuidared.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;

/**
 * Clase abstracta que representa a un usuario genérico en el sistema CuidaRed.
 * Se utiliza herencia para distinguir entre Padres y Cuidadores.
 * Jackson se configura para manejar el polimorfismo en la serialización/deserialización.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME, 
    include = JsonTypeInfo.As.PROPERTY, 
    property = "tipoUsuario"
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = Padre.class, name = "PADRE"),
    @JsonSubTypes.Type(value = Cuidador.class, name = "CUIDADOR")
})
public abstract class Usuario {
    
    private String id;
    private String nombre;
    private String correo;
    private String telefono;
    private double calificacionPromedio;

    /**
     * Constructor por defecto requerido por Jackson.
     */
    public Usuario() {
        this.id = UUID.randomUUID().toString();
    }

    public Usuario(String nombre, String correo, String telefono) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.calificacionPromedio = 0.0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public double getCalificacionPromedio() {
        return calificacionPromedio;
    }

    public void setCalificacionPromedio(double calificacionPromedio) {
        this.calificacionPromedio = calificacionPromedio;
    }
}
