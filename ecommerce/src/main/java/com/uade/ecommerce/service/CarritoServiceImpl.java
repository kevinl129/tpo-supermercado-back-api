package com.uade.ecommerce.service;

import com.uade.ecommerce.entity.*;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import com.uade.ecommerce.entity.dto.ItemCarritoDTO;
import com.uade.ecommerce.exception.EstadoInvalidoException;
import com.uade.ecommerce.exception.NoEncontradoException;
import com.uade.ecommerce.exception.ParametroFueraDeRangoException;
import com.uade.ecommerce.exception.StockInsuficienteException;
import com.uade.ecommerce.repository.CarritoRepository;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private UsuarioRepository usuarioRepository; // Necesario para crear el carrito si no existe

    // --- NUEVOS MÉTODOS REQUERIDOS POR EL CONTROLADOR ---

    @Override
    @Transactional
    public Carrito obtenerOCrearCarritoPorUsuarioId(Long usuarioId) {
        // Buscamos primero un carrito ACTIVO o VACIO usando tu método de repositorio
        return carritoRepository.findByUsuarioIdAndEstadoConItems(usuarioId.intValue(), EstadoCarrito.ACTIVO)
                .or(() -> carritoRepository.findByUsuarioIdAndEstadoConItems(usuarioId.intValue(), EstadoCarrito.VACIO))
                .orElseGet(() -> {
                    // Si no existe ninguno, lo creamos
                    Usuario usuario = usuarioRepository.findById(usuarioId.intValue())
                            .orElseThrow(() -> new NoEncontradoException("Usuario no encontrado con ID: " + usuarioId));
                    
                    Carrito nuevoCarrito = Carrito.builder()
                        .usuario(usuario)
                        .estado(EstadoCarrito.VACIO)
                        .fechaCreacion(LocalDateTime.now())
                        .build();
                    
                    return carritoRepository.save(nuevoCarrito);
                });
    }

    @Override
    @Transactional
    public Carrito agregarProductoAlCarrito(Long usuarioId, Long productoId, int cantidad) {
        // 1. Obtenemos el carrito del usuario (o lo creamos si es necesario)
        Carrito carrito = obtenerOCrearCarritoPorUsuarioId(usuarioId);

        // 2. REUTILIZAMOS TU LÓGICA DE NEGOCIO ANTERIOR
        Producto producto = productoService.getProductoById(productoId.intValue())
                .orElseThrow(() -> new NoEncontradoException("Producto no encontrado con ID: " + productoId));

        if (!"activo".equalsIgnoreCase(producto.getEstado())) {
            throw new EstadoInvalidoException("El producto con ID: " + producto.getId() + " está desactivado.");
        }

        if (producto.getStock() - producto.getStock_minimo() < cantidad) {
            throw new StockInsuficienteException("No hay suficiente stock para el producto con ID: " + productoId);
        }

        Optional<ItemCarrito> itemExistente = carrito.getItemsCarrito().stream()
                .filter(item -> item.getProducto().getId() == productoId)
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito item = itemExistente.get();
            int nuevaCantidad = item.getCantidad() + cantidad;
            if (nuevaCantidad > producto.getStock()) {
                throw new StockInsuficienteException("No se puede agregar más productos que el stock disponible.");
            }
            item.setCantidad(nuevaCantidad);
        } else {
            if (cantidad <= 0) {
                throw new IllegalArgumentException("No se puede agregar un producto con cantidad cero o negativa.");
            }
            BigDecimal precioConDescuento = producto.getPrecio();
            if (producto.getDescuento() != null && producto.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal descuento = producto.getDescuento().divide(new BigDecimal(100));
                precioConDescuento = producto.getPrecio().multiply(BigDecimal.ONE.subtract(descuento));
            }
            ItemCarrito nuevoItem = new ItemCarrito(cantidad, precioConDescuento, carrito, producto);
            carrito.getItemsCarrito().add(nuevoItem);
        }

        if (carrito.getItemsCarrito().isEmpty()) {
            carrito.setEstado(EstadoCarrito.VACIO);
        } else if (carrito.getEstado() == EstadoCarrito.VACIO) {
            carrito.setEstado(EstadoCarrito.ACTIVO);
            carrito.setFechaActivacion(LocalDateTime.now());
        }

        return carritoRepository.save(carrito);
    }

    @Override
    @Transactional
    public Carrito eliminarProductoDelCarrito(Long usuarioId, Long productoId, int cantidad) {
        // 1. Obtenemos el carrito del usuario
        Carrito carrito = obtenerOCrearCarritoPorUsuarioId(usuarioId);
        if (carrito.getEstado() != EstadoCarrito.ACTIVO) {
            throw new EstadoInvalidoException("El carrito debe estar activo para eliminar productos.");
        }
        
        // 2. REUTILIZAMOS TU LÓGICA DE NEGOCIO ANTERIOR
        ItemCarrito item = carrito.getItemsCarrito().stream()
                .filter(i -> i.getProducto().getId() == productoId)
                .findFirst()
                .orElseThrow(() -> new NoEncontradoException("El producto no está en el carrito"));
        
        if (cantidad <= 0) {
            throw new ParametroFueraDeRangoException("La cantidad debe ser mayor a cero.");
        }

        if (item.getCantidad() > cantidad) {
            item.setCantidad(item.getCantidad() - cantidad);
        } else {
            carrito.getItemsCarrito().remove(item);
        }
        
        if (carrito.getItemsCarrito().isEmpty()) {
            carrito.setEstado(EstadoCarrito.VACIO);
            carrito.setFechaActivacion(null);
        }

        return carritoRepository.save(carrito);
    }

    // --- MÉTODOS DE UTILIDAD QUE MANTENEMOS ---

    @Override
    public CarritoResponse convertirACarritoResponse(Carrito carrito) {
        List<ItemCarritoDTO> items = carrito.getItemsCarrito().stream()
                .map(item -> new ItemCarritoDTO(
                        (long) item.getProducto().getId(), // Cast a Long para DTO
                        item.getProducto().getNombre(),
                        item.getCantidad(),
                        item.getPrecio_unitario().doubleValue(),
                        item.getPrecio_unitario().doubleValue() * item.getCantidad()))
                .collect(Collectors.toList());

        double total = items.stream()
                .mapToDouble(ItemCarritoDTO::getSubtotal)
                .sum();

        return new CarritoResponse(
                carrito.getId(),
                carrito.getEstado().toString(),
                items,
                total);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    @Override
    public void vaciarCarritosAntiguos() {
        LocalDateTime seisHorasAtras = LocalDateTime.now().minusHours(6);
        List<Carrito> carritosAntiguos = carritoRepository.findByEstadoAndFechaActivacionBefore(EstadoCarrito.ACTIVO, seisHorasAtras);
        for (Carrito carrito : carritosAntiguos) {
            carrito.setEstado(EstadoCarrito.VACIO);
            carrito.getItemsCarrito().clear();
            carritoRepository.save(carrito);
        }
    }

    @Override
    public List<Carrito> findAllCarritos() {
        return carritoRepository.findAll();
    }
}