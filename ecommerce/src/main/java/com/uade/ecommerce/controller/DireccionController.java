package com.uade.ecommerce.controller;

import com.uade.ecommerce.entity.Direccion;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.service.DireccionService;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.uade.ecommerce.exception.NoEncontradoException;

import java.security.Principal; // Importar la clase Principal
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/direcciones")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    @Autowired
    private UsuarioService usuarioService;

    // Listar direcciones del usuario autenticado
    @GetMapping
    public ResponseEntity<List<Direccion>> getDireccionesUsuario(Principal principal) {
        // Obtener el usuario autenticado directamente desde el Principal
        Usuario usuario = getUsuarioDesdePrincipal(principal);
        List<Direccion> direcciones = direccionService.getDireccionesByUsuario(usuario);
        return ResponseEntity.ok(direcciones);
    }

    // Crear nueva dirección para el usuario autenticado
    @PostMapping
    public ResponseEntity<Direccion> crearDireccion(Principal principal, @RequestBody Direccion direccion) {
        Usuario usuario = getUsuarioDesdePrincipal(principal);
        direccion.setUsuario(usuario);
        Direccion nueva = direccionService.saveDireccion(direccion);
        return ResponseEntity.ok(nueva);
    }

    // Actualizar dirección (solo si pertenece al usuario)
    @PutMapping("/{id}")
    public ResponseEntity<Direccion> actualizarDireccion(Principal principal, @PathVariable int id, @RequestBody Direccion direccion) {
        Usuario usuario = getUsuarioDesdePrincipal(principal);
        Optional<Direccion> dirOpt = direccionService.getDireccionById(id);

        if (!dirOpt.isPresent() || dirOpt.get().getUsuario().getId() != usuario.getId()) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }

        direccion.setId(id);
        direccion.setUsuario(usuario);
        Direccion actualizada = direccionService.saveDireccion(direccion);
        return ResponseEntity.ok(actualizada);
    }

    // Eliminar dirección (solo si pertenece al usuario)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(Principal principal, @PathVariable int id) {
        Usuario usuario = getUsuarioDesdePrincipal(principal);
        Optional<Direccion> dirOpt = direccionService.getDireccionById(id);

        if (!dirOpt.isPresent() || dirOpt.get().getUsuario().getId() != usuario.getId()) {
            return ResponseEntity.status(403).build(); // 403 Forbidden
        }

        direccionService.deleteDireccion(id);
        return ResponseEntity.noContent().build();
    }
    
    // Método auxiliar para obtener el objeto Usuario del Principal
    private Usuario getUsuarioDesdePrincipal(Principal principal) {
        String username = principal.getName();
        return usuarioService.getUsuarioByUsername(username)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));
    }
}