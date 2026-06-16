package com.cuidared.models;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("CUIDADOR")
public class Cuidador extends Usuario {

    private boolean disponible;
    private double tarifaHora;

    @ElementCollection
    @CollectionTable(name = "cuidador_habilidades", joinColumns = @JoinColumn(name = "cuidador_id"))
    @Enumerated(EnumType.STRING)
    private List<TipoAsistencia> habilidades;

    private String rutaDocumentoAntecedentes;

    @ElementCollection
    @CollectionTable(name = "cuidador_horarios", joinColumns = @JoinColumn(name = "cuidador_id"))
    // @OrderBy ordena los bloques por fecha de inicio al leerlos, sin columna física.
    // Da un orden estable (los endpoints modificar/eliminar usan el índice en este orden)
    // y evita el bug de @OrderColumn, que Hibernate crea NOT NULL y luego viola al insertar.
    @OrderBy("fechaInicio ASC")
    private List<Horario> horariosDisponibles;

    public Cuidador() {
        super();
        this.setTipoUsuario("CUIDADOR");
        this.disponible = true;
        this.habilidades = new ArrayList<>();
        this.horariosDisponibles = new ArrayList<>();
    }

    public Cuidador(String nombre, String correo, String telefono, double tarifaHora) {
        super(nombre, correo, telefono);
        this.setTipoUsuario("CUIDADOR");
        this.disponible = true;
        this.tarifaHora = tarifaHora;
        this.habilidades = new ArrayList<>();
        this.horariosDisponibles = new ArrayList<>();
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    public double getTarifaHora() {
        return tarifaHora;
    }

    public void setTarifaHora(double tarifaHora) {
        this.tarifaHora = tarifaHora;
    }

    public List<TipoAsistencia> getHabilidades() {
        if (this.habilidades == null) {
            this.habilidades = new ArrayList<>();
        }
        return habilidades;
    }

    public void setHabilidades(List<TipoAsistencia> habilidades) {
        if (habilidades == null) {
            this.habilidades = new ArrayList<>();
        } else {
            this.habilidades = habilidades;
        }
    }

    public String getRutaDocumentoAntecedentes() {
        return rutaDocumentoAntecedentes;
    }

    public void setRutaDocumentoAntecedentes(String rutaDocumentoAntecedentes) {
        this.rutaDocumentoAntecedentes = rutaDocumentoAntecedentes;
    }

    public List<Horario> getHorariosDisponibles() {
        if (this.horariosDisponibles == null) {
            this.horariosDisponibles = new ArrayList<>();
        }
        return horariosDisponibles;
    }

    public void setHorariosDisponibles(List<Horario> horariosDisponibles) {
        if (horariosDisponibles == null) {
            this.horariosDisponibles = new ArrayList<>();
        } else {
            this.horariosDisponibles = horariosDisponibles;
        }
    }

    public void addHorarioDisponible(Horario horario) {
        if (this.horariosDisponibles == null) {
            this.horariosDisponibles = new ArrayList<>();
        }
        this.horariosDisponibles.add(horario);
    }
}