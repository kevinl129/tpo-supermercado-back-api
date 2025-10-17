package com.uade.ecommerce.controller.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.uade.ecommerce.entity.Imagen;
import com.uade.ecommerce.entity.Producto;
import lombok.Data;

@Data
public class ProductoDTO {
    
    // CAMBIO CLAVE: Usa Integer en lugar de int para permitir el valor 'null'.
    private Integer id; 
    
    private String nombre;
    private String descripcion;
    private List<String> imagenes = new ArrayList<>(); 
    private BigDecimal precio;
    private String marca;
    private String categoria;
    private Integer stock;
    private BigDecimal descuento;

    public ProductoDTO(Producto producto) {
        // En el constructor, puedes seguir usando getID() si es int, y se auto-envuelve.
        this.id = producto.getId(); 
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.imagenes = new ArrayList<>();
        cargarImagenes(producto);
        this.precio = producto.getPrecio();
        this.marca = producto.getMarca();
        this.categoria = producto.getCategoria().getNombre();
        this.stock = producto.getStock();
        this.descuento = producto.getDescuento();
    }

    private void cargarImagenes(Producto producto) {
        if (producto.getImagenes() != null) {
            for (Imagen imagen : producto.getImagenes()) {
                this.imagenes.add(imagen.getImagen());
            }
        }
    }
}