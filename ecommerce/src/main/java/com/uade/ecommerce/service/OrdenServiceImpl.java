package com.uade.ecommerce.service;

import java.math.BigDecimal;
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
    private CarritoRepository carritoRepository;
    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private DetalleOrdenRepository detalleOrdenRepository;

    @Autowired
    private DireccionService direccionService;

    @Transactional
    public Orden finalizarCompra(Usuario usuario, Integer direccionId) {

        // 1. Obtener el carrito del usuario
        Carrito carrito = carritoRepository.findByUsuarioIdAndEstado(usuario.getId(), EstadoCarrito.ACTIVO)
                .orElseThrow(() -> new EstadoInvalidoException("El carrito esta vacio"));

        // 2. Verificar el stock de los productos en el carrito
        for (ItemCarrito item : carrito.getItemsCarrito()) {
            Producto producto = item.getProducto();
            if (producto.getStock() - producto.getStock_minimo() < item.getCantidad()) {
                throw new StockInsuficienteException(
                        "No hay suficiente stock para el producto: " + producto.getNombre());
            }
            if (!"activo".equalsIgnoreCase(producto.getEstado())) {
                throw new EstadoInvalidoException("El producto con ID: " + producto.getId() + " está desactivado.");
            }

        }

        // 5. Calcular el total de la compra
        BigDecimal totalCompra = carrito.getItemsCarrito().stream()
                .map(item -> item.getPrecio_unitario().multiply(new BigDecimal(item.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 6. Obtener la dirección de envío si corresponde
        Direccion direccionEnvio = null;
        if (direccionId != null) {
            direccionEnvio = direccionService.getDireccionById(direccionId)
                    .filter(dir -> dir.getUsuario().getId() == usuario.getId())
                    .orElseThrow(() -> new NoEncontradoException("Dirección no encontrada o no pertenece al usuario"));
        }

        // 7. Crear la orden
        Orden orden = new Orden(usuario, totalCompra, LocalDateTime.now(), "FINALIZADA", direccionEnvio,
                BigDecimal.ZERO);

        // 8. Guardar la orden
        ordenRepository.save(orden);

        // 9. Crear los detalles de la orden y actualizar el stock de los productos
        for (ItemCarrito item : carrito.getItemsCarrito()) {
            BigDecimal subtotal = item.getPrecio_unitario().multiply(BigDecimal.valueOf(item.getCantidad()));

            // Crear el detalle de la orden
            DetalleOrden detalle = new DetalleOrden(item.getCantidad(), item.getPrecio_unitario(), subtotal, orden,
                    item.getProducto());
            detalleOrdenRepository.save(detalle);
            orden.getItemsOrden().add(detalle);

            // Actualizar el stock del producto
            Producto producto = item.getProducto();
            producto.setStock(producto.getStock() - item.getCantidad());
            productoRepository.save(producto);
        }

        // 10. Vaciar el carrito
        carrito.getItemsCarrito().clear();
        carrito.setEstado(EstadoCarrito.VACIO);

        carrito.setFechaActivacion(null);

        // 11. Guardar el carrito vacío
        carritoRepository.save(carrito);

        // 12. Devolver la orden
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
        double descuentoTotal = 0.0;
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
            descuentoTotal += descuentoItem;
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

        return new OrdenResponseDTO(
                orden.getId(),
                fechaFormateada,
                orden.getEstado(),
                redondear(subtotal),
                redondear(descuentoTotal),
                redondear(total),
                direccionStr,
                items);
    }

    private String formatearDireccion(Direccion direccion) {
        if (direccion == null) {
            return "";
        }
        // Ajusta los campos según la estructura de tu clase Direccion
        return direccion.getCalle() + " " + direccion.getNumero() +
        // (direccion.getPiso() != null ? ", Piso " + direccion.getPiso() : "") +
        // (direccion.getDepartamento() != null ? ", Depto " +
        // direccion.getDepartamento() : "") +
                ", " + direccion.getCiudad() +
                ", " + direccion.getProvincia() +
                ", " + direccion.getCodigoPostal();
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
