package com.uade.ecommerce.repository;

import com.uade.ecommerce.entity.Direccion;
import com.uade.ecommerce.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DireccionRepository extends JpaRepository<Direccion, Integer> {
    List<Direccion> findByUsuario(Usuario usuario);
}
