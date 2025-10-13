package com.uade.tpo.supermercado.controller.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.uade.tpo.supermercado.entity.Imagen;
import com.uade.tpo.supermercado.entity.Producto;
import lombok.Data;

@Data
public class ProductoDTO {
    private int id;
    private String nombre;
    private String descripcion;
    private List<String> imagenes;// puede ser null
    private BigDecimal precio;
    private String marca;// puede ser null
    private String categoria;
    private int stock;
    private String unidadMedida;// puede ser null
    private BigDecimal descuento;

    // Constructors
    public ProductoDTO(Producto producto) {
        this.id = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.imagenes = new ArrayList<>();
        cargarImagenes(producto);
        this.precio = producto.getPrecio();
        this.marca = producto.getMarca();
        this.categoria = producto.getCategoria().getNombre();
        this.stock = producto.getStock();
        this.unidadMedida = producto.getUnidad_medida();
        this.descuento = producto.getDescuento();
    }

    private void cargarImagenes(Producto producto) {
        List<Imagen> imagenes = producto.getImagenes();
        for (Imagen imagen : imagenes) {
            this.imagenes.add(imagen.getImagen());
        }
    }

}