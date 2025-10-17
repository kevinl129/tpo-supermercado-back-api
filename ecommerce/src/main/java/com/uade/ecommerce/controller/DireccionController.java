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

@CrossOrigin(origins = "http://localhost:5174")
@RestController
@RequestMapping("/direcciones")
public class DireccionController {

    @Autowired
    private DireccionService direccionService;

    @Autowired
    private UsuarioService usuarioService;

    // Listar todas las direcciones (solo para testing)
    @GetMapping
    public ResponseEntity<List<Direccion>> getDirecciones() {
        List<Direccion> direcciones = direccionService.getAllDirecciones();
        return ResponseEntity.ok(direcciones);
    }

    // 👇 ESTE ES EL IMPORTANTE: Filtrar por usuario
    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<Direccion>> getDireccionesByUsuarioId(@PathVariable int usuarioId) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(usuarioId);
        
        if (usuario.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        List<Direccion> direcciones = direccionService.getDireccionesByUsuario(usuario.get());
        return ResponseEntity.ok(direcciones);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Direccion> getDireccionById(@PathVariable int id) {
        Optional<Direccion> direccion = direccionService.getDireccionById(id);
        return direccion.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Direccion> crearDireccion(@RequestBody Direccion direccion) {
        Direccion nueva = direccionService.saveDireccion(direccion);
        return ResponseEntity.ok(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Direccion> actualizarDireccion(@PathVariable int id, @RequestBody Direccion direccion) {
        Optional<Direccion> dirOpt = direccionService.getDireccionById(id);

        if (dirOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        direccion.setId(id);
        Direccion actualizada = direccionService.saveDireccion(direccion);
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarDireccion(@PathVariable int id) {
        Optional<Direccion> dirOpt = direccionService.getDireccionById(id);

        if (dirOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        direccionService.deleteDireccion(id);
        return ResponseEntity.noContent().build();
    }
}