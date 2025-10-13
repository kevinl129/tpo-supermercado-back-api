package com.uade.ecommerce.controller;

import com.uade.ecommerce.controller.PasswordChangeRequest;
import com.uade.ecommerce.controller.LoginRequest;
import com.uade.ecommerce.controller.UsuarioLoginResponse;
import com.uade.ecommerce.controller.LoginJwtResponse;
import com.uade.ecommerce.entity.Usuario;
import com.uade.ecommerce.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    // DTO para exponer solo datos seguros
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

    // Obtener todos los usuarios (sin password)
    @GetMapping
    public ResponseEntity<List<UsuarioProfileDTO>> getAllUsuarios() {
        List<Usuario> usuarios = usuarioService.getAllUsuarios();
        List<UsuarioProfileDTO> safeUsuarios = usuarios.stream()
                .map(UsuarioProfileDTO::new)
                .toList();
        return ResponseEntity.ok(safeUsuarios);
    }

    // Obtener un usuario por ID (sin password)
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioProfileDTO> getUsuarioById(@PathVariable int id) {
        Optional<Usuario> usuario = usuarioService.getUsuarioById(id);
        return usuario.map(u -> ResponseEntity.ok(new UsuarioProfileDTO(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // Crear un nuevo usuario (devuelve sin password)
    @PostMapping
    public ResponseEntity<UsuarioProfileDTO> createUsuario(@RequestBody Usuario usuario) {
        try {
            Usuario nuevoUsuario = usuarioService.createOrUpdateUsuario(usuario);
            return ResponseEntity.status(HttpStatus.CREATED).body(new UsuarioProfileDTO(nuevoUsuario));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // Actualizar un usuario existente (reemplazo total, PUT) (sin password)
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

    // Actualización parcial de usuario (PATCH) (sin password)
    @PatchMapping("/{id}")
    public ResponseEntity<UsuarioProfileDTO> patchUsuario(@PathVariable int id, @RequestBody Usuario usuarioPatch) {
        try {
            Optional<Usuario> usuarioOpt = usuarioService.getUsuarioById(id);
            if (!usuarioOpt.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            Usuario usuarioExistente = usuarioOpt.get();
            // Solo actualiza los campos que vienen en el PATCH (no null)
            if (usuarioPatch.getUsername() != null)
                usuarioExistente.setUsername(usuarioPatch.getUsername());
            if (usuarioPatch.getEmail() != null)
                usuarioExistente.setEmail(usuarioPatch.getEmail());
            if (usuarioPatch.getPassword() != null)
                usuarioExistente.setPassword(usuarioPatch.getPassword());
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

    // cambiar la contrasena de un usuario no auteticador por email
    @PutMapping("/cambiar-password")
    public ResponseEntity<String> cambiarPasswordPorEmail(@RequestParam String email,
            @RequestBody PasswordChangeRequest passwordChangeRequest) {
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByEmail(email);
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
        Usuario usuario = usuarioOpt.get();
        usuario.setPassword(passwordChangeRequest.getNuevaContrasena());
        usuarioService.createOrUpdateUsuario(usuario);
        return ResponseEntity.ok("La contraseña fue actualizada correctamente.");
    }

    // Cambiar la contraseña del usuario autenticado
    @PutMapping("/password")
    public ResponseEntity<String> cambiarPassword(@RequestBody PasswordChangeRequest passwordChangeRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByUsername(username);
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Usuario no encontrado.");
        }
        Usuario usuario = usuarioOpt.get();
        // Verifica la contraseña actual
        if (!usuario.getPassword().equals(passwordChangeRequest.getContrasenaActual())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("La contraseña actual es incorrecta.");
        }
        usuario.setPassword(passwordChangeRequest.getNuevaContrasena());
        usuarioService.createOrUpdateUsuario(usuario);
        return ResponseEntity.ok("La contraseña fue actualizada correctamente.");
    }

    // borrar un usuario por ID (devuelve mensaje de confirmacion)
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUsuario(@PathVariable int id) {
        usuarioService.deleteUsuarioById(id);
        return ResponseEntity.ok("Usuario eliminado correctamente.");
    }

    // verificar si un usuario existe por nombre de usuario
    @GetMapping("/exists/username/{username}")
    public ResponseEntity<Boolean> existsByUsername(@PathVariable String username) {
        boolean exists = usuarioService.existsByUsername(username);
        return ResponseEntity.ok(exists);
    }

    // verificar si un usuario existe por email
    @GetMapping("/exists/email/{email}")
    public ResponseEntity<Boolean> existsByEmail(@PathVariable String email) {
        boolean exists = usuarioService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    // Obtener usuarios por rol (sin password)
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<UsuarioProfileDTO>> getUsuariosByRol(@PathVariable String rol) {
        List<Usuario> usuarios = usuarioService.getUsuariosByRol(rol);
        List<UsuarioProfileDTO> safeUsuarios = usuarios.stream()
                .map(UsuarioProfileDTO::new)
                .toList();
        return ResponseEntity.ok(safeUsuarios);
    }

    // obtener el usuario autenticado (perfil propio) - SIN password
    @GetMapping("/me")
    public ResponseEntity<UsuarioProfileDTO> getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Optional<Usuario> usuarioOpt = usuarioService.getUsuarioByUsername(username);
        return usuarioOpt.map(u -> ResponseEntity.ok(new UsuarioProfileDTO(u)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    // endpoint de login (autenticacion de usuario)
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Optional<Usuario> usuarioOpt = loginRequest.getUsername() != null
                ? usuarioService.getUsuarioByUsername(loginRequest.getUsername())
                : usuarioService.getUsuarioByEmail(loginRequest.getEmail());
        if (!usuarioOpt.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos.");
        }
        Usuario usuario = usuarioOpt.get();
        if (!usuario.getPassword().equals(loginRequest.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuario o contraseña incorrectos.");
        }
        // Generar JWT
        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getRol());
        UsuarioLoginResponse response = new UsuarioLoginResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getEmail(),
                usuario.getNombre(),
                usuario.getApellido(),
                usuario.getRol());
        return ResponseEntity.ok(new LoginJwtResponse(token, response));
    }
}