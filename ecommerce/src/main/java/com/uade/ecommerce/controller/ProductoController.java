package com.uade.ecommerce.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.uade.ecommerce.controller.dto.CatalogoResponse;
import com.uade.ecommerce.controller.dto.ProductoDTO;
import com.uade.ecommerce.entity.Categoria;
import com.uade.ecommerce.entity.Imagen;
import com.uade.ecommerce.entity.Producto;
import com.uade.ecommerce.exception.*;
import com.uade.ecommerce.service.ProductoService;
import com.uade.ecommerce.service.CategoriaService;
import com.uade.ecommerce.service.ImagenService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("producto")
public class ProductoController {

    @Autowired
    private ProductoService productoService;
    @Autowired
    private CategoriaService categorias;
    @Autowired
    private ImagenService imagenService;

    // Subir 1 imagen
    @PostMapping("/{id}/imagen")
    public ResponseEntity<?> subirImagen(@PathVariable int id,
                                        @RequestParam("archivo") MultipartFile archivo) throws Exception {
        if (archivo == null || archivo.isEmpty())
            throw new ParametroFueraDeRangoException("Debe seleccionar un archivo");
        Imagen imagen = imagenService.guardarImagen(id, archivo);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Imagen subida correctamente al producto con ID " + id,
            "idImagen", imagen.getId(),
            "path", imagen.getImagen()
        ));
    }

    // Listar imágenes de un producto
    @GetMapping("/{id}/imagenes")
    public ResponseEntity<?> listarImagenes(@PathVariable int id) {
        var imagenes = imagenService.obtenerImagenesPorProducto(id)
                .stream()
                .map(img -> Map.of("id", img.getId(), "path", img.getImagen()))
                .collect(Collectors.toList());
        if (imagenes.isEmpty()) {
            return ResponseEntity.ok(Map.of("mensaje", "El producto con ID " + id + " no tiene imágenes asociadas"));
        }
        return ResponseEntity.ok(Map.of(
            "mensaje", "Listado de imágenes obtenido correctamente",
            "imagenes", imagenes
        ));
    }

    // Borrar imagen por id
    @DeleteMapping("/imagen/{imagenId}")
    public ResponseEntity<?> borrarImagenPorId(@PathVariable int imagenId) throws IOException {
        imagenService.eliminarImagenPorId(imagenId);
        return ResponseEntity.ok(Map.of("mensaje", "Imagen con ID " + imagenId + " eliminada correctamente"));
    }

    // Borrar todas las imágenes de un producto
    @DeleteMapping("/{id}/imagenes")
    public ResponseEntity<?> borrarImagenesPorProducto(@PathVariable int id) {
        imagenService.eliminarImagenesPorProducto(id);
        return ResponseEntity.ok(Map.of("mensaje", "Todas las imágenes del producto con ID " + id + " fueron eliminadas"));
    }

    // Listar productos
    @GetMapping
    public ResponseEntity<?> getProductos(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false) BigDecimal precioMin,
            @RequestParam(required = false) BigDecimal precioMax) throws ProductoNotFoundException {
        int pageNum = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 200 : size;
        if (pageNum < 0 || pageSize < 1)
            throw new ParametroFueraDeRangoException("Los parámetros de paginación deben ser mayores a 0");

        Page<Producto> productos = productoService.filtrarProductos(nombre, marca, categoriaId, precioMin, precioMax,
                PageRequest.of(pageNum, pageSize));
        if (productos.isEmpty())
            throw new ProductoNotFoundException("No hay productos que coincidan con los filtros");

        Page<ProductoDTO> productosDTO = productos.map(ProductoDTO::new);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Productos obtenidos correctamente",
            "total", productos.getTotalElements(),
            "productos", productosDTO.getContent()
        ));
    }

    // Obtener producto por ID
    @GetMapping("/id/{id}")
    public ResponseEntity<?> getProductoById(@PathVariable int id) throws ProductoNotFoundException {
        if (id < 1)
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        Optional<Producto> result = productoService.getProductoById(id);
        if (result.isPresent())
            return ResponseEntity.ok(Map.of(
                "mensaje", "Producto encontrado con éxito",
                "producto", new ProductoDTO(result.get())
            ));
        throw new ProductoNotFoundException("No se encontró el producto con id: " + id);
    }

    // Crear producto
    @PostMapping
    public ResponseEntity<?> createProducto(@RequestBody ProductoRequest producto)
            throws ProductoDuplicateException, ParametroFueraDeRangoException {
        if (producto.getNombre() == null || producto.getNombre().isEmpty())
            throw new ParametroFueraDeRangoException("El nombre del producto no puede ser nulo o vacío");
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0)
            throw new ParametroFueraDeRangoException("El precio no puede ser nulo o menor a 0");
        if (producto.getDescripcion() == null || producto.getDescripcion().isEmpty())
            throw new ParametroFueraDeRangoException("La descripción no puede ser nula o vacía");
        if (producto.getStock() < 0)
            throw new ParametroFueraDeRangoException("El stock no puede ser menor a 0");
        if (producto.getStockMinimo() < 0)
            throw new ParametroFueraDeRangoException("El stock mínimo no puede ser menor a 0");
        if (producto.getDescuento() == null || producto.getDescuento().compareTo(BigDecimal.ZERO) < 0
                || producto.getDescuento().compareTo(new BigDecimal("100")) > 0)
            throw new ParametroFueraDeRangoException("El descuento debe estar entre 0 y 100");
        if (producto.getImagenes() != null && producto.getImagenes().size() > 10)
            throw new ParametroFueraDeRangoException("No se pueden agregar más de 10 imágenes");

        categorias.getCategoriaById(producto.getCategoria_id())
                .orElseThrow(() -> new ParametroFueraDeRangoException("La categoría no existe"));

        Producto result = productoService.createProducto(producto);
        return ResponseEntity.created(URI.create("/productos/" + result.getId())).body(Map.of(
            "mensaje", "Producto creado correctamente",
            "id", result.getId(),
            "producto", new ProductoDTO(result)
        ));
    }

    // Actualizar producto
    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable int id, @RequestBody ProductoRequest productoRequest)
            throws ProductoNotFoundException {
        if (productoRequest.getCategoria_id() < 1)
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        if (productoRequest.getNombre() == null || productoRequest.getNombre().isEmpty())
            throw new ParametroFueraDeRangoException("El nombre del producto no puede ser nulo o vacío");
        if (productoRequest.getPrecio() == null || productoRequest.getPrecio().compareTo(BigDecimal.ZERO) <= 0)
            throw new ParametroFueraDeRangoException("El precio no puede ser nulo o menor a 0");
        if (productoRequest.getDescripcion() == null || productoRequest.getDescripcion().isEmpty())
            throw new ParametroFueraDeRangoException("La descripción no puede ser nula o vacía");
        if (productoRequest.getStock() < 0)
            throw new ParametroFueraDeRangoException("El stock no puede ser menor a 0");
        if (productoRequest.getStockMinimo() < 0)
            throw new ParametroFueraDeRangoException("El stock mínimo no puede ser menor a 0");
        if (productoRequest.getDescuento() == null || productoRequest.getDescuento().compareTo(BigDecimal.ZERO) < 0
                || productoRequest.getDescuento().compareTo(new BigDecimal("100")) > 0)
            throw new ParametroFueraDeRangoException("El descuento debe estar entre 0 y 100");
        if (productoRequest.getImagenes() != null && productoRequest.getImagenes().size() > 10)
            throw new ParametroFueraDeRangoException("No se pueden agregar más de 10 imágenes");

        categorias.getCategoriaById(productoRequest.getCategoria_id())
                .orElseThrow(() -> new ParametroFueraDeRangoException("La categoría no existe"));
        Producto result = productoService.updateProducto(id, productoRequest);

        return ResponseEntity.ok(Map.of(
            "mensaje", "Producto actualizado correctamente",
            "productoActualizado", new ProductoDTO(result)
        ));
    }

    // Eliminar producto
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProducto(@PathVariable int id) throws ProductoNotFoundException {
        if (id < 1)
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");

        productoService.getProductoById(id)
                .orElseThrow(() -> new ProductoNotFoundException("No se encontró el producto con id: " + id));
        productoService.deleteProducto(id);

        return ResponseEntity.ok(Map.of("mensaje", "Producto con ID " + id + " eliminado correctamente"));
    }
}
