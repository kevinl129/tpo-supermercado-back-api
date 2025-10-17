package com.uade.ecommerce.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 100, nullable = false)
    private String calle;

    @Column(length = 10, nullable = false)
    private String numero;

    @Column(length = 50)
    private String pisoDepto;

    @Column(length = 50, nullable = false)
    private String ciudad;

    @Column(length = 50, nullable = false)
    private String provincia;

    @Column(length = 10, nullable = false)
    private String codigoPostal;

    @Column(length = 20)
    private String tipoVivienda;

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    // Comentá o eliminá el @JsonBackReference temporalmente para testing
    // @JsonBackReference
    @JsonIgnoreProperties({"direcciones", "password", "email"}) // Evita el bucle infinito
    private Usuario usuario;
}

