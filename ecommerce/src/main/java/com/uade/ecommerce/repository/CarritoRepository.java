package com.uade.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.uade.ecommerce.entity.Carrito;
import com.uade.ecommerce.entity.EstadoCarrito;
import com.uade.ecommerce.entity.Usuario;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface CarritoRepository extends JpaRepository<Carrito, Integer> {

    // Buscar carrito por usuario
    Optional<Carrito> findByUsuario(Usuario usuario);

    // Buscar carrito por usuario y estado ("ACTIVO", "VACIO")
    Optional<Carrito> findByUsuarioIdAndEstado(int usuarioId, EstadoCarrito estado);

    @Query("SELECT c FROM Carrito c LEFT JOIN FETCH c.itemsCarrito WHERE c.usuario.id = :usuarioId AND c.estado = :estado")
    Optional<Carrito> findByUsuarioIdAndEstadoConItems(@Param("usuarioId") int usuarioId,
            @Param("estado") EstadoCarrito estado);

    List<Carrito> findByEstadoAndFechaActivacionBefore(EstadoCarrito estado, LocalDateTime fechaLimite);

}
