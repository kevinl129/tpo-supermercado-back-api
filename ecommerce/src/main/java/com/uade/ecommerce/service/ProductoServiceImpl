package com.uade.tpo.supermercado.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import com.uade.tpo.supermercado.controller.ProductoRequest;
import com.uade.tpo.supermercado.entity.Categoria;
import com.uade.tpo.supermercado.entity.Imagen;
import com.uade.tpo.supermercado.entity.Producto;
import com.uade.tpo.supermercado.excepciones.ProductoDuplicateException;
import com.uade.tpo.supermercado.excepciones.ProductoNotFoundException;
import com.uade.tpo.supermercado.repository.ImagenRepository;
import com.uade.tpo.supermercado.repository.ProductoRepository;
import com.uade.tpo.supermercado.spec.ProductoSpecification;
import org.springframework.data.jpa.domain.Specification;

@Service
public class ProductoServiceImpl implements ProductoService {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private ImagenRepository imagenRepository;

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

        // Crea un nuevo producto y lo guarda en la base de datos
        // Verifica si el producto ya existe
        if (productoRepository.existsByNombreAndDescripcionAndMarcaAndCategoria(
                productoRequest.getNombre(),
                productoRequest.getDescripcion(),
                productoRequest.getMarca(),
                categorias.getCategoriaById(productoRequest.getCategoria_id()).get())) {
            throw new ProductoDuplicateException("El producto ya existe.");
        }
        // Si no existe, crea un nuevo producto
        Producto nuevoProducto = new Producto();
        nuevoProducto.setNombre(productoRequest.getNombre());
        nuevoProducto.setDescripcion(productoRequest.getDescripcion());
        nuevoProducto.setMarca(productoRequest.getMarca());
        nuevoProducto.setPrecio(productoRequest.getPrecio());
        categorias.getCategoriaById(productoRequest.getCategoria_id())
                .ifPresent(nuevoProducto::setCategoria);
        nuevoProducto.setStock(productoRequest.getStock());
        nuevoProducto.setStock_minimo(productoRequest.getStockMinimo());
        nuevoProducto.setUnidad_medida(productoRequest.getUnidadMedida());
        nuevoProducto.setEstado(productoRequest.getEstado());
        nuevoProducto.setVentas_totales(productoRequest.getVentasTotales());
        nuevoProducto.setDescuento(productoRequest.getDescuento());

        Producto productoConImagenes = productoRepository.save(nuevoProducto);
        // agregamos las imagenes
        List<Imagen> imagenes = new ArrayList<>();
        for (String imagenUrl : productoRequest.getImagenes()) {
            Imagen imagen = new Imagen();
            imagen.setImagen(imagenUrl);
            imagen.setProducto(nuevoProducto);
            imagenes.add(imagen);
            imagenRepository.save(imagen); // Guarda cada imagen en la base de datos
        }
        // Asocia las imágenes al producto
        productoConImagenes.setImagenes(imagenes);
        return productoConImagenes;
    }

    @Override
    public Producto updateProducto(int id, ProductoRequest productoRequest)
            throws ProductoNotFoundException {
        // Verifica si el producto existe
        if (!productoRepository.findById(id).isPresent()) {
            throw new ProductoNotFoundException("El producto no existe.");
        }
        Producto producto = productoRepository.findById(id).get();
        producto.setNombre(productoRequest.getNombre());
        producto.setDescripcion(productoRequest.getDescripcion());
        producto.setMarca(productoRequest.getMarca());
        producto.setPrecio(productoRequest.getPrecio());
        categorias.getCategoriaById(productoRequest.getCategoria_id())
                .ifPresent(producto::setCategoria);
        producto.setStock(productoRequest.getStock());
        producto.setStock_minimo(productoRequest.getStockMinimo());
        producto.setUnidad_medida(productoRequest.getUnidadMedida());
        producto.setEstado(productoRequest.getEstado());
        producto.setVentas_totales(productoRequest.getVentasTotales());
        producto.setDescuento(productoRequest.getDescuento());
        // eliminar las imagenes viejas
        producto.getImagenes().clear(); // Limpia la lista de imágenes del producto
        Producto productoConImagenes = productoRepository.save(producto); // Guarda el producto, JPA elimina las imágenes huérfanas
        // Guarda las nuevas imágenes en la base de datos
        List<Imagen> imagenes = new ArrayList<>();
        for (String imagenUrl : productoRequest.getImagenes()) {
            Imagen imagen = new Imagen();
            imagen.setImagen(imagenUrl);
            imagen.setProducto(producto);
            imagenes.add(imagen);
            imagenRepository.save(imagen); // Guarda cada imagen en la base de datos
        }
        // Asocia las imágenes al producto
        productoConImagenes.setImagenes(imagenes);
        return productoConImagenes;
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
    public Page<Producto> filtrarProductos(String nombre, String marca, Integer categoriaId, BigDecimal precioMin,
            BigDecimal precioMax, Pageable pageable) {
        Specification<Producto> spec = Specification.where(ProductoSpecification.nombreContains(nombre))
                .and(ProductoSpecification.marcaEquals(marca))
                .and(ProductoSpecification.categoriaIdEquals(categoriaId))
                .and(ProductoSpecification.precioGreaterThanOrEqual(precioMin))
                .and(ProductoSpecification.precioLessThanOrEqual(precioMax));
        return productoRepository.findAll(spec, pageable);
    }

}
