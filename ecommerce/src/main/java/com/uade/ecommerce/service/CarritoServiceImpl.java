package com.uade.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uade.ecommerce.entity.*;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import com.uade.ecommerce.entity.dto.ItemCarritoDTO;
import com.uade.ecommerce.exception.DatoDuplicadoException;
import com.uade.ecommerce.exception.EstadoInvalidoException;
import com.uade.ecommerce.exception.NoEncontradoException;
import com.uade.ecommerce.exception.ParametroFueraDeRangoException;
import com.uade.ecommerce.exception.StockInsuficienteException;
import com.uade.ecommerce.repository.CarritoRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class CarritoServiceImpl implements CarritoService {

    @Autowired
    private CarritoRepository carritoRepository;

    @Autowired
    private ProductoService productoService;

    @Override
    public CarritoResponse convertirACarritoResponse(Carrito carrito) {
        List<ItemCarritoDTO> items = carrito.getItemsCarrito().stream()
                .map(item -> new ItemCarritoDTO(
                        item.getProducto().getId(),
                        item.getProducto().getNombre(),
                        item.getCantidad(),
                        item.getPrecio_unitario().doubleValue(), // Corregido: getPrecioUnitario
                        item.getPrecio_unitario().doubleValue() * item.getCantidad())) // Corregido: getPrecioUnitario
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

    @Override
    public Carrito crearCarrito(Usuario usuario) {
        boolean yaTieneCarrito = false;
        if (usuario != null) {
            yaTieneCarrito = carritoRepository
                    .findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO)
                    .isPresent()
                    || carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.VACIO).isPresent();
        }

        if (yaTieneCarrito) {
            throw new DatoDuplicadoException("El usuario ya tiene un carrito.");
        }

        Carrito nuevoCarrito = Carrito.builder() // Usando el patrón Builder
            .usuario(usuario)
            .estado(EstadoCarrito.VACIO)
            .fechaCreacion(LocalDateTime.now())
            .build();
        
        carritoRepository.save(nuevoCarrito);
        return nuevoCarrito;
    }

    @Override
    public Carrito obtenerCarrito(Usuario usuario) {
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuario(usuario);
        if (carritoExistente.isEmpty()) {
            return crearCarrito(usuario);
        }

        Carrito carrito = carritoExistente.get();
        if (carrito.getItemsCarrito().isEmpty()) {
            carrito.setEstado(EstadoCarrito.VACIO);
        } else {
            carrito.setEstado(EstadoCarrito.ACTIVO);
        }
        return carrito;
    }

    @Override
    @Transactional
    public Carrito agregarProducto(Usuario usuario, int productoId, int cantidad) {
        Carrito carrito = carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.VACIO)
                .or(() -> carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO))
                .orElseGet(() -> crearCarrito(usuario));

        Producto producto = productoService.getProductoById(productoId)
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
            if (nuevaCantidad < 0) {
                throw new IllegalArgumentException("La cantidad no puede ser menor a cero.");
            }
            if (nuevaCantidad == 0) {
                carrito.getItemsCarrito().remove(item);
            } else {
                if (nuevaCantidad > producto.getStock()) {
                    throw new StockInsuficienteException("No se puede agregar más productos que el stock disponible.");
                }
                item.setCantidad(nuevaCantidad);
            }
        } else {
            if (cantidad <= 0) {
                throw new IllegalArgumentException("No se puede agregar un producto con cantidad cero o negativa.");
            }
            BigDecimal precioConDescuento = producto.getPrecio();
            if (producto.getDescuento() != null && producto.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal descuento = producto.getDescuento().divide(new BigDecimal(100));
                precioConDescuento = producto.getPrecio().multiply(BigDecimal.ONE.subtract(descuento));
            } else {
                precioConDescuento = producto.getPrecio();
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
    public Carrito eliminarProducto(Usuario usuario, int productoId, int cantidad) {
        Carrito carrito = carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new EstadoInvalidoException("El carrito esta vacio"));

        Optional<ItemCarrito> itemCarrito = carrito.getItemsCarrito().stream()
                .filter(item -> item.getProducto().getId() == productoId)
                .findFirst();

        if (itemCarrito.isEmpty()) {
            throw new NoEncontradoException("El producto no está en el carrito");
        }
        if (cantidad <= 0) {
            throw new ParametroFueraDeRangoException("La cantidad debe ser mayor a cero.");
        }

        ItemCarrito item = itemCarrito.get();
        if (item.getCantidad() > cantidad) {
            item.setCantidad(item.getCantidad() - cantidad);
        } else {
            carrito.getItemsCarrito().remove(item);
        }
        if (carrito.getItemsCarrito().isEmpty()) {
            carrito.setEstado(EstadoCarrito.VACIO);
            carrito.setFechaActivacion(null);
        }

        carritoRepository.save(carrito);

        return carrito;
    }

    @Override
    public Carrito vaciarCarrito(Usuario usuario) {
        Carrito carrito = carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new EstadoInvalidoException("El carrito esta vacio"));

        carrito.getItemsCarrito().clear();
        carrito.setEstado(EstadoCarrito.VACIO);
        carrito.setFechaActivacion(null);
        carritoRepository.save(carrito);
        return carrito;
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    @Override
    public void vaciarCarritosAntiguos() {
        LocalDateTime seisHorasAtras = LocalDateTime.now().minusHours(6);

        List<Carrito> carritosAntiguos = carritoRepository.findByEstadoAndFechaActivacionBefore(EstadoCarrito.ACTIVO,
                seisHorasAtras);

        for (Carrito carrito : carritosAntiguos) {
            carrito.setEstado(EstadoCarrito.VACIO);
            carrito.getItemsCarrito().clear();
            carritoRepository.save(carrito);
        }
    }
    
    // Implementaciones de los nuevos métodos...
    @Override
    public Carrito obtenerCarritoPorId(int carritoId) {
        return carritoRepository.findById(carritoId)
                .orElseThrow(() -> new NoEncontradoException("Carrito no encontrado con ID: " + carritoId));
    }

    @Override
    @Transactional
    public Carrito agregarProductoPorId(int carritoId, int productoId, int cantidad) {
        Carrito carrito = obtenerCarritoPorId(carritoId);

        Producto producto = productoService.getProductoById(productoId)
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
            if (nuevaCantidad < 0) {
                throw new IllegalArgumentException("La cantidad no puede ser menor a cero.");
            }
            if (nuevaCantidad == 0) {
                carrito.getItemsCarrito().remove(item);
            } else {
                if (nuevaCantidad > producto.getStock()) {
                    throw new StockInsuficienteException("No se puede agregar más productos que el stock disponible.");
                }
                item.setCantidad(nuevaCantidad);
            }
        } else {
            if (cantidad <= 0) {
                throw new IllegalArgumentException("No se puede agregar un producto con cantidad cero o negativa.");
            }
            BigDecimal precioConDescuento = producto.getPrecio();
            if (producto.getDescuento() != null && producto.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal descuento = producto.getDescuento().divide(new BigDecimal(100));
                precioConDescuento = producto.getPrecio().multiply(BigDecimal.ONE.subtract(descuento));
            } else {
                precioConDescuento = producto.getPrecio();
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
    public Carrito eliminarProductoPorId(int carritoId, int productoId, int cantidad) {
        Carrito carrito = obtenerCarritoPorId(carritoId);

        Optional<ItemCarrito> itemCarrito = carrito.getItemsCarrito().stream()
                .filter(item -> item.getProducto().getId() == productoId)
                .findFirst();

        if (itemCarrito.isEmpty()) {
            throw new NoEncontradoException("El producto no está en el carrito");
        }
        if (cantidad <= 0) {
            throw new ParametroFueraDeRangoException("La cantidad debe ser mayor a cero.");
        }

        ItemCarrito item = itemCarrito.get();
        if (item.getCantidad() > cantidad) {
            item.setCantidad(item.getCantidad() - cantidad);
        } else {
            carrito.getItemsCarrito().remove(item);
        }
        if (carrito.getItemsCarrito().isEmpty()) {
            carrito.setEstado(EstadoCarrito.VACIO);
            carrito.setFechaActivacion(null);
        }

        carritoRepository.save(carrito);

        return carrito;
    }

    @Override
    @Transactional
    public Carrito vaciarCarritoPorId(int carritoId) {
        Carrito carrito = obtenerCarritoPorId(carritoId);

        if (carrito.getItemsCarrito().isEmpty()) {
            throw new EstadoInvalidoException("El carrito ya esta vacio.");
        }
        
        carrito.getItemsCarrito().clear();
        carrito.setEstado(EstadoCarrito.VACIO);
        carrito.setFechaActivacion(null);
        
        return carritoRepository.save(carrito);
    }
    
    // Implementación del nuevo método
    @Override
    public List<Carrito> findAllCarritos() {
        return carritoRepository.findAll();
    }
}