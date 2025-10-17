package com.uade.ecommerce.controller;

import com.uade.ecommerce.entity.Carrito;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import com.uade.ecommerce.service.CarritoService;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioService usuarioService;

    /**
     * Endpoint para OBTENER el carrito del usuario autenticado.
     * URL: GET /carritos/mi-carrito
     */
    @GetMapping("/mi-carrito")
    public ResponseEntity<CarritoResponse> getMiCarrito(Authentication authentication) {
        String userEmail = authentication.getName();
        Usuario usuario = usuarioService.getUsuarioByEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con email: " + userEmail));

        Carrito carrito = carritoService.obtenerOCrearCarritoPorUsuarioId((long) usuario.getId());
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carrito));
    }

    /**
     * Endpoint para AGREGAR o SUMAR cantidad de un producto al carrito del usuario autenticado.
     * URL: PATCH /carritos/mi-carrito/producto/{productoId}?cantidad=1
     */
    @PatchMapping("/mi-carrito/producto/{productoId}")
    public ResponseEntity<CarritoResponse> agregarProductoAMiCarrito(
            Authentication authentication,
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "1") int cantidad) {

        String userEmail = authentication.getName();
        Usuario usuario = usuarioService.getUsuarioByEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con email: " + userEmail));

        Carrito carritoActualizado = carritoService.agregarProductoAlCarrito((long) usuario.getId(), productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    /**
     * Endpoint para QUITAR o RESTAR cantidad de un producto del carrito del usuario autenticado.
     * URL: DELETE /carritos/mi-carrito/producto/{productoId}?cantidad=1
     */
    @DeleteMapping("/mi-carrito/producto/{productoId}")
    public ResponseEntity<CarritoResponse> eliminarProductoDeMiCarrito(
            Authentication authentication,
            @PathVariable Long productoId,
            @RequestParam(defaultValue = "1") int cantidad) {

        String userEmail = authentication.getName();
        Usuario usuario = usuarioService.getUsuarioByEmail(userEmail)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado con email: " + userEmail));

        Carrito carritoActualizado = carritoService.eliminarProductoDelCarrito((long) usuario.getId(), productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }
}