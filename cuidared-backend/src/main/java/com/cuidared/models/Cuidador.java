package com.cuidared.models;

/**
 * Clase que representa a un Cuidador en CuidaRed.
 * Extiende de Usuario.
 */
public class Cuidador extends Usuario {

    private boolean disponible;
    private double tarifaHora;

    public Cuidador() {
        super();
        this.disponible = true;
    }

    public Cuidador(String nombre, String correo, String telefono, double tarifaHora) {
        super(nombre, correo, telefono);
        this.disponible = true;
        this.tarifaHora = tarifaHora;
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
}
