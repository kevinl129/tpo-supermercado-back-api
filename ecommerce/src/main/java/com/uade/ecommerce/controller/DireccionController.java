package com.uade.ecommerce.controller;

import com.uade.ecommerce.entity.Direccion;
import com.uade.ecommerce.service.DireccionService;
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

    // Listar todas las direcciones
    @GetMapping
    public ResponseEntity<List<Direccion>> getDirecciones() {
        List<Direccion> direcciones = direccionService.getAllDirecciones();
        return ResponseEntity.ok(direcciones);
    }

    // Obtener dirección por ID
    @GetMapping("/{id}")
    public ResponseEntity<Direccion> getDireccionById(@PathVariable int id) {
        Optional<Direccion> direccion = direccionService.getDireccionById(id);
        return direccion.map(ResponseEntity::ok)
                       .orElse(ResponseEntity.notFound().build());
        
    }

    // Crear nueva dirección
    @PostMapping
    public ResponseEntity<Direccion> crearDireccion(@RequestBody Direccion direccion) {
        Direccion nueva = direccionService.saveDireccion(direccion);
        return ResponseEntity.ok(nueva);
    }

    // Actualizar dirección
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

    // Eliminar dirección
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