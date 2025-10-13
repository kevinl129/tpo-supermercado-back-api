package com.uade.ecommerce.entity;

import jakarta.persistence.*;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Data
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
    private String tipoVivienda; // "casa" o "departamento"

    @ManyToOne(optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonBackReference
    private Usuario usuario;

    // Puedes agregar campos extra como "referencia", "nombreDireccion", etc.
}
