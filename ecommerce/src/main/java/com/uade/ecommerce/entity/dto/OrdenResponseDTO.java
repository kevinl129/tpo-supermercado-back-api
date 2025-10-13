package com.uade.ecommerce.entity.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class OrdenResponseDTO {
    private int ordenId;
    private String fechaCreacion;
    private String estado;
    private double subtotal;
    private double descuentoTotal;
    private double total;
    private String direccion; // o null si es retiro en tienda
    private List<ItemOrdenDTO> items;

    public OrdenResponseDTO(int ordenId, LocalDateTime fechaCreacion, String estado, double total, double subtotal,
            double descuentoTotal, String direccion, List<ItemOrdenDTO> items) {
        this.ordenId = ordenId;
        this.fechaCreacion = fechaCreacion.toString();
        this.estado = estado;
        this.total = total;
        this.subtotal = subtotal;
        this.descuentoTotal = descuentoTotal;
        this.direccion = direccion;
        this.items = items;
    }

}