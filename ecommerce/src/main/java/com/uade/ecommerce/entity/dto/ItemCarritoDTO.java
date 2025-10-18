package com.uade.ecommerce.entity.dto;

import lombok.Data;

@Data
public class ItemCarritoDTO {
    private int productoId;
    private String nombreProducto;
    private String imageUrl;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public ItemCarritoDTO(int productoId, String nombreProducto, String imageUrl, int cantidad, double precioUnitario, double subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.imageUrl = imageUrl; 
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

}