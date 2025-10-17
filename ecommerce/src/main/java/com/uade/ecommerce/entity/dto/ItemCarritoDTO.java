package com.uade.ecommerce.entity.dto;

import lombok.Data;

@Data
public class ItemCarritoDTO {
    private int productoId;
    private String nombreProducto;
    private String imageUrl; // ✅ NUEVO CAMPO: URL de la imagen
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    // ✅ CONSTRUCTOR ACTUALIZADO con 6 parámetros
    public ItemCarritoDTO(int productoId, String nombreProducto, String imageUrl, int cantidad, double precioUnitario, double subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.imageUrl = imageUrl; 
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

}