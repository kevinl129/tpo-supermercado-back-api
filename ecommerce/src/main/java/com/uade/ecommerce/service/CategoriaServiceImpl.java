package com.uade.ecommerce.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import com.uade.ecommerce.entity.Categoria;
import com.uade.ecommerce.entity.dto.CategoryRequest;
import com.uade.ecommerce.exception.CategoriaNoEncontrada;
import com.uade.ecommerce.exception.DatoDuplicadoException;
import com.uade.ecommerce.exception.NoEncontradoException;
import com.uade.ecommerce.exception.ParametroFueraDeRangoException;
import com.uade.ecommerce.repository.CategoriaRepository;

@Service
public class CategoriaServiceImpl implements CategoriaService {
    @Autowired
    private CategoriaRepository categoriaRepository;

    @Override
    public Categoria createCategory(CategoryRequest categoryRequest) {
        // Si es subcategoría, validar que el padre exista
        Optional<Categoria> parentCategory = validateParentCategoryExists(categoryRequest.getParentId());

        // Validar que no exista una categoría con el mismo nombre y mismo padre
        validateCategoryDuplicate(categoryRequest.getNombre(), categoryRequest.getParentId());

        // Crear la nueva categoría
        Categoria nuevaCategoria = new Categoria();
        nuevaCategoria.setNombre(categoryRequest.getNombre());

        // Guardar la nueva categoría
        Categoria savedCategory = categoriaRepository.save(nuevaCategoria);

        // Si tiene un padre, agregar la subcategoría a la lista de subcategorías del padre
        if (parentCategory.isPresent()) {
            Categoria parent = parentCategory.get();
            categoriaRepository.save(parent); 
        }

        return savedCategory;

    }

    @Override
    public Categoria updateCategory(int id, CategoryRequest categoryRequest) {
        // 1. Verificar que la categoría a actualizar exista
        Optional<Categoria> categoriaExistente = categoriaRepository.findById(id);
        if (!categoriaExistente.isPresent()) {
            throw new NoEncontradoException("La categoría con ID " + id + " no existe.");
        }

        // 2. Evitar que se asigne como su propio padre
        if (categoryRequest.getParentId() != null && categoryRequest.getParentId().equals(id)) {
            throw new ParametroFueraDeRangoException("No se puede asignar la categoría como su propio padre.");
        }

        // 3. Verificar duplicado (otra categoría con el mismo nombre y mismo padre)
        validateCategoryDuplicate(categoryRequest.getNombre(), categoryRequest.getParentId());

        // 5. Actualizar los datos de la categoría
        Categoria categoria = categoriaExistente.get();
        categoria.setNombre(categoryRequest.getNombre());


        // Guardar los cambios
        return categoriaRepository.save(categoria);

    }

    @Override
    public Page<Categoria> getCategorias(Pageable pageable) {
        return categoriaRepository.findAll(pageable);

    }

    @Override
    public Optional<Categoria> getCategoriaById(int id) {
        // Validación del ID de la categoría
        if (id < 1) {
            throw new ParametroFueraDeRangoException("El ID de la categoría debe ser mayor o igual a 1.");
        }
        // Obtener la categoría por ID
        Optional<Categoria> categoria = categoriaRepository.findById(id);

        return categoria;

    }

    @Override
    public void deleteAllCategories() {
        // Eliminar todas las categorías
        categoriaRepository.deleteAll();
    }

    @Override
    public void deleteCategory(int id) {
        Optional<Categoria> categoria = categoriaRepository.findById(id);

        if (categoria.isPresent()) {
            Categoria cat = categoria.get();
            categoriaRepository.delete(cat);
        }

    }

    @Override
    public boolean existsByNombreAndPadre(String nombre, Integer parentId) {
        return categoriaRepository.existsByNombreAndParentId(nombre, parentId);
    }

    @Override
    public List<Categoria> getSubcategoriasByParentId(int parentId) {
        // Validación del ID de la categoría padre
        if (parentId < 1) {
            throw new ParametroFueraDeRangoException("El ID de la categoría padre debe ser mayor o igual a 1.");
        }

        List<Categoria> subcategorias = categoriaRepository.findByParentId(parentId);

        // Si no se encuentran subcategorías, lanzamos una excepción
        if (subcategorias.isEmpty()) {
            throw new CategoriaNoEncontrada(
                    "No se encontraron subcategorías para la categoría padre con ID " + parentId + ".");
        }

        return subcategorias;

    }

    @Override
    public long countCategorias() {
        return categoriaRepository.count();

    }

    // Método para validar si la categoría padre existe
    private Optional<Categoria> validateParentCategoryExists(Integer parentId) {
        if (parentId != null) {
            Optional<Categoria> parentCategory = categoriaRepository.findById(parentId);
            if (!parentCategory.isPresent()) {
                throw new CategoriaNoEncontrada("La categoría padre con ID " + parentId + " no existe.");
            }
            return parentCategory;
        }
        return Optional.empty();
    }

    // Método para validar si ya existe una categoría con el mismo nombre y padre
    private void validateCategoryDuplicate(String nombre, Integer parentId) {
        if (categoriaRepository.existsByNombreAndParentId(nombre, parentId)) {
            throw new DatoDuplicadoException("Ya existe una categoría con el nombre '" + nombre + "' para ese padre.");
        }

    }

}