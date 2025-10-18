package com.uade.ecommerce.service;

import java.math.BigDecimal;
import java.util.List;

import com.uade.ecommerce.controller.OrdenController.ItemCompraRequest;
import com.uade.ecommerce.entity.*;
import com.uade.ecommerce.entity.dto.OrdenResponseDTO;

public interface OrdenService {
    // Método para finalizar la compra y crear una orden

    public Orden crearOrden(Integer usuarioId, Integer direccionId, List<ItemCompraRequest> items, BigDecimal descuento);

    Orden obtenerOrden(int usuarioId, int ordenId); // Método para obtener una orden específica de un usuario

    List<Orden> obtenerOrdenes(int usuarioId); // Método para obtener todas las órdenes de un usuario

    public OrdenResponseDTO convertirAOrdenResponse(Orden orden); // Método para convertir una orden a su representación

}
