package com.cuidared.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Clase que representa a un Padre o responsable legal en CuidaRed.
 * Extiende de Usuario.
 */
public class Padre extends Usuario {

    /**
     * Lista de identificadores de las solicitudes que el padre ha creado.
     */
    private List<String> solicitudesIds;

    public Padre() {
        super();
        this.solicitudesIds = new ArrayList<>();
    }

    public Padre(String nombre, String correo, String telefono) {
        super(nombre, correo, telefono);
        this.solicitudesIds = new ArrayList<>();
    }

    public List<String> getSolicitudesIds() {
        return solicitudesIds;
    }

    public void setSolicitudesIds(List<String> solicitudesIds) {
        this.solicitudesIds = solicitudesIds;
    }

    public void addSolicitudId(String solicitudId) {
        this.solicitudesIds.add(solicitudId);
    }
}
