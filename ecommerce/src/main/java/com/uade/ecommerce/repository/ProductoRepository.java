package com.uade.ecommerce.repository;

import java.math.BigDecimal;
//import java.time.LocalDate;
//import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.uade.ecommerce.entity.Categoria;
import com.uade.ecommerce.entity.Producto;

//import jakarta.transaction.Transactional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer>, JpaSpecificationExecutor<Producto> {

        @Query(value = "select p from Producto p where p.id = ?1")
        Optional<Producto> findById(int id);

        @Query(value = "select p from Producto p where p.nombre = ?1")
        Optional<Producto> findByNombre(String nombre);

        @Query(value = "select p from Producto p where p.marca = ?1")
        Optional<Producto> findByMarca(String marca);

        @Query(value = "select p from Producto p where p.precio <= ?1")
        Optional<Producto> findByPrecioMaximo(BigDecimal precio);

        @Query(value = "select p from Producto p where p.precio >= ?1")
        Optional<Producto> findByPrecioMinimo(BigDecimal precio);

        @Query(value = "select p from Producto p where p.precio >= ?2 and p.precio <= ?1")
        Optional<Producto> findByPrecio(BigDecimal precioMax, BigDecimal precioMin);

        @Query(value = "select p from Producto p where p.categoria = ?1")
        Optional<Producto> findByCategoria(Categoria categoria);

        boolean existsByNombreAndDescripcionAndMarcaAndCategoria(String nombre, String descripcion,
                        String marca, Categoria categoria);

        @Query("""
        SELECT p FROM Producto p
        WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
          AND (:marca IS NULL OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :marca, '%')))
          AND (:categoriaId IS NULL OR p.categoria.id = :categoriaId)
          AND (:precioMin IS NULL OR p.precio >= :precioMin)
          AND (:precioMax IS NULL OR p.precio <= :precioMax)
    """)
    Page<Producto> filtrarProductos(
        @Param("nombre") String nombre,
        @Param("marca") String marca,
        @Param("categoriaId") Integer categoriaId,
        @Param("precioMin") BigDecimal precioMin,
        @Param("precioMax") BigDecimal precioMax,
        Pageable pageable
    );
}

