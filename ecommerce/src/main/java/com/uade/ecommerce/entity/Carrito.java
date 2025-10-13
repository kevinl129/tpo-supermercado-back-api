package com.uade.ecommerce.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.CascadeType;
import jakarta.persistence.EnumType;
import lombok.Data;

@Data
@Entity
public class Carrito {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private LocalDateTime fecha_creacion = LocalDateTime.now();

    @Column(nullable = true)
    private LocalDateTime fechaActivacion;

    @OneToOne
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", nullable = false, unique = true)
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "estado")
    private EstadoCarrito estado;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<ItemCarrito> itemsCarrito = new ArrayList<>(); // Lista de los items del carrito

    public Carrito() {
    }

    public Carrito(Usuario usuario, EstadoCarrito estado, LocalDateTime fecha_creacion) {
        this.fecha_creacion = fecha_creacion;
        this.usuario = usuario;
        this.itemsCarrito = new ArrayList<>();// Inicializar la lista vacía
        this.estado = estado;
    }
