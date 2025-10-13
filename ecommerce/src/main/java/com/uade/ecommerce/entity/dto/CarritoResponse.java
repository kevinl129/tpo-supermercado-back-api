package com.uade.ecommerce.entity.dto;

import java.util.List;

import lombok.Data;

@Data
public class CarritoResponse {
        private int id;
        private String estado;
        private List<ItemCarritoDTO> items;
        private double total;

        public CarritoResponse(int id, String estado, List<ItemCarritoDTO> items, double total) {
                this.id = id;
                this.estado = estado;
                this.items = items;
                this.total = total;
        }

}