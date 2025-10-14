package com.uade.ecommerce.controller;

import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

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

    // 1. Obtener todos los usuarios (sin password)
    @GetMapping
    public ResponseEntity<List<UsuarioProfileDTO>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioProfileDTO> safeUsuarios = usuarios.stream()
                .map(UsuarioProfileDTO::new)
                .toList();
        return ResponseEntity.ok(safeUsuarios);
    }

    // 2. Obtener un usuario por ID (sin password)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioProfileDTO> getUsuarioById(@PathVariable int id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(u -> ResponseEntity.ok(new UsuarioProfileDTO(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // 3. Crear un nuevo usuario (devuelve sin password)
    @PostMapping
    public ResponseEntity<UsuarioProfileDTO> createUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.createOrUpdateUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioProfileDTO(nuevoUsuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 4. Actualizar un usuario existente (reemplazo total, PUT) (sin password)
    @PutMapping("/{id}")
    public ResponseEntity<UsuarioProfileDTO> updateUsuario(@PathVariable int id, @RequestBody Usuario usuario) {
        try {
            usuario.setId(id); // Asegurarse de que el ID sea el correcto
            Usuario usuarioActualizado = usuarioService.createOrUpdateUsuario(usuario);
            return ResponseEntity.ok(new UsuarioProfileDTO(usuarioActualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // 5. Actualización parcial de usuario (PATCH) (sin password)
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioProfileDTO> patchUsuario(@PathVariable int id, @RequestBody Usuario usuarioPatch) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioById(id);
            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
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
            return ResponseEntity.ok(new UsuarioProfileDTO(usuarioActualizado));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    // 6. Eliminar un usuario por ID
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUsuario(@PathVariable int id) {
        // Asumiendo que el servicio maneja la excepción NotFound si el ID no existe
        usuarioService.deleteUsuarioById(id);
        return ResponseEntity.ok("Usuario eliminado correctamente.");
    }
    
    // 7. verificar si un usuario existe por nombre de usuario
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        boolean exists = usuarioService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    // 8. verificar si un usuario existe por email
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = usuarioService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    // 9. Obtener usuarios por rol (sin password)
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UsuarioProfileDTO>> getUsuariosByRol(@PathVariable String rol) {
        List<Usuario> usuarios = usuarioService.getUsuariosByRol(rol);
        List<UsuarioProfileDTO> safeUsuarios = usuarios.stream()
                .map(UsuarioProfileDTO::new)
                .toList();
        return ResponseEntity.ok(safeUsuarios);
    }
    
    // Todos los demás métodos y DTOs de seguridad/login han sido eliminados por solicitud.
}
