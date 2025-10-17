package com.uade.ecommerce.controller;

import com.uade.ecommerce.controller.dto.CatalogoResponse;
import com.uade.ecommerce.controller.dto.ProductoDTO;
import com.uade.ecommerce.entity.Categoria;
import com.uade.ecommerce.entity.Imagen;
import com.uade.ecommerce.entity.Producto;
import com.uade.ecommerce.exception.*;
import com.uade.ecommerce.service.CategoriaService;
import com.uade.ecommerce.service.ImagenService;
import com.uade.ecommerce.service.ProductoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (pageNum < 0 || pageSize < 1) {
            throw new ParametroFueraDeRangoException("Los parámetros de paginación deben ser mayores a 0");
        }
        Page<Producto> productos = productoService.filtrarProductos(nombre, marca, categoriaId, precioMin, precioMax,
                PageRequest.of(pageNum, pageSize));
        if (productos.isEmpty()) {
            throw new ProductoNotFoundException("No hay productos que coincidan con los filtros");
        }
        Page<ProductoDTO> productosDTO = productos.map(ProductoDTO::new);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Productos obtenidos correctamente",
                "total", productos.getTotalElements(),
                "productos", productosDTO.getContent()
        ));
    }

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
    
    // ✅ ENDPOINT NUEVO PARA PRODUCTOS RELACIONADOS
    @GetMapping("/id/{id}/relacionados")
    public ResponseEntity<?> getProductosRelacionados(@PathVariable int id) {
        Pageable pageable = PageRequest.of(0, 4); // Traemos hasta 4 productos relacionados
        Page<Producto> relacionados = productoService.findRelatedProducts(id, pageable);

        if (relacionados.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        List<ProductoDTO> relacionadosDTO = relacionados.getContent().stream()
                .map(ProductoDTO::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(relacionadosDTO);
    }

    @GetMapping("/nombre/{nombreProducto}")
    public ResponseEntity<ProductoDTO> getProductoByName(@RequestParam String nombreProducto)
            throws ProductoNotFoundException {
        if (nombreProducto == null || nombreProducto.isEmpty())
            throw new ParametroFueraDeRangoException("El nombre del producto no puede ser nulo o vacío");
        Optional<Producto> result = productoService.getProductoByName(nombreProducto);
        if (result.isPresent())
            return ResponseEntity.ok(new ProductoDTO(result.get()));
        throw new ProductoNotFoundException("No se encontró el producto con nombre: " + nombreProducto);
    }

    @GetMapping("/categoria/{categoria}")
    public ResponseEntity<ProductoDTO> getProductoByCategory(@RequestParam int categoria_id)
            throws ProductoNotFoundException {
        if (categoria_id < 1)
            throw new ParametroFueraDeRangoException("El id de la categoría debe ser mayor a 0");
        Optional<Categoria> categoriaOptional = categorias.getCategoriaById(categoria_id);
        if (categoriaOptional.isPresent()) {
            Categoria categoria = categoriaOptional.get();
            Optional<Producto> producto = productoService.getProductoByCategory(categoria);
            return ResponseEntity.ok(new ProductoDTO(producto.get()));
        }
        throw new ProductoNotFoundException("No se encontró el producto con categoría: " + categoria_id);
    }

    @GetMapping("/marca/{marca}")
    public ResponseEntity<ProductoDTO> getProductoByMarca(@RequestParam String marca) throws ProductoNotFoundException {
        if (marca == null || marca.isEmpty())
            throw new ParametroFueraDeRangoException("La marca no puede ser nula o vacía");
        Optional<Producto> result = productoService.getProductoByMarca(marca);
        if (result.isPresent())
            return ResponseEntity.ok(new ProductoDTO(result.get()));
        throw new ProductoNotFoundException("No se encontró el producto con marca: " + marca);
    }

    @GetMapping("/precio/{precioMax}&{precioMin}")
    public ResponseEntity<ProductoDTO> getProductoByPrecioMaximo(@RequestParam BigDecimal precioMax,
                                                                 @RequestParam BigDecimal precioMin) throws ProductoNotFoundException {
        Optional<Producto> result = Optional.empty();
        if (precioMax == null && precioMin == null) {
            throw new ParametroFueraDeRangoException("Ambos precios no pueden ser nulos");
        } else if (precioMax != null && precioMin != null && precioMax.compareTo(precioMin) < 0) {
            throw new ParametroFueraDeRangoException("El precio máximo debe ser mayor al mínimo");
        } else if (precioMax == null) {
            result = productoService.getProductoByPrecioMinimo(precioMin);
        } else if (precioMin == null) {
            result = productoService.getProductoByPrecioMaximo(precioMax);
        } else {
            result = productoService.getProductoByPrecio(precioMax, precioMin);
        }
        if (result.isPresent()) {
            return ResponseEntity.ok(new ProductoDTO(result.get()));
        }
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
            throw new ProductoNotFoundException("No hay productos cargados");
        }
        return ResponseEntity.ok(catalogoResponse);
    }

    @PostMapping
    public ResponseEntity<?> createProducto(@RequestBody ProductoRequest producto)
            throws ProductoDuplicateException, ParametroFueraDeRangoException {
        // ... (validaciones)
        Producto result = productoService.createProducto(producto);
        return ResponseEntity.created(URI.create("/productos/" + result.getId())).body(Map.of(
                "mensaje", "Producto creado correctamente",
                "id", result.getId(),
                "producto", new ProductoDTO(result)
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable int id, @RequestBody ProductoRequest productoRequest)
            throws ProductoNotFoundException {
        // ... (validaciones)
        Producto result = productoService.updateProducto(id, productoRequest);
        return ResponseEntity.ok(Map.of(
                "mensaje", "Producto actualizado correctamente",
                "productoActualizado", new ProductoDTO(result)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProducto(@PathVariable int id) throws ProductoNotFoundException {
        if (id < 1) {
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        }
        productoService.getProductoById(id)
                .orElseThrow(() -> new ProductoNotFoundException("No se encontró el producto con id: " + id));
        productoService.deleteProducto(id);
        return ResponseEntity.ok(Map.of("mensaje", "Producto con ID " + id + " eliminado correctamente"));
    }
}