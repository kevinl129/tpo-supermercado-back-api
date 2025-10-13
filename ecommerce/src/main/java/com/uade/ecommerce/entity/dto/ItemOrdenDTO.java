package com.uade.ecommerce.entity.dto;

import lombok.Data;

@Data
public class ItemOrdenDTO {
    private int productoId;
    private String nombreProducto;
    private int cantidad;
    private double precioUnitario;
    private double subtotal;

    public ItemOrdenDTO(int productoId, String nombreProducto, int cantidad, double precioUnitario, double subtotal) {
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

}