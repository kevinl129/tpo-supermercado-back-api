package com.uade.ecommerce.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uade.ecommerce.entity.Orden;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.entity.dto.OrdenResponseDTO;
import com.uade.ecommerce.exception.NoEncontradoException;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import com.uade.ecommerce.service.OrdenService;
import com.uade.ecommerce.service.UsuarioService;

import lombok.Data;

@RestController
@RequestMapping("ordenes")
@CrossOrigin(origins = "http://localhost:5174")
public class OrdenController {
    @Autowired
    private OrdenService ordenService;

    @Autowired
    private UsuarioService usuarioService;

    // DTO para finalizar compra
    public static class FinalizarCompraRequest {
        public Integer usuarioId; // Se añadió un ID de usuario al request
        public Integer direccionId; // null para retiro en tienda
        public List<ItemCompraRequest> items;
    }

    @Data
    public static class ItemCompraRequest {
        public Integer productoId;
        public Integer cantidad;
        public BigDecimal precioUnitario; // El precio final ya calculado por el front
    }

    // POST para finalizar compra
    @PostMapping
    public ResponseEntity<OrdenResponseDTO> finalizarCompra(
            @RequestBody FinalizarCompraRequest request) {
        
        // El servicio manejará ahora la lógica completa
        Orden orden = ordenService.crearOrden(
            request.usuarioId, 
            request.direccionId, 
            request.items // Pasamos la lista de ítems
        ); 
        
        OrdenResponseDTO dto = ordenService.convertirAOrdenResponse(orden);
        return ResponseEntity.ok(dto);
    }

    // GET una orden por id
    @GetMapping("/{ordenId}/usuarios/{id}")
    public ResponseEntity<OrdenResponseDTO> obtenerOrden(@PathVariable int id, @PathVariable int ordenId) {
        Orden orden = ordenService.obtenerOrden(id, ordenId);
        OrdenResponseDTO dto = ordenService.convertirAOrdenResponse(orden);
        return ResponseEntity.ok(dto);
    }

    // GET historial de ordenes por usuario
    @GetMapping("/usuarios/{id}")
    public ResponseEntity<List<OrdenResponseDTO>> obtenerOrdenes(@PathVariable int id) {
        List<Orden> ordenes = ordenService.obtenerOrdenes(id);
        List<OrdenResponseDTO> dtos = ordenes.stream()
                .map(ordenService::convertirAOrdenResponse)
                .toList();
        return ResponseEntity.ok(dtos);
    }
}