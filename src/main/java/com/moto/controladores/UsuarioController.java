package com.moto.controladores;

import com.moto.modelos.Usuario;
import com.moto.modelos.Rol;
import com.moto.repositorios.UsuarioRepository;
import com.moto.repositorios.RolRepository;
import com.moto.dtos.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;

    private final String jwtSecret = "secreto123";
    private final long jwtExpirationMs = 86400000; // 1 día

    @GetMapping
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            return ResponseEntity.badRequest().body("El nombre de usuario ya existe");
        }
        // Asignar rol CLIENTE por defecto
        Rol rolCliente = rolRepository.findByNombre("CLIENTE");
        if (rolCliente == null) {
            rolCliente = new Rol();
            rolCliente.setNombre("CLIENTE");
            rolCliente = rolRepository.save(rolCliente);
        }
        usuario.setRol(rolCliente);
        // Hashear la clave antes de guardar
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setClave(encoder.encode(usuario.getClave()));
        usuarioRepository.save(usuario);
        return ResponseEntity.ok("USUARIO CREADO EXITOSAMENTE. TIPO: CLIENTE");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(loginRequest.getUsername()))
                .findFirst().orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(401).body("Usuario o clave incorrectos");
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(loginRequest.getClave(), usuario.getClave())) {
            return ResponseEntity.status(401).body("Usuario o clave incorrectos");
        }
        // Generar JWT
        String token = Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("rol", usuario.getRol().getNombre())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
        return ResponseEntity.ok(token);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id) {
        return usuarioRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable Long id) {
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
