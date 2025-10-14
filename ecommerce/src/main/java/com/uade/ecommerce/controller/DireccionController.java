package com.uade.ecommerce.controller;

import com.uade.ecommerce.entity.Direccion;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.service.DireccionService;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<List<Direccion>> getDireccionesUsuario() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByUsername(username);
        if (!usuarioOpt.isPresent()) return ResponseEntity.status(401).build();
        List<Direccion> direcciones = direccionService.getDireccionesByUsuario(usuarioOpt.get());
        return ResponseEntity.ok(direcciones);
    }

    // Crear nueva dirección para el usuario autenticado
    @PostMapping
    public ResponseEntity<Direccion> crearDireccion(@RequestBody Direccion direccion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByUsername(username);
        if (!usuarioOpt.isPresent()) return ResponseEntity.status(401).build();
        direccion.setUsuario(usuarioOpt.get());
        Direccion nueva = direccionService.saveDireccion(direccion);
        return ResponseEntity.ok(nueva);
    }

    // Actualizar dirección (solo si pertenece al usuario)
    @PutMapping("/{id}")
    public ResponseEntity<Direccion> actualizarDireccion(@PathVariable int id, @RequestBody Direccion direccion) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByUsername(username);
        if (!usuarioOpt.isPresent()) return ResponseEntity.status(401).build();
        Optional<Direccion> dirOpt = direccionService.getDireccionById(id);
        if (!dirOpt.isPresent() || dirOpt.get().getUsuario().getId() != usuarioOpt.get().getId())
            return ResponseEntity.status(403).build();
        direccion.setId(id);
        direccion.setUsuario(usuarioOpt.get());
        Direccion actualizada = direccionService.saveDireccion(direccion);
        return ResponseEntity.ok(actualizada);
    }

    // Eliminar dirección (solo si pertenece al usuario)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable int id) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByUsername(username);
        if (!usuarioOpt.isPresent()) return ResponseEntity.status(401).build();
        Optional<Direccion> dirOpt = direccionService.getDireccionById(id);
        if (!dirOpt.isPresent() || dirOpt.get().getUsuario().getId() != usuarioOpt.get().getId())
            return ResponseEntity.status(403).build();
        direccionService.deleteDireccion(id);
        return ResponseEntity.noContent().build();
    }
}
