package com.uade.ecommerce.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.uade.ecommerce.entity.*;
import com.uade.ecommerce.entity.dto.CarritoResponse;
import com.uade.ecommerce.entity.dto.ItemCarritoDTO;
//import com.uade.ecommerce.entity.dto.ItemOrdenDTO;
//import com.uade.ecommerce.entity.dto.OrdenResponseDTO;
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

    @Override
    public Carrito crearCarrito(Usuario usuario) {
        // Chequear si ya tiene un carrito ACTIVO o VACIO
        boolean yaTieneCarrito = carritoRepository
                .findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO)
                .isPresent()
                || carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.VACIO).isPresent();

        if (yaTieneCarrito) {
            throw new DatoDuplicadoException("El usuario ya tiene un carrito.");
        }
        // Crear el carrito con estado "VACIO"
        Carrito nuevoCarrito = new Carrito(usuario, EstadoCarrito.VACIO, LocalDateTime.now());

        carritoRepository.save(nuevoCarrito); // Guardar el nuevo carrito en la base de datos

        return nuevoCarrito; // Retornar el carrito recién creado

    }

    @Override
    public Carrito obtenerCarrito(Usuario usuario) {

        // Buscar el carrito del usuario
        Optional<Carrito> carritoExistente = carritoRepository.findByUsuario(usuario);

        // Si no existe el carrito, crear uno vacío
        if (!carritoExistente.isPresent()) {
            return crearCarrito(usuario);
        }

        Carrito carrito = carritoExistente.get();

        // Si el carrito está vacío, asegurarse de que su estado sea "VACIO"
        if (carrito.getItemsCarrito().isEmpty()) {
            carrito.setEstado(EstadoCarrito.VACIO);
        } else {
            // Si hay productos, asegurarse de que el estado sea "ACTIVO"
            carrito.setEstado(EstadoCarrito.ACTIVO);
        }

        return carrito;

    }

    @Override
    @Transactional
    public Carrito agregarProducto(Usuario usuario, int productoId, int cantidad) {

        // Obtener el carrito (activo o vacio)
        Carrito carrito = carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.VACIO)
                .or(() -> carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO))
                .orElseGet(() -> crearCarrito(usuario));

        // Validar existencia del producto
        Producto producto = productoService.getProductoById(productoId)
                .orElseThrow(() -> new NoEncontradoException("Producto no encontrado con ID: " + productoId));

        // Validar que el producto esté activo
        if (!"activo".equalsIgnoreCase(producto.getEstado())) {
            throw new EstadoInvalidoException("El producto con ID: " + producto.getId() + " está desactivado.");
        }

        // Validar stock disponible
        if (producto.getStock() - producto.getStock_minimo() < cantidad) {
            throw new StockInsuficienteException("No hay suficiente stock para el producto con ID: " + productoId);
        }

        // Verificar si el producto ya está en el carrito
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
            // Si no existe, crear nuevo ItemCarrito
            BigDecimal precioConDescuento = producto.getPrecio();
            if (producto.getDescuento() != null && producto.getDescuento().compareTo(BigDecimal.ZERO) > 0) {
                // Ejemplo si descuento es porcentaje (e.g. 15 para 15%)
                BigDecimal descuento = producto.getDescuento().divide(new BigDecimal(100));
                precioConDescuento = producto.getPrecio().multiply(BigDecimal.ONE.subtract(descuento));
            } else {
                // Si no hay descuento, usar el precio original
                precioConDescuento = producto.getPrecio();
            }
            ItemCarrito nuevoItem = new ItemCarrito(cantidad, precioConDescuento, carrito, producto);
            carrito.getItemsCarrito().add(nuevoItem);
        }

        // Si el carrito estaba vacío, cambiar su estado a "ACTIVO"
        if (carrito.getEstado() == EstadoCarrito.VACIO) {
            carrito.setEstado(EstadoCarrito.ACTIVO);
            carrito.setFechaActivacion(LocalDateTime.now());
        }

        // Guardar carrito y devolver
        return carritoRepository.save(carrito);

    }

    @Override
    public Carrito eliminarProducto(Usuario usuario, int productoId, int cantidad) {

        // 1. Obtener el carrito del usuario
        Carrito carrito = carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new EstadoInvalidoException("El carrito esta vacio"));

        // 2. Verificar si el producto está en el carrito
        Optional<ItemCarrito> itemCarrito = carrito.getItemsCarrito().stream()
                .filter(item -> item.getProducto().getId() == productoId)
                .findFirst();

        // 3. Si el producto no está en el carrito, lanzar una excepción
        if (!itemCarrito.isPresent()) {
            throw new NoEncontradoException("El producto no está en el carrito");
        }
        // validar la cantidad
        if (cantidad <= 0) {
            throw new ParametroFueraDeRangoException("La cantidad debe ser mayor a cero.");
        }

        // 4. Eliminar el producto del carrito
        ItemCarrito item = itemCarrito.get();
        if (item.getCantidad() > cantidad) {
            item.setCantidad(item.getCantidad() - cantidad);
        } else {
            carrito.getItemsCarrito().remove(item);
        }
        // 5. Verificar si el carrito está vacío después de la eliminación
        if (carrito.getItemsCarrito().isEmpty()) {
            carrito.setEstado(EstadoCarrito.VACIO);
            carrito.setFechaActivacion(null); // Limpiar la fecha de activación
        }

        // 6. Persistir los cambios
        carritoRepository.save(carrito); // Guardar los cambios en el carrito

        // 7. Retornar el carrito actualizado
        return carrito;

    }

    @Override
    public Carrito vaciarCarrito(Usuario usuario) {
        // 1. Obtener el carrito del usuario
        Carrito carrito = carritoRepository.findByUsuarioIdAndEstadoConItems(usuario.getId(), EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new EstadoInvalidoException("El carrito esta vacio"));

        // 2. Vaciar el carrito eliminando todos los productos del carrito
        carrito.getItemsCarrito().clear(); // Elimina todos los productos del carrito

        // 3. Cambiar el estado del carrito a "VACIO"
        carrito.setEstado(EstadoCarrito.VACIO);

        carrito.setFechaActivacion(null); // Limpiar la fecha de activación

        // 4. Persistir los cambios
        carritoRepository.save(carrito); // Guardar el carrito vacío en la base de datos

        // 5. Retornar el carrito actualizado
        return carrito;

    }

    @Scheduled(fixedRate = 3600000) // Se ejecuta cada hora (3600000 ms)
    @Transactional
    @Override
    public void vaciarCarritosAntiguos() {
        LocalDateTime seisHorasAtras = LocalDateTime.now().minusHours(6);

        // Buscar carritos activos que tienen más de 6 horas desde su activación
        List<Carrito> carritosAntiguos = carritoRepository.findByEstadoAndFechaActivacionBefore(EstadoCarrito.ACTIVO,
                seisHorasAtras);

        // Vaciar los carritos y actualizar su estado
        for (Carrito carrito : carritosAntiguos) {
            carrito.setEstado(EstadoCarrito.VACIO); // Cambiar estado a "VACIO"
            carrito.getItemsCarrito().clear(); // Eliminar todos los productos del carrito
            carritoRepository.save(carrito); // Persistir los cambios
        }
    }

}
