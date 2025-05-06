package com.moto.controladores;

import com.moto.modelos.Producto;
import com.moto.repositorios.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    @Autowired
    private ProductoRepository productoRepository;
    private final String jwtSecret = "secreto123";

    // Listar productos (público)
    @GetMapping
    public ResponseEntity<?> listarProductos() {
        List<Producto> productos = productoRepository.findAll();
        return ResponseEntity.ok(Map.of("productos", productos));
    }

    // Obtener producto por ID (público)
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        return productoRepository.findById(id)
            .map(producto -> ResponseEntity.ok(Map.of("producto", (Object) producto)))
            .orElse(ResponseEntity.status(404).body(Map.of("error", (Object) "Producto no encontrado")));
    }

    // Crear producto (solo ADMIN o VENDEDOR)
    @PostMapping
    public ResponseEntity<?> crearProducto(@RequestBody Producto producto, HttpServletRequest request) {
        String rol = getRolFromToken(request);
        if (!("ADMIN".equals(rol) || "VENDEDOR".equals(rol))) {
            return ResponseEntity.status(403).body(Map.of("error", "Solo ADMIN o VENDEDOR pueden crear productos"));
        }
        if (productoRepository.findByNombre(producto.getNombre()) != null) {
            return ResponseEntity.badRequest().body(Map.of("error", "El producto ya existe"));
        }
        productoRepository.save(producto);
        return ResponseEntity.ok(Map.of("mensaje", "Producto creado correctamente"));
    }

    // Utilidad para extraer el rol desde el token
    private String getRolFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            return (String) claims.get("rol");
        } catch (Exception e) {
            return null;
        }
    }
}
