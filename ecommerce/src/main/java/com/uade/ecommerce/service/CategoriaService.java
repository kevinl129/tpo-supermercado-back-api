package com.uade.ecommerce.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.List;
import com.uade.ecommerce.entity.dto.CategoryRequest;
import com.uade.ecommerce.entity.Categoria;

public interface CategoriaService {
    // Obtener todas las categorías paginadas
    Page<Categoria> getCategorias(Pageable pageable);

    // Obtener una categoría por ID
    Optional<Categoria> getCategoriaById(int id);

    // Contar cantidad total de categorías
    long countCategorias();

    // Eliminar todas las categorías
    void deleteAllCategories();

    // Obtener subcategorías de una categoría padre
    List<Categoria> getSubcategoriasByParentId(int parentId);

    // Eliminar una categoría por ID
    void deleteCategory(int id);

    // Verificar si ya existe una categoría con ese nombre y padre
    boolean existsByNombreAndPadre(String nombre, Integer parentId);

    // Crear una nueva categoría desde un DTO
    Categoria createCategory(CategoryRequest categoryRequest);

    // Actualizar una categoría existente
    Categoria updateCategory(int id, CategoryRequest categoryRequest);

}
