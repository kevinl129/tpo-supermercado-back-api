package com.uade.ecommerce.repository;

import com.uade.ecommerce.entity.Orden;
import com.uade.ecommerce.entity.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OrdenRepository extends JpaRepository<Orden, Integer> {
    @Query("SELECT o FROM Orden o WHERE o.id = :id AND o.usuario.id = :usuarioId")
    Optional<Orden> buscarOrdenDeUsuario(@Param("id") int id, @Param("usuarioId") int usuarioId);

    List<Orden> findByUsuario(Usuario usuario);

}
