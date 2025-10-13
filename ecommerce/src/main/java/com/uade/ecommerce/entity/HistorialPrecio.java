package com.uade.ecommerce.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;   
import lombok.AllArgsConstructor;   

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor 
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

    @CreationTimestamp 
    @Column(nullable = false) 
    private LocalDateTime fechaCambio; 
   
}