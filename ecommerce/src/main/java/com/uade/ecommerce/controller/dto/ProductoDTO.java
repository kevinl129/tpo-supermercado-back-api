package com.uade.ecommerce.controller.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import com.uade.ecommerce.entity.Imagen; 
import com.uade.ecommerce.entity.Producto;
import lombok.Data;

@Data
public class ProductoDTO {
    
    private Integer id; 
    private String nombre;
    private String descripcion;

    private List<Imagen> imagenes = new ArrayList<>(); 
    
    private BigDecimal precio;
    private String marca;
    private String categoria;
    private Integer stock;
    private BigDecimal descuento;

    public ProductoDTO(Producto producto) {
        this.id = producto.getId(); 
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        
        if (producto.getImagenes() != null) {
            this.imagenes = producto.getImagenes();
        }
        
        this.precio = producto.getPrecio();
        this.marca = producto.getMarca();
        this.categoria = producto.getCategoria().getNombre();
        this.stock = producto.getStock();
        this.descuento = producto.getDescuento();
    }
}