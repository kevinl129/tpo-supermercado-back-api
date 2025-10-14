package com.uade.ecommerce.entity;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Entity
@Data
//Es tabla detalle_orden en sql
public class DetalleOrden {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    //Este detalle pertenece a una orden. Una orden tiene muchos detalles.
    @ManyToOne
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    //Este detalle representa un producto comprado. Una orden puede tener varios detalles.
    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    public DetalleOrden() {

    }

    public DetalleOrden(int cantidad, BigDecimal precioUnitario, BigDecimal subtotal, Orden orden, Producto producto) {
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
        this.orden = orden;
        this.producto = producto;
    }





    
}
