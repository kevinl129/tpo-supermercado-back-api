package com.uade.ecommerce.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ManejoErrores {
    // Maneja ParametroFueraDeRangoException
    @ExceptionHandler(ParametroFueraDeRangoException.class)
    public ResponseEntity<String> manejarParametroFueraDeRango(ParametroFueraDeRangoException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    // Maneja NoEncontradoException
    @ExceptionHandler(NoEncontradoException.class)
    public ResponseEntity<String> manejarNoEncontrado(NoEncontradoException ex) {
        System.out.println("Excepción capturada: " + ex.getMessage());
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // Maneja DatoDuplicadoException
    @ExceptionHandler(DatoDuplicadoException.class)
    public ResponseEntity<String> manejarDatoDuplicado(DatoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    // Manejo de errores genéricos
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> manejarExcepcionGenerica(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Ha ocurrido un error inesperado: " + ex.getMessage());

    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<String> manejarStockInsuficiente(StockInsuficienteException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(EstadoInvalidoException.class)
    public ResponseEntity<String> manejarEstadoInvalido(EstadoInvalidoException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
