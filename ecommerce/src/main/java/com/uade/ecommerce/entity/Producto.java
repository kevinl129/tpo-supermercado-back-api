package com.uade.ecommerce.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String descripcion;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private int stock_minimo;

    @Column(length = 100, nullable = false)
    private String nombre;

    @Column(length = 50)
    private String marca;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;
    
    @OneToMany(mappedBy = "producto", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference // evita la recursividad infinita al serializar la entidad
    private List<Imagen> imagenes = new ArrayList<>();
}
