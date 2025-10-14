package com.uade.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.ecommerce.entity.Carrito;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import com.uade.ecommerce.exception.NoEncontradoException;
import com.uade.ecommerce.service.CarritoService;
import com.uade.ecommerce.service.UsuarioService;

import java.net.URI;
import java.security.Principal;

@RestController
@RequestMapping("carritos")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;
    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<CarritoResponse> crearCarrito(Principal principal) {
        Usuario usuario = getUsuarioIdDesdePrincipal(principal);
        Carrito nuevoCarrito = carritoService.crearCarrito(usuario);
        return ResponseEntity
                .created(URI.create("/carritos/" + nuevoCarrito.getId()))
                .body(carritoService.convertirACarritoResponse(nuevoCarrito));
    }

    @GetMapping
    public ResponseEntity<CarritoResponse> obtenerCarrito(Principal principal) {
        Usuario usuario = getUsuarioIdDesdePrincipal(principal);
        Carrito carrito = carritoService.obtenerCarrito(usuario);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carrito));
    }

    @PatchMapping("/{productoId}")
    public ResponseEntity<CarritoResponse> agregarProducto(
            Principal principal,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        Usuario usuario = getUsuarioIdDesdePrincipal(principal);
        Carrito carritoActualizado = carritoService.agregarProducto(usuario, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    @DeleteMapping("/{productoId}")
    public ResponseEntity<CarritoResponse> eliminarProducto(
            Principal principal,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        Usuario usuario = getUsuarioIdDesdePrincipal(principal);
        Carrito carritoActualizado = carritoService.eliminarProducto(usuario, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    @DeleteMapping
    public ResponseEntity<CarritoResponse> vaciarCarrito(Principal principal) {
        Usuario usuario = getUsuarioIdDesdePrincipal(principal);
        Carrito carritoVaciado = carritoService.vaciarCarrito(usuario);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoVaciado));
    }

    private Usuario getUsuarioIdDesdePrincipal(Principal principal) {
        String username = principal.getName();
        return usuarioService.getUsuarioByUsername(username)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
    }
}