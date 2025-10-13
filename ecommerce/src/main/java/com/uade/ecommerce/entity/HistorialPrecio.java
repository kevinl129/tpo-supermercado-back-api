package com.uade.ecommerce.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
public class HistorialPrecio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioAnterior;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precioNuevo;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime fechaCambio;

    public HistorialPrecio() {
        
    }

    public HistorialPrecio(Producto producto, BigDecimal precioAnterior, BigDecimal precioNuevo, LocalDateTime fechaCambio) {
        this.producto = producto;
        this.precioAnterior = precioAnterior;
        this.precioNuevo = precioNuevo;
        this.fechaCambio = fechaCambio;
    }
    
}
