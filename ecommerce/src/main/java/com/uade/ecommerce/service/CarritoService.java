package com.uade.ecommerce.service;

import com.uade.ecommerce.entity.Carrito;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import java.util.List;

public interface CarritoService {

    // Nuevos métodos que serán llamados por el controlador
    Carrito obtenerOCrearCarritoPorUsuarioId(Long usuarioId);
    Carrito agregarProductoAlCarrito(Long usuarioId, Long productoId, int cantidad);
    Carrito eliminarProductoDelCarrito(Long usuarioId, Long productoId, int cantidad);

    // Métodos de utilidad que sigues necesitando
    CarritoResponse convertirACarritoResponse(Carrito carrito);
    void vaciarCarritosAntiguos();
    List<Carrito> findAllCarritos();
}