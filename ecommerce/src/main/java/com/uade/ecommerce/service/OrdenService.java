package com.uade.ecommerce.service;

import java.security.Principal;
import java.util.List;
import com.uade.ecommerce.entity.*;
import com.uade.ecommerce.entity.dto.OrdenResponseDTO;

public interface OrdenService {
    Orden finalizarCompra(Usuario usuario, Integer direccionId); // Método para finalizar la compra y crear una orden

    Orden obtenerOrden(int usuarioId, int ordenId); // Método para obtener una orden específica de un usuario

    List<Orden> obtenerOrdenes(int usuarioId); // Método para obtener todas las órdenes de un usuario

    public OrdenResponseDTO convertirAOrdenResponse(Orden orden); // Método para convertir una orden a su representación
                                                                  // DTO (OrdenResponse)

}
