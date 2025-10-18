package com.uade.ecommerce.service;

import com.uade.ecommerce.entity.Carrito;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import java.util.List;

public interface CarritoService {
    Carrito crearCarrito(Usuario usuario);
    Carrito obtenerCarrito(Usuario usuario);
    Carrito agregarProducto(Usuario usuario, int productoId, int cantidad);
    Carrito eliminarProducto(Usuario usuario, int productoId, int cantidad);
    Carrito vaciarCarrito(Usuario usuario);
    CarritoResponse convertirACarritoResponse(Carrito carrito);
    public void vaciarCarritosAntiguos();

    // Nuevos métodos para manejar el carrito por su ID
    Carrito obtenerCarritoPorId(int carritoId);
    Carrito agregarProductoPorId(int carritoId, int productoId, int cantidad);
    Carrito eliminarProductoPorId(int carritoId, int productoId, int cantidad);
    Carrito vaciarCarritoPorId(int carritoId);
    
    List<Carrito> findAllCarritos();
}