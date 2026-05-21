package com.cuidared.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Excepción lanzada cuando se detecta un solapamiento en el horario.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class SolapamientoHorarioException extends RuntimeException {

    public SolapamientoHorarioException(String message) {
        super(message);
    }
}