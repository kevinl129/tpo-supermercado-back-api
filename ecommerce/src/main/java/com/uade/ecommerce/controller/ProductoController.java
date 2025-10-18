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

import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "http://localhost:5174")
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
        // Se puede obtener un producto por id
        if (id < 1)
            // Si el id es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        Optional<Producto> result = productoService.getProductoById(id);
        if (result.isPresent())
             return ResponseEntity.ok(Map.of(
                "mensaje", "Producto encontrado con éxito",
                "producto", new ProductoDTO(result.get())
            ));
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
    public ResponseEntity<?> createProducto(@RequestBody ProductoRequest producto)
            throws ProductoDuplicateException, ParametroFueraDeRangoException {
        // Se puede crear un producto
            // Si el id de la categoria es menor a 1, se lanza una excepción
            
        if (producto.getNombre() == null || producto.getNombre().isEmpty()) {
            // Si el nombre es nulo o vacío, se lanza una excepción
            throw new ParametroFueraDeRangoException("El nombre del producto no puede ser nulo o vacío");
        }
        if (producto.getPrecio() == null || producto.getPrecio().compareTo(BigDecimal.ZERO) <= 0) {
            // Si el precio es nulo o menor a 0, se lanza una excepción
            throw new ParametroFueraDeRangoException("El precio no puede ser nulo o menor a 0");
        }
            // Si el id de la categoria es menor a 1, se lanza una excepción
            
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
        if (producto.getImagenes().size() > 10) {
            // Si la lista de imagenes es mayor a 10, se lanza una excepción
            throw new ParametroFueraDeRangoException("No se pueden agregar más de 10 imagenes");
        }
        // Si la categoria no existe, se lanza una excepción
        categorias.getCategoriaById(producto.getCategoria_id())
                .orElseThrow(() -> new ParametroFueraDeRangoException("La categoría no existe"));
        Producto result = productoService.createProducto(producto);
        return ResponseEntity.created(URI.create("/productos/" + result.getId())).body(Map.of(
            "mensaje", "Producto creado correctamente",
            "id", result.getId(),
            "producto", new ProductoDTO(result)
        ));    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProducto(@PathVariable int id, @RequestBody ProductoRequest productoRequest)
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
        if (productoRequest.getImagenes().size() > 10) {
            // Si la lista de imagenes es mayor a 10, se lanza una excepción
            throw new ParametroFueraDeRangoException("No se pueden agregar más de 10 imagenes");
        }
        // Si la categoria no existe, se lanza una excepción
        categorias.getCategoriaById(productoRequest.getCategoria_id())
                .orElseThrow(() -> new ParametroFueraDeRangoException("La categoría no existe"));
        Producto result = productoService.updateProducto(id, productoRequest);
        return ResponseEntity.ok(Map.of(
            "mensaje", "Producto actualizado correctamente",
            "productoActualizado", new ProductoDTO(result)
        ));    
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProducto(@PathVariable int id) throws ProductoNotFoundException {
        // Se puede eliminar un producto
        if (id < 1) {
            // Si el id es menor a 1, se lanza una excepción
            throw new ParametroFueraDeRangoException("El id del producto debe ser mayor a 0");
        }
        // Si el producto no existe, se lanza una excepción
        productoService.getProductoById(id)
                .orElseThrow(() -> new ProductoNotFoundException("No se encontró el producto con id: " + id));
        productoService.deleteProducto(id);
        return ResponseEntity.ok(Map.of("mensaje", "Producto con ID " + id + " eliminado correctamente"));    }
}
