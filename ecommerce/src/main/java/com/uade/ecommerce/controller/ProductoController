package com.uade.tpo.supermercado.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uade.tpo.supermercado.controller.dto.CatalogoResponse;
import com.uade.tpo.supermercado.controller.dto.ProductoDTO;
import com.uade.tpo.supermercado.entity.Categoria;
import com.uade.tpo.supermercado.entity.Producto;
import com.uade.tpo.supermercado.excepciones.*;
import com.uade.tpo.supermercado.service.ProductoService;
import com.uade.tpo.supermercado.service.CategoriaService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("producto")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private CategoriaService categorias;

    @GetMapping
    public ResponseEntity<Page<ProductoDTO>> getProductos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax) throws ProductoNotFoundException {
        int pageNum = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 200 : size;
        if (pageNum < 0 || pageSize < 1) {
            throw new ParametroFueraDeRangoException("Los parámetros de paginación deben ser mayores a 0");
        }
        Page<Producto> productos = productoService.filtrarProductos(nombre, marca, categoriaId, precioMin, precioMax,
                PageRequest.of(pageNum, pageSize));
        if (productos.isEmpty()) {
            throw new ProductoNotFoundException("No hay productos que coincidan con los filtros");
        }
        Page<ProductoDTO> productosDTO = productos.map(ProductoDTO::new);
        return ResponseEntity.ok(productosDTO);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<ProductoDTO> getProductoById(@PathVariable int id) throws ProductoNotFoundException {
        // Se puede obtener un producto por id
        if (id < 1)
            // Si el id es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        Optional<Producto> result = productoService.getProductoById(id);
        if (result.isPresent())
            return ResponseEntity.ok(new ProductoDTO(result.get()));
        // Si no se encuentra el producto, se lanza una excepción
        throw new ProductoNotFoundException("No se encontró el producto con id: " + id);
    }

    @GetMapping("/nombre/{nombreProducto}")
    public ResponseEntity<ProductoDTO> getProductoByName(@RequestParam String nombreProducto)
            throws ProductoNotFoundException {
        // Se puede obtener el producto por nombre
        if (nombreProducto == null || nombreProducto.isEmpty())
            // Si el nombre es nulo o vacío, se lanza una excepción
            throw new ParametroFueraDeRangoException("El nombre del producto no puede ser nulo o vacío");
        Optional<Producto> result = productoService.getProductoByName(nombreProducto);
        if (result.isPresent())
            return ResponseEntity.ok(new ProductoDTO(result.get()));
        // Si no se encuentra el producto, se lanza una excepción
        throw new ProductoNotFoundException("No se encontró el producto con nombre: " + nombreProducto);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ProductoDTO> getProductoByCategory(@RequestParam int categoria_id)
            throws ProductoNotFoundException {
        // Se puede obtener el producto por el id de la categoria
        if (categoria_id < 1)
            // Si el id de la categoria es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id de la categoría debe ser mayor a 0");
        Optional<Categoria> categoriaOptional = categorias.getCategoriaById(categoria_id);
        if (categoriaOptional.isPresent()) {
            Categoria categoria = categoriaOptional.get();
            Optional<Producto> producto = productoService.getProductoByCategory(categoria);
            // Si la categoria es nula, se lanza una excepción
            return ResponseEntity.ok(new ProductoDTO(producto.get()));

        }
        // Si no se encuentra el producto, se lanza una excepción
        throw new ProductoNotFoundException("No se encontró el producto con categoría: " + categoria_id);
    }

    @GetMapping("/marca/{marca}")
    public ResponseEntity<ProductoDTO> getProductoByMarca(@RequestParam String marca) throws ProductoNotFoundException {
        // Se puede obtener el producto por marca
        if (marca == null || marca.isEmpty())
            // Si la marca es nula o vacía, se lanza una excepción
            throw new ParametroFueraDeRangoException("La marca no puede ser nula o vacía");
        Optional<Producto> result = productoService.getProductoByMarca(marca);
        if (result.isPresent())
            return ResponseEntity.ok(new ProductoDTO(result.get()));
        // Si no se encuentra el producto, se lanza una excepción
        throw new ProductoNotFoundException("No se encontró el producto con marca: " + marca);
    }

    @GetMapping("/precio/{precioMax}&{precioMin}")
    public ResponseEntity<ProductoDTO> getProductoByPrecioMaximo(@RequestParam BigDecimal precioMax,
            @RequestParam BigDecimal precioMin) throws ProductoNotFoundException {
        // Se puede obtener el producto por precio
        Optional<Producto> result = Optional.empty();
        if (precioMax == null && precioMin == null) {
            // Si ambos son nulos, se lanza una excepción
            throw new ParametroFueraDeRangoException("Ambos precios no pueden ser nulos");
        } else if (precioMax != null && precioMin != null && precioMax.compareTo(precioMin) < 0) {
            // Si el precio máximo es menor al mínimo, se lanza una excepción
            throw new ParametroFueraDeRangoException("El precio máximo debe ser mayor al mínimo");
        } else if (precioMax == null) {
            // Si el precio máximo es nulo, se busca por precio mínimo
            result = productoService.getProductoByPrecioMinimo(precioMin);
        } else if (precioMin == null) {
            // Si el precio mínimo es nulo, se busca por precio máximo
            result = productoService.getProductoByPrecioMaximo(precioMax);
        } else {
            result = productoService.getProductoByPrecio(precioMax, precioMin);
        }
        if (result.isPresent()) {
            return ResponseEntity.ok(new ProductoDTO(result.get()));
        }
        // Si no se encuentra el producto, se lanza una excepción
        throw new ProductoNotFoundException(
                "No se encontraron productos con precio máximo: " + precioMax + " y mínimo: " + precioMin);
    }

    @GetMapping("/catalogo")
    public ResponseEntity<Page<CatalogoResponse>> getCatalogo(@RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax) throws ProductoNotFoundException {
        // Se puede obtener el catalogo de productos
        int pageNum = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 200 : size;
        if (pageNum < 0 || pageSize < 1) {
            throw new ParametroFueraDeRangoException("Los parámetros de paginación deben ser mayores a 0");
        }
        Page<Producto> productos = productoService.filtrarProductos(nombre, marca, categoriaId, precioMin, precioMax,
                PageRequest.of(pageNum, pageSize));
        if (productos.isEmpty()) {
            throw new ProductoNotFoundException("No hay productos que coincidan con los filtros");
        }
        Page<CatalogoResponse> catalogoResponse = productos.stream()
                .filter(producto -> producto.getStock() > producto.getStock_minimo()
                        && producto.getEstado().equals("activo"))
                .map(CatalogoResponse::new)
                .collect(Collectors.collectingAndThen(Collectors.toList(),
                        list -> new PageImpl<>(list, productos.getPageable(), productos.getTotalElements())));
        if (catalogoResponse.isEmpty()) {
            // Si no hay productos, se lanza una excepción
            throw new ProductoNotFoundException("No hay productos cargados");
        }
        return ResponseEntity.ok(catalogoResponse);
    }

    @PostMapping
    public ResponseEntity<Object> createProducto(@RequestBody ProductoRequest producto)
            throws ProductoDuplicateException, ParametroFueraDeRangoException {
        // Se puede crear un producto
        if (producto.getCategoria_id() < 1) {
            // Si el id de la categoria es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        }
        if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
            // Si el nombre es nulo o vacío, se lanza una excepción
            throw new ParametroFueraDeRangoException("El nombre del producto no puede ser nulo o vacío");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            // Si el precio es nulo o menor a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("El precio no puede ser nulo o menor a 0");
        }
        if (producto.getCategoria_id() < 1) {
            // Si el id de la categoria es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id de la categoría debe ser mayor a 0");
        }
        if (producto.getDescripcion() == null || producto.getDescripcion().isEmpty()) {
            // Si la descripción es nula o vacía, se lanza una excepción
            throw new ParametroFueraDeRangoException("La descripción no puede ser nula o vacía");
        }
        if (producto.getStock() < 0) {
            // Si el stock es menor a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("El stock no puede ser menor a 0");
        }
        if (producto.getStockMinimo() < 0) {
            // Si el stock mínimo es menor a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("El stock mínimo no puede ser menor a 0");
        }
        if (producto.getDescuento() == null || producto.getDescuento().compareTo(BigDecimal.ZERO) < 0
                || producto.getDescuento().compareTo(new BigDecimal("100")) > 0) {
            throw new ParametroFueraDeRangoException("El descuento debe estar entre 0 y 100");
        }
        if (producto.getVentasTotales() < 0) {
            // Si las ventas totales son menores a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("Las ventas totales no pueden ser menores a 0");
        }
        if (producto.getImagenes().size() > 10) {
            // Si la lista de imagenes es mayor a 10, se lanza una excepción
            throw new ParametroFueraDeRangoException("No se pueden agregar más de 10 imagenes");
        }
        // Si la categoria no existe, se lanza una excepción
        categorias.getCategoriaById(producto.getCategoria_id())
                .orElseThrow(() -> new ParametroFueraDeRangoException("La categoría no existe"));
        Producto result = productoService.createProducto(producto);
        return ResponseEntity.created(URI.create("/productos/" + result.getId())).body(new ProductoDTO(result));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateProducto(@PathVariable int id, @RequestBody ProductoRequest productoRequest)
            throws ProductoNotFoundException {
        // Se puede actualizar un producto
        if (productoRequest.getCategoria_id() < 1) {
            // Si el id de la categoria es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        }
        if (productoRequest.getNombre() == null || productoRequest.getNombre().isEmpty()) {
            // Si el nombre es nulo o vacío, se lanza una excepción
            throw new ParametroFueraDeRangoException("El nombre del producto no puede ser nulo o vacío");
        }
        if (productoRequest.getPrecio() == null || productoRequest.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            // Si el precio es nulo o menor a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("El precio no puede ser nulo o menor a 0");
        }
        if (productoRequest.getCategoria_id() < 1) {
            // Si el id de la categoria es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id de la categoría debe ser mayor a 0");
        }
        if (productoRequest.getDescripcion() == null || productoRequest.getDescripcion().isEmpty()) {
            // Si la descripción es nula o vacía, se lanza una excepción
            throw new ParametroFueraDeRangoException("La descripción no puede ser nula o vacía");
        }
        if (productoRequest.getStock() < 0) {
            // Si el stock es menor a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("El stock no puede ser menor a 0");
        }
        if (productoRequest.getStockMinimo() < 0) {
            // Si el stock mínimo es menor a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("El stock mínimo no puede ser menor a 0");
        }
        if (productoRequest.getDescuento() == null || productoRequest.getDescuento().compareTo(BigDecimal.ZERO) < 0
                || productoRequest.getDescuento().compareTo(new BigDecimal("100")) > 0) {
            throw new ParametroFueraDeRangoException("El descuento debe estar entre 0 y 100");
        }
        if (productoRequest.getVentasTotales() < 0) {
            // Si las ventas totales son menores a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("Las ventas totales no pueden ser menores a 0");
        }
        if (productoRequest.getImagenes().size() > 10) {
            // Si la lista de imagenes es mayor a 10, se lanza una excepción
            throw new ParametroFueraDeRangoException("No se pueden agregar más de 10 imagenes");
        }
        // Si la categoria no existe, se lanza una excepción
        categorias.getCategoriaById(productoRequest.getCategoria_id())
                .orElseThrow(() -> new ParametroFueraDeRangoException("La categoría no existe"));
        Producto result = productoService.updateProducto(id, productoRequest);
        return ResponseEntity.ok(new ProductoDTO(result));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteProducto(@PathVariable int id) throws ProductoNotFoundException {
        // Se puede eliminar un producto
        if (id < 1) {
            // Si el id es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        }
        // Si el producto no existe, se lanza una excepción
        productoService.getProductoById(id)
                .orElseThrow(() -> new ProductoNotFoundException("No se encontró el producto con id: " + id));
        productoService.deleteProducto(id);
        return ResponseEntity.noContent().build();
    }
}
