package com.uade.ecommerce.controller.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.uade.ecommerce.entity.Imagen;
import com.uade.ecommerce.entity.Producto;

import lombok.Data;

@Data
public class CatalogoResponse {
    private String nombre;
    private String marca;
    private BigDecimal precio;
    private List<String> imagenes;
    private BigDecimal descuento;

    public CatalogoResponse(Producto producto) {
        this.nombre = producto.getNombre();
        this.marca = producto.getMarca();
        this.precio = producto.getPrecio();
        this.descuento = producto.getDescuento();
        this.imagenes = new ArrayList<>();
        cargarImagenes(producto);
    }

    private void cargarImagenes(Producto producto) {
        List<Imagen> imagenes = producto.getImagenes();
        for (Imagen imagen : imagenes) {
            this.imagenes.add(imagen.getImagen());
        }
    }
}
