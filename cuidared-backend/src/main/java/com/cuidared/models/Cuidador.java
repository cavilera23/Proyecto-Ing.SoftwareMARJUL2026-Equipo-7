package com.cuidared.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa a un Cuidador en CuidaRed.
 * Extiende de Usuario.
 */
public class Cuidador extends Usuario {

    private boolean disponible;
    private double tarifaHora;
    
    // --- Nuevos atributos para HU 6 (Perfil) ---
    private List<TipoAsistencia> habilidades;
    private String rutaDocumentoAntecedentes; // Para simular la carga del PDF/JPG
    
    // --- Nuevos atributos para HU 5 (Horarios) ---
    private List<Horario> horariosDisponibles;

    public Cuidador() {
        super();
        this.disponible = true;
        this.habilidades = new ArrayList<>();
        this.horariosDisponibles = new ArrayList<>();
    }

    public Cuidador(String nombre, String correo, String telefono, double tarifaHora) {
        super(nombre, correo, telefono);
        this.disponible = true;
        this.tarifaHora = tarifaHora;
        this.habilidades = new ArrayList<>();
        this.horariosDisponibles = new ArrayList<>();
    }

    // --- Getters y Setters originales ---
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    
    public double getTarifaHora() { return tarifaHora; }
    public void setTarifaHora(double tarifaHora) { this.tarifaHora = tarifaHora; }

    // --- Nuevos Getters y Setters ---
    public List<TipoAsistencia> getHabilidades() { return habilidades; }
    public void setHabilidades(List<TipoAsistencia> habilidades) { this.habilidades = habilidades; }

    public String getRutaDocumentoAntecedentes() { return rutaDocumentoAntecedentes; }
    public void setRutaDocumentoAntecedentes(String rutaDocumentoAntecedentes) { this.rutaDocumentoAntecedentes = rutaDocumentoAntecedentes; }

    public List<Horario> getHorariosDisponibles() { return horariosDisponibles; }
    public void setHorariosDisponibles(List<Horario> horariosDisponibles) { this.horariosDisponibles = horariosDisponibles; }
    
    // Método útil para la HU 5
    public void addHorarioDisponible(Horario nuevoHorario) {
        this.horariosDisponibles.add(nuevoHorario);
    }
}