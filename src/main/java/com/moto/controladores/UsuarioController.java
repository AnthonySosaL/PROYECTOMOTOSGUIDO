package com.moto.controladores;

import com.moto.modelos.Usuario;
import com.moto.modelos.Rol;
import com.moto.modelos.Cliente;
import com.moto.modelos.Carrito;
import com.moto.repositorios.UsuarioRepository;
import com.moto.repositorios.RolRepository;
import com.moto.repositorios.ClienteRepository;
import com.moto.repositorios.CarritoRepository;
import com.moto.repositorios.DetalleCarritoRepository;
import com.moto.repositorios.ProductoRepository;
import com.moto.dtos.LoginRequest;
import com.moto.dtos.UsuarioResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CarritoRepository carritoRepository;
    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;
    @Autowired
    private ProductoRepository productoRepository;

    @PersistenceContext
    private EntityManager entityManager;

    private final String jwtSecret = "secreto123";
    private final long jwtExpirationMs = 86400000; // 1 día

    @GetMapping
    public ResponseEntity<?> listarUsuarios(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        String token = authHeader.replace("Bearer ", "");
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }
        String rol = (String) claims.get("rol");
        if (!"ADMIN".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado: solo ADMIN puede ver la lista de usuarios"));
        }
        List<UsuarioResponse> usuarios = usuarioRepository.findAll().stream()
            .map(u -> new UsuarioResponse(u.getId(), u.getUsername(), u.getRol().getNombre()))
            .toList();
        return ResponseEntity.ok(Map.of("usuarios", usuarios));
    }

    @PostMapping
    public ResponseEntity<?> crearUsuario(@RequestBody Usuario usuario) {
        if (usuarioRepository.existsByUsername(usuario.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "El nombre de usuario ya existe"));
        }
        Rol rolCliente = rolRepository.findByNombre("CLIENTE");
        if (rolCliente == null) {
            rolCliente = new Rol();
            rolCliente.setNombre("CLIENTE");
            rolCliente = rolRepository.save(rolCliente);
        }
        usuario.setRol(rolCliente);
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setClave(encoder.encode(usuario.getClave()));
        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuarioGuardado);
        clienteRepository.save(cliente);
        // Crear carrito vacío para el cliente
        Carrito carrito = new Carrito();
        carrito.setCliente(cliente);
        carritoRepository.save(carrito);
        return ResponseEntity.ok(Map.of("mensaje", "USUARIO CREADO EXITOSAMENTE. TIPO: CLIENTE"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(loginRequest.getUsername()))
                .findFirst().orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario o clave incorrectos"));
        }
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(loginRequest.getClave(), usuario.getClave())) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario o clave incorrectos"));
        }
        // Generar JWT
        String token = Jwts.builder()
                .setSubject(usuario.getUsername())
                .claim("rol", usuario.getRol().getNombre())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
        return ResponseEntity.ok(Map.of("token", token));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerUsuario(@PathVariable Long id, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        String token = authHeader.replace("Bearer ", "");
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }
        String rol = (String) claims.get("rol");
        if (!"ADMIN".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado: solo ADMIN puede ver usuarios"));
        }
        return usuarioRepository.findById(id)
                .map(u -> ResponseEntity.ok(Map.of("usuario", (Object) new UsuarioResponse(u.getId(), u.getUsername(), u.getRol().getNombre()))))
                .orElse(ResponseEntity.status(404).body(Map.of("error", (Object) "Usuario no encontrado")));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarUsuario(@PathVariable Long id, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        String token = authHeader.replace("Bearer ", "");
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido"));
        }
        String rol = (String) claims.get("rol");
        if (!"ADMIN".equals(rol)) {
            return ResponseEntity.status(403).body(Map.of("error", "Acceso denegado: solo ADMIN puede eliminar usuarios"));
        }
        if (!usuarioRepository.existsById(id)) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        usuarioRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/borrar-todo")
    public ResponseEntity<?> borrarTodo() {
        // Borrar todo
        detalleCarritoRepository.deleteAll();
        carritoRepository.deleteAll();
        clienteRepository.deleteAll();
        usuarioRepository.deleteAll();
        rolRepository.deleteAll();
        productoRepository.deleteAll();
        // Resetear autoincremento (PostgreSQL)
        try {
            entityManager.createNativeQuery("ALTER SEQUENCE detalle_carrito_id_seq RESTART WITH 1").executeUpdate();
            entityManager.createNativeQuery("ALTER SEQUENCE carritos_id_seq RESTART WITH 1").executeUpdate();
            entityManager.createNativeQuery("ALTER SEQUENCE clientes_id_seq RESTART WITH 1").executeUpdate();
            entityManager.createNativeQuery("ALTER SEQUENCE usuarios_id_seq RESTART WITH 1").executeUpdate();
            entityManager.createNativeQuery("ALTER SEQUENCE roles_id_seq RESTART WITH 1").executeUpdate();
            entityManager.createNativeQuery("ALTER SEQUENCE productos_id_seq RESTART WITH 1").executeUpdate();
        } catch (Exception e) {
            // Ignorar si no existe la secuencia (por ejemplo, en H2)
        }
        return ResponseEntity.ok(Map.of("mensaje", "Todos los datos han sido eliminados y los IDs reseteados"));
    }
}
