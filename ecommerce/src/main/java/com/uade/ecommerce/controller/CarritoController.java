package com.uade.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.uade.ecommerce.entity.Carrito;
import com.uade.ecommerce.entity.Usuario;
//import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import com.uade.ecommerce.exception.NoEncontradoException;
//import com.uade.ecommerce.exception.NoEncontradoException;
import com.uade.ecommerce.service.CarritoService;
import com.uade.ecommerce.service.UsuarioService;

import java.net.URI;
//import java.security.Principal;
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

    // Se modificó el método para crear un carrito para un usuario no autenticado (invitado).
    // Nota: Necesitas adaptar tu CarritoService para manejar un usuario nulo o un ID de sesión.
    @GetMapping
    public ResponseEntity<List<CarritoResponse>> getAllCarritos() {
        // En una app real, esto podría devolver una lista paginada de carritos
        // para un ADMIN, por ejemplo.
        List<Carrito> carritos = carritoService.findAllCarritos();
        List<CarritoResponse> response = carritos.stream()
            .map(carritoService::convertirACarritoResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/usuario/{userId}")
    public ResponseEntity<CarritoResponse> crearCarrito(@PathVariable int userId) {
        Usuario usuario = usuarioService.getUsuarioById(userId)
        .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado con ID: " + userId));
        Carrito nuevoCarrito = carritoService.crearCarrito(usuario);
        return ResponseEntity
                .created(URI.create("/carritos/" + nuevoCarrito.getId()))
                .body(carritoService.convertirACarritoResponse(nuevoCarrito));
    }

    // Se modificó el método para obtener un carrito por su ID, sin requerir autenticación.
    @GetMapping("/{carritoId}")
    public ResponseEntity<CarritoResponse> obtenerCarrito(@PathVariable int carritoId) {
        // Necesitas un nuevo método en CarritoService que acepte un ID de carrito
        Carrito carrito = carritoService.obtenerCarritoPorId(carritoId);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carrito));
    }

    // Se modificó el método para agregar un producto a un carrito por su ID.
    @PatchMapping("/{carritoId}/producto/{productoId}")
    public ResponseEntity<CarritoResponse> agregarProducto(
            @PathVariable int carritoId,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        Carrito carritoActualizado = carritoService.agregarProductoPorId(carritoId, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    // Se modificó el método para eliminar un producto de un carrito por su ID.
    @DeleteMapping("/{carritoId}/producto/{productoId}")
    public ResponseEntity<CarritoResponse> eliminarProducto(
            @PathVariable int carritoId,
            @PathVariable int productoId,
            @RequestParam(defaultValue = "1") int cantidad) {
        Carrito carritoActualizado = carritoService.eliminarProductoPorId(carritoId, productoId, cantidad);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoActualizado));
    }

    // Se modificó el método para vaciar un carrito por su ID.
    @DeleteMapping("/{carritoId}")
    public ResponseEntity<CarritoResponse> vaciarCarrito(@PathVariable int carritoId) {
        Carrito carritoVaciado = carritoService.vaciarCarritoPorId(carritoId);
        return ResponseEntity.ok(carritoService.convertirACarritoResponse(carritoVaciado));
    }
}