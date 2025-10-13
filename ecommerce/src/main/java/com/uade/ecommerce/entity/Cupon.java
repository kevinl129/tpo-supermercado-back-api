package com.uade.ecommerce.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class Cupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 20, unique = true, nullable = false)
    private String codigo;

    @Column(length = 20, nullable = false)
    private String tipo;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(nullable = false)
    private LocalDate validoHasta;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal minimoCompra;

    @Column(nullable = false)
    private int usosMaximos;

    public Cupon() {

    }

    public Cupon(String codigo, String tipo, BigDecimal valor, LocalDate validoHasta, BigDecimal minimoCompra, int usosMaximos) {
        this.codigo = codigo;
        this.tipo = tipo;
        this.valor = valor;
        this.validoHasta = validoHasta;
        this.minimoCompra = minimoCompra;
        this.usosMaximos = usosMaximos;
    }
    
}

