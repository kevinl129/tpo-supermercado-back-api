package com.uade.ecommerce.entity.dto;

import java.math.BigDecimal;
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
    private BigDecimal descuentoTotal;
    private double total;
    private String direccion; // o null si es retiro en tienda
    private List<ItemOrdenDTO> items;


}