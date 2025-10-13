package com.uade.tpo.supermercado.excepciones;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST, reason = "El producto no existe.")
public class ProductoNotFoundException extends Exception {
    
    public ProductoNotFoundException(String message) {
        super(message);
    }
    
}
