package com.uade.ecommerce.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.uade.ecommerce.entity.Categoria;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {
    List<Categoria> findByParentCategoriaId(int parentId);
    boolean existsByNombreAndParentCategoriaId(String nombre, Integer parentId);
}
