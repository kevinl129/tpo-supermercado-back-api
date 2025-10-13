package com.uade.ecommerce.repository;

import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uade.ecommerce.entity.Imagen;

@Repository
public interface ImagenRepository extends JpaRepository<Imagen, Integer> {
    void deleteByProductoId(int producto_id); // Eliminar las imagenes de un producto por su id
    List<Imagen> findByProductoId(int producto_id); // Buscar imagenes por el id del producto
    
}
