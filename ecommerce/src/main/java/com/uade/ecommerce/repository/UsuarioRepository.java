package com.uade.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.uade.ecommerce.entity.Usuario;
import java.util.List;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    Optional<Usuario> findById(int id); // busco un usuario por id
    Optional<Usuario> findByUsername(String username); // busco un usuario por su nombre de usuario
    Optional<Usuario> findByEmail(String email); // busco un usuario por mail
    List<Usuario> findByRol(String rol); // busco usuarios por rol
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

}