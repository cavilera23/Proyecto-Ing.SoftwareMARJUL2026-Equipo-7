package com.cuidared.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_usuario")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "tipoUsuario",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = Padre.class, name = "PADRE"),
        @JsonSubTypes.Type(value = Cuidador.class, name = "CUIDADOR")
})
public abstract class Usuario {

    // Mapeado de solo lectura sobre la columna discriminadora: la subclase
    // (@DiscriminatorValue) es la que decide el valor que se persiste.
    @Column(name = "tipo_usuario", insertable = false, updatable = false)
    private String tipoUsuario;

    @Id
    private String id;
    private String nombre;
    private String correo;
    private String telefono;
    private double calificacionPromedio;

    // Se recibe al registrar/login (WRITE_ONLY) pero NUNCA se serializa de vuelta
    // al cliente. Guardada en texto plano por decisión del proyecto académico.
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String contrasena;

    public Usuario() {
        this.id = UUID.randomUUID().toString();
        this.calificacionPromedio = 0.0;
    }

    public Usuario(String nombre, String correo, String telefono) {
        this.id = UUID.randomUUID().toString();
        this.nombre = nombre;
        this.correo = correo;
        this.telefono = telefono;
        this.calificacionPromedio = 0.0;
    }

    public String getTipoUsuario() {
        return tipoUsuario;
    }

    public void setTipoUsuario(String tipoUsuario) {
        this.tipoUsuario = tipoUsuario;
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

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}