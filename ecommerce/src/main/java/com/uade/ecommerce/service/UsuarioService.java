package com.uade.ecommerce.service;

import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    // obtener usuario
    public List<Usuario> getAllUsuarios() {
        return usuarioRepository.findAll();
    }

    public Page<Usuario> getAllUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable);
    }

    // buscar un usuario por ID
    public Optional<Usuario> getUsuarioById(int id) {
        return usuarioRepository.findById(id);
    }

    public Usuario getUsuarioByIdOrThrow(int id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con ID: " + id));
    }

    //  buscar un usuario por nombre de usuario
    public Optional<Usuario> getUsuarioByUsername(String username) {
        return usuarioRepository.findByUsername(username);
    }

    // buscar un usuario por email
    public Optional<Usuario> getUsuarioByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    // buscar usuarios por rol
    public List<Usuario> getUsuariosByRol(String rol) {
        return usuarioRepository.findByRol(rol);
    }

    // crear o actualizar un usuario
    public Usuario createOrUpdateUsuario(Usuario usuario) {
        if (usuario.getId() == 0) {
            //crear nuevo user
            if (usuarioRepository.existsByUsername(usuario.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
            }
            if (usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new IllegalArgumentException("El email ya está en uso.");
            }
            return usuarioRepository.save(usuario);
        } else {
            // Actualizar usuario existente
            Optional<Usuario> existente = usuarioRepository.findById(usuario.getId());
            if (!existente.isPresent()) {
                throw new IllegalArgumentException("Usuario no encontrado con ID: " + usuario.getId());
            }
            Usuario usuarioExistente = existente.get();
            // Validar username/email solo si cambiaron
            if (!usuarioExistente.getUsername().equals(usuario.getUsername())
                    && usuarioRepository.existsByUsername(usuario.getUsername())) {
                throw new IllegalArgumentException("El nombre de usuario ya está en uso.");
            }
            if (!usuarioExistente.getEmail().equals(usuario.getEmail())
                    && usuarioRepository.existsByEmail(usuario.getEmail())) {
                throw new IllegalArgumentException("El email ya está en uso.");
            }
            // Actualizar campos
            usuarioExistente.setUsername(usuario.getUsername());
            usuarioExistente.setEmail(usuario.getEmail());
            usuarioExistente.setPassword(usuario.getPassword());
            usuarioExistente.setNombre(usuario.getNombre());
            usuarioExistente.setApellido(usuario.getApellido());
            usuarioExistente.setRol(usuario.getRol());
            // No actualizar fecha_registro
            return usuarioRepository.save(usuarioExistente);
        }
    }

    // borrar un usuario por ID
    public void deleteUsuarioById(int id) {
        usuarioRepository.deleteById(id);
    }

    // validar si un usuario existe por nombre de usuario
    public boolean existsByUsername(String username) {
        return usuarioRepository.existsByUsername(username);
    }

    // verificar si un usuario existe por email
    public boolean existsByEmail(String email) {
        return usuarioRepository.existsByEmail(email);
    }
}