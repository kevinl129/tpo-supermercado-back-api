package com.uade.ecommerce.service;

import java.math.BigDecimal;

import com.uade.ecommerce.controller.OrdenController.ItemCompraRequest;
import com.uade.ecommerce.entity.*;
import com.uade.ecommerce.entity.dto.ItemOrdenDTO;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uade.ecommerce.repository.OrdenRepository;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.ProductoRepository;
import com.uade.ecommerce.repository.DetalleOrdenRepository;

import java.util.ArrayList;
import java.util.List;
import com.uade.ecommerce.entity.dto.OrdenResponseDTO;
import com.uade.ecommerce.exception.EstadoInvalidoException;
import com.uade.ecommerce.exception.NoEncontradoException;
import com.uade.ecommerce.exception.StockInsuficienteException;


@Service

public class OrdenServiceImpl implements OrdenService {
    @Autowired
    private OrdenRepository ordenRepository;
    
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private DireccionService direccionService;

    @Transactional
    public Orden crearOrden(Integer usuarioId, Integer direccionId, List<ItemCompraRequest> items, BigDecimal descuento) { 

        // 1. Obtener Usuario y Dirección 
        Usuario usuario = usuarioService.getUsuarioById(usuarioId)
                .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado"));

        Direccion direccionEnvio = null;
        if (direccionId != null) {
             direccionEnvio = direccionService.getDireccionById(direccionId)
                    .orElseThrow(() -> new NoEncontradoException("Dirección de envío no encontrada"));
        }
        
        // Si no hay items, no se puede crear la orden
        if (items == null || items.isEmpty()) {
             throw new EstadoInvalidoException("La orden debe contener al menos un producto.");
        }


        // 2. Verificar Stock, Precios y calcular el Total
        BigDecimal totalCompra = BigDecimal.ZERO;
        List<DetalleOrden> detalles = new ArrayList<>();

        for (ItemCompraRequest itemRequest : items) {
            Producto producto = productoRepository.findById(itemRequest.getProductoId())
                    .orElseThrow(() -> new NoEncontradoException("Producto con ID " + itemRequest.getProductoId() + " no encontrado."));

            // Validaciones
            if (!"activo".equalsIgnoreCase(producto.getEstado())) {
                throw new EstadoInvalidoException("El producto con ID: " + producto.getId() + " está desactivado.");
            }
            if (producto.getStock() - producto.getStock_minimo() < itemRequest.getCantidad()) {
                throw new StockInsuficienteException(
                        "No hay suficiente stock para el producto: " + producto.getNombre());
            }

            // Cálculo del subtotal
            BigDecimal cantidad = new BigDecimal(itemRequest.getCantidad());
             BigDecimal precioUnitario = producto.getPrecio(); 
            BigDecimal subtotal = precioUnitario.multiply(cantidad);

            totalCompra = totalCompra.add(subtotal);

            // Preparamos el detalle de la orden
            DetalleOrden detalle = new DetalleOrden(
                itemRequest.getCantidad(), 
                precioUnitario, 
                subtotal, 
                null, 
                producto
            );
            detalles.add(detalle);
        }

        BigDecimal totalFinalConDescuento = totalCompra.subtract(descuento); 
        
        // 3. Crear la orden
        Orden orden = new Orden(
            usuario, 
            totalFinalConDescuento, 
            LocalDateTime.now(), 
            "FINALIZADA", 
            direccionEnvio,
            descuento
        );

        // 4. Guardar la orden
        ordenRepository.save(orden);

        // 5. Crear los detalles de la orden y actualizar el stock
        for (DetalleOrden detalle : detalles) {
            detalle.setOrden(orden); 
            detalleOrdenRepository.save(detalle);
            orden.getItemsOrden().add(detalle); 

            // Actualizar el stock del producto
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        // 6.vaciar carrito
        try {
            Carrito carrito = carritoRepository.findByUsuarioIdAndEstado(usuarioId, EstadoCarrito.ACTIVO)
                    .orElse(null);

            if (carrito != null) {
                carrito.getItemsCarrito().clear();
                carrito.setEstado(EstadoCarrito.VACIO);
                carrito.setFechaActivacion(null);
                carritoRepository.save(carrito);
            } else {
                 System.out.println("WARN: No se encontró carrito ACTIVO para usuario ID: " + usuarioId);
            }
        } catch (Exception e) {
             System.err.println("ERROR: No se pudo limpiar carrito para usuario ID: " + usuarioId + " - " + e.getMessage());
        }

        return orden;

    }

    @Override
    public Orden obtenerOrden(int usuarioId, int ordenId) {
        // 1. Validar existencia del usuario para evitar errores
        if (usuarioService.getUsuarioById(usuarioId).isEmpty()) {
            throw new NoEncontradoException("El usuario no existe.");
        }
        // 2. Buscar la orden
        return ordenRepository.buscarOrdenDeUsuario(ordenId, usuarioId)
                .orElseThrow(() -> new NoEncontradoException("No se encontró la orden para este usuario"));

    }

    @Override
    public List<Orden> obtenerOrdenes(int usuarioId) {
        // 1. Validar existencia del usuario
        Usuario usuario = usuarioService.getUsuarioByIdOrThrow(usuarioId);
        // 2. Obtener las ordenes del usuario
        List<Orden> ordenes = ordenRepository.findByUsuario(usuario);
        // 3. Verificar que el usuario tenga ordenes
        if (ordenes.isEmpty()) {
            throw new NoEncontradoException("El usuario no tiene ordenes.");
        }
        // 4. Devolver las ordenes
        return ordenes;
    }

    public OrdenResponseDTO convertirAOrdenResponse(Orden orden) {
        double subtotal = 0.0;
        BigDecimal descuentoTotal = new BigDecimal(0);
        double total = 0.0;

        List<ItemOrdenDTO> items = new java.util.ArrayList<>();
        for (DetalleOrden detalle : orden.getItemsOrden()) {
            double precioSinDescuento = detalle.getProducto().getPrecio().doubleValue();
            double precioUnitario = detalle.getPrecioUnitario().doubleValue();
            double cantidad = detalle.getCantidad();
            double subtotalItem = precioSinDescuento * cantidad;
            double totalItem = precioUnitario * cantidad;
            double descuentoItem = subtotalItem - totalItem;

            subtotal += subtotalItem;
            descuentoTotal = orden.getDescuento() != null ? orden.getDescuento() : new BigDecimal("0.0");
            total += totalItem;

            items.add(new ItemOrdenDTO(
                    detalle.getProducto().getId(),
                    detalle.getProducto().getNombre(),
                    detalle.getCantidad(),
                    precioUnitario,
                    totalItem));
        }

        String direccionStr = orden.getDireccionEnvio() != null
                ? formatearDireccion(orden.getDireccionEnvio())
                : "Retiro en local";
        String fechaFormateada = orden.getFecha() != null
                ? orden.getFecha().toString()
                : "";

        BigDecimal totalRedondeado = BigDecimal.valueOf(redondear(total));
        BigDecimal totalConDescuento = totalRedondeado.subtract(descuentoTotal);

        return new OrdenResponseDTO(
                orden.getId(),
                fechaFormateada,
                orden.getEstado(),
                redondear(subtotal),
                descuentoTotal,
                totalConDescuento.doubleValue(),
                direccionStr,
                items);
    }

    private String formatearDireccion(Direccion direccion) {
        if (direccion == null) {
            return "";
        }
        // Ajusta los campos según la estructura de tu clase Direccion
        return direccion.getCalle() + " " + direccion.getNumero() +
                ", " + direccion.getCiudad() +
                ", " + direccion.getProvincia() +
                ", " + direccion.getCodigoPostal();
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
