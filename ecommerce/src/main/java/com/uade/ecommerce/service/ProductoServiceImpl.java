package com.uade.ecommerce.service;

import java.math.BigDecimal;
// import java.util.ArrayList; // Ya no se usa para imágenes
// import java.util.List;    // Ya no se usa para imágenes
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.uade.ecommerce.controller.ProductoRequest;
import com.uade.ecommerce.entity.Categoria;
// import com.uade.ecommerce.entity.Imagen; // Ya no se usa aquí
import com.uade.ecommerce.entity.Producto;
import com.uade.ecommerce.exception.ProductoDuplicateException;
import com.uade.ecommerce.exception.ProductoNotFoundException;
import com.uade.ecommerce.repository.ImagenRepository;
import com.uade.ecommerce.repository.ProductoRepository;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    // Ya no necesitamos ImagenRepository aquí si solo lo usábamos para los bucles
    // @Autowired
    // private ImagenRepository imagenRepository; 

    @Autowired
    private CategoriaService categorias;

    @Override
    public Page<Producto> getProductos(Pageable pageable) {
        return productoRepository.findAll(pageable);
    }

    @Override
    public Optional<Producto> getProductoByName(String nombre) {
        return productoRepository.findByNombre(nombre);
    }

    @Override
    public Optional<Producto> getProductoByCategory(Categoria categoria) {
        return productoRepository.findByCategoria(categoria);
    }

    @Override
    public Optional<Producto> getProductoByMarca(String marca) {
        return productoRepository.findByMarca(marca);
    }

    // ... (Otros métodos 'get' que tenías) ...
    @Override
    public Optional<Producto> getProductoByPrecio(BigDecimal precioMax, BigDecimal precioMin) {
        return productoRepository.findByPrecio(precioMax, precioMin);
    }

    @Override
    public Optional<Producto> getProductoByPrecioMaximo(BigDecimal precio) {
        return productoRepository.findByPrecioMaximo(precio);
    }

    @Override
    public Optional<Producto> getProductoByPrecioMinimo(BigDecimal precio) {
        return productoRepository.findByPrecioMinimo(precio);
    }


    @Override
    public Producto createProducto(ProductoRequest productoRequest)
            throws ProductoDuplicateException {

        // 1. Verificación de duplicado (tu lógica existente)
        if (productoRepository.existsByNombreAndDescripcionAndMarcaAndCategoria(
                productoRequest.getNombre(),
                productoRequest.getDescripcion(),
                productoRequest.getMarca(),
                categorias.getCategoriaById(productoRequest.getCategoria_id()).get())) {
            throw new ProductoDuplicateException("El producto ya existe.");
        }
        
        // 2. Mapeo de DTO a Entidad (solo datos del producto)
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(productoRequest.getNombre());
        nuevoProducto.setDescripcion(productoRequest.getDescripcion());
        nuevoProducto.setMarca(productoRequest.getMarca());
        nuevoProducto.setPrecio(productoRequest.getPrecio());
        categorias.getCategoriaById(productoRequest.getCategoria_id())
                .ifPresent(categoria -> nuevoProducto.setCategoria(categoria));
        nuevoProducto.setStock(productoRequest.getStock());
        nuevoProducto.setStock_minimo(productoRequest.getStockMinimo());
        nuevoProducto.setEstado(productoRequest.getEstado());
        nuevoProducto.setDescuento(productoRequest.getDescuento());

        // 3. ✅ CAMBIO CLAVE: Guardar y devolver.
        Producto productoGuardado = productoRepository.save(nuevoProducto);
        
        // ▼▼▼ LÓGICA DE IMÁGENES ELIMINADA ▼▼▼
        // Ya no creamos imágenes desde la lista de strings.
        // Eso ahora se maneja 100% por ImagenService.
        
        return productoGuardado;
    }

    @Override
    public Producto updateProducto(int id, ProductoRequest productoRequest)
            throws ProductoNotFoundException {
        
        // 1. Verificar si existe y obtenerlo
        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new ProductoNotFoundException("El producto no existe."));

        // 2. Mapeo de DTO a Entidad (solo datos del producto)
        producto.setNombre(productoRequest.getNombre());
        producto.setDescripcion(productoRequest.getDescripcion());
        producto.setMarca(productoRequest.getMarca());
        producto.setPrecio(productoRequest.getPrecio());
        categorias.getCategoriaById(productoRequest.getCategoria_id())
                .ifPresent(producto::setCategoria);
        producto.setStock(productoRequest.getStock());
        producto.setStock_minimo(productoRequest.getStockMinimo());
        producto.setEstado(productoRequest.getEstado());
        producto.setDescuento(productoRequest.getDescuento());

        // ▼▼▼ LÍNEA ELIMINADA ▼▼▼
        // producto.getImagenes().clear(); // ¡NO BORRAMOS LAS IMÁGENES!
       
        // 3. ✅ CAMBIO CLAVE: Simplemente guardamos los cambios.
        Producto productoActualizado = productoRepository.save(producto);
        
        // ▼▼▼ LÓGICA DE IMÁGENES ELIMINADA ▼▼▼
        // Ya no borramos ni creamos imágenes desde la lista de strings.
        
        return productoActualizado;
    }

    @Override
    public void deleteProducto(int id) throws ProductoNotFoundException {
        productoRepository.deleteById(id);
    }

    @Override
    public Optional<Producto> getProductoById(int id) {
        return productoRepository.findById(id);
    }
    
     @Override
    public Page<Producto> filtrarProductos(String nombre, String marca, Integer categoriaId,
                                           BigDecimal precioMin, BigDecimal precioMax, Pageable pageable) {
        return productoRepository.filtrarProductos(nombre, marca, categoriaId, precioMin, precioMax, pageable);
    }
}