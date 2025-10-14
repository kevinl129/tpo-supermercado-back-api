package com.uade.ecommerce.controller;

import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.RequiredArgsConstructor; // Importamos Lombok para la inyección por constructor

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor // Genera el constructor para la inyección de UsuarioService
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    //DTO datos del user
    public static class UsuarioProfileDTO {
        public int id;
        public String username;
        public String email;
        public String nombre;
        public String apellido;
        public String rol;
        public java.time.LocalDateTime fecha_registro;

        public UsuarioProfileDTO(Usuario u) {
            this.id = u.getId();
            this.username = u.getUsername();
            this.email = u.getEmail();
            this.nombre = u.getNombre();
            this.apellido = u.getApellido();
            this.rol = u.getRol();
            this.fecha_registro = u.getFecha_registro();
        }
    }

      // Clase auxiliar para devolver mensajes junto con los datos
    public static class ApiResponse<T> {
        public String mensaje;
        public T data;

        public ApiResponse(String mensaje, T data) {
            this.mensaje = mensaje;
            this.data = data;
        }
    }

    // 1. Obtener todos los usuarios (sin password)
    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioProfileDTO>>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioProfileDTO> safeUsuarios = usuarios.stream()
                .map(UsuarioProfileDTO::new)
                .toList();
        String msg = safeUsuarios.isEmpty()
                ? "⚠️ No se encontraron usuarios registrados."
                : "✅ Lista de usuarios obtenida correctamente (" + safeUsuarios.size() + " usuarios).";

        return ResponseEntity.ok(new ApiResponse<>(msg, safeUsuarios));
    }

    // 2. Obtener un usuario por ID (sin password)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioProfileDTO>> getUsuarioById(@PathVariable int id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
         if (usuario.isPresent()) {
            return ResponseEntity.ok(new ApiResponse<>("✅ Usuario encontrado correctamente.", new UsuarioProfileDTO(usuario.get())));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("⚠️ No se encontró el usuario con ID: " + id, null));
        }
    }

    // 3. Crear un nuevo usuario (devuelve sin password)
    @PostMapping
    public ResponseEntity<ApiResponse<UsuarioProfileDTO>> createUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.createOrUpdateUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>("✅ Usuario creado correctamente.", new UsuarioProfileDTO(nuevoUsuario)));        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("❌ Error al crear usuario: " + e.getMessage(), null));
        }
    }

    // 4. Actualizar un usuario existente (reemplazo total, PUT) (sin password)
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioProfileDTO>> updateUsuario(@PathVariable int id, @RequestBody Usuario usuario) {
        try {
            usuario.setId(id); // Asegurarse de que el ID sea el correcto
            Usuario usuarioActualizado = usuarioService.createOrUpdateUsuario(usuario);
            return ResponseEntity.ok(new ApiResponse<>("✅ Usuario actualizado correctamente.", new UsuarioProfileDTO(usuarioActualizado)));       
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("❌ Error al actualizar usuario: " + e.getMessage(), null));        }
    }

    // 5. Actualización parcial de usuario (PATCH) (sin password)
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioProfileDTO>> patchUsuario(@PathVariable int id, @RequestBody Usuario usuarioPatch) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioById(id);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>("⚠️ No se encontró el usuario con ID: " + id, null));
            }
            Usuario usuarioExistente = usuarioOpt.get();
            
            // Solo actualiza los campos que vienen en el PATCH (no null)
            if (usuarioPatch.getUsername() != null)
                usuarioExistente.setUsername(usuarioPatch.getUsername());
            if (usuarioPatch.getEmail() != null)
                usuarioExistente.setEmail(usuarioPatch.getEmail());
            if (usuarioPatch.getPassword() != null)
                usuarioExistente.setPassword(usuarioPatch.getPassword()); // Real: debería ser hasheada aquí o en el servicio
            if (usuarioPatch.getNombre() != null)
                usuarioExistente.setNombre(usuarioPatch.getNombre());
            if (usuarioPatch.getApellido() != null)
                usuarioExistente.setApellido(usuarioPatch.getApellido());
            if (usuarioPatch.getRol() != null)
                usuarioExistente.setRol(usuarioPatch.getRol());
                
            Usuario usuarioActualizado = usuarioService.createOrUpdateUsuario(usuarioExistente);
            return ResponseEntity.ok(new ApiResponse<>("✅ Usuario actualizado parcialmente.", new UsuarioProfileDTO(usuarioActualizado)));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>("❌ Error al actualizar usuario parcialmente: " + e.getMessage(), null));
        }
    }
    
    // 6. Eliminar un usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteUsuario(@PathVariable int id) {
        // Asumiendo que el servicio maneja la excepción NotFound si el ID no existe
         try {
            usuarioService.deleteUsuarioById(id);
            return ResponseEntity.ok(new ApiResponse<>("🗑️ Usuario eliminado correctamente.", "ID eliminado: " + id));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>("⚠️ No se pudo eliminar: usuario con ID " + id + " no encontrado.", null));
        }
    }
    
    // 7. verificar si un usuario existe por nombre de usuario
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<ApiResponse<Boolean>> existsByUsername(@PathVariable String username) {
        boolean exists = usuarioService.existsByUsername(username);
        String msg = exists
                ? "✅ El nombre de usuario '" + username + "' ya existe."
                : "ℹ️ El nombre de usuario '" + username + "' está disponible.";
        return ResponseEntity.ok(new ApiResponse<>(msg, exists));
    }

    // 8. verificar si un usuario existe por email
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<ApiResponse<Boolean>> existsByEmail(@PathVariable String email) {
        boolean exists = usuarioService.existsByEmail(email);
         String msg = exists
                ? "✅ El email '" + email + "' ya está registrado."
                : "ℹ️ El email '" + email + "' está disponible.";
        return ResponseEntity.ok(new ApiResponse<>(msg, exists));
    }

    // 9. Obtener usuarios por rol (sin password)
    @GetMapping("/rol/{rol}")
    public ResponseEntity<ApiResponse<List<UsuarioProfileDTO>>> getUsuariosByRol(@PathVariable String rol) {
        List<Usuario> usuarios = usuarioService.getUsuariosByRol(rol);
        List<UsuarioProfileDTO> safeUsuarios = usuarios.stream()
                .map(UsuarioProfileDTO::new)
                .toList();
         String msg = safeUsuarios.isEmpty()
                ? "⚠️ No hay usuarios con el rol '" + rol + "'."
                : "✅ Usuarios con rol '" + rol + "' obtenidos correctamente.";

        return ResponseEntity.ok(new ApiResponse<>(msg, safeUsuarios));
    }
    
    // Todos los demás métodos y DTOs de seguridad/login han sido eliminados por solicitud.
}
