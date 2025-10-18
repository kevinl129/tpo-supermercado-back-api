package com.uade.ecommerce.controller;

import com.uade.ecommerce.entity.Carrito;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import com.uade.ecommerce.exception.NoEncontradoException;
import com.uade.ecommerce.service.CarritoService;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("carritos")
@CrossOrigin(origins = "http://localhost:5174")
public class CarritoController {

    @Autowired
    private CarritoService carritoService;

    @Autowired
    private UsuarioService usuarioService;

    // --- Endpoints usando ID de Usuario 

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<CarritoResponse> getCarritoPorUsuario(@PathVariable int usuarioId) {
        // 1. Obtener el usuario por ID
        Usuario usuario = usuarioService.getUsuarioById(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

        // 2. Obtener/Crear carrito por objeto Usuario
        Carrito carrito = carritoService.obtenerCarrito(usuario);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carrito));
    }

    @PatchMapping("/usuario/{usuarioId}/producto/{productoId}")
    public ResponseEntity<CarritoResponse> agregarProductoPorUsuario(
            @PathVariable int usuarioId,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        
        // 1. Obtener el usuario por ID
        Usuario usuario = usuarioService.getUsuarioById(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado con ID: " + usuarioId));
        
        // 2. Agregar producto usando el objeto Usuario
        Carrito carritoActualizado = carritoService.agregarProducto(usuario, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    @DeleteMapping("/usuario/{usuarioId}/producto/{productoId}")
    public ResponseEntity<CarritoResponse> eliminarProductoPorUsuario(
            @PathVariable int usuarioId,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {

        Usuario usuario = usuarioService.getUsuarioById(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

        Carrito carritoActualizado = carritoService.eliminarProducto(usuario, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    @DeleteMapping("/usuario/{usuarioId}/vaciar")
    public ResponseEntity<CarritoResponse> vaciarCarritoPorUsuario(@PathVariable int usuarioId) {
        // 1. Obtener el usuario por ID
        Usuario usuario = usuarioService.getUsuarioById(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado con ID: " + usuarioId));

        // 2. Vaciar carrito usando el objeto Usuario
        Carrito carritoActualizado = carritoService.vaciarCarrito(usuario);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    // --- Endpoints usando ID de Carrito (Existentes) ---

    @GetMapping("/{carritoId}")
    public ResponseEntity<CarritoResponse> obtenerCarritoPorId(@PathVariable int carritoId) {
        Carrito carrito = carritoService.obtenerCarritoPorId(carritoId);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carrito));
    }

    @PatchMapping("/{carritoId}/producto/{productoId}")
    public ResponseEntity<CarritoResponse> agregarProductoPorCarritoId(
            @PathVariable int carritoId,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        Carrito carritoActualizado = carritoService.agregarProductoPorId(carritoId, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    @DeleteMapping("/{carritoId}/eliminar/producto/{productoId}")
    public ResponseEntity<CarritoResponse> eliminarProductoPorCarritoId(
            @PathVariable int carritoId,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        Carrito carritoActualizado = carritoService.eliminarProductoPorId(carritoId, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    @DeleteMapping("/{carritoId}/vaciar")
    public ResponseEntity<CarritoResponse> vaciarCarritoPorId(@PathVariable int carritoId) {
        Carrito carritoActualizado = carritoService.vaciarCarritoPorId(carritoId);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    // --- Endpoints de Administración ---
    
    @GetMapping
    public ResponseEntity<List<CarritoResponse>> getAllCarritos() {
        List<CarritoResponse> carritos = carritoService.findAllCarritos().stream()
                .map(carritoService::convertirACarritoResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(carritos);
    }
}