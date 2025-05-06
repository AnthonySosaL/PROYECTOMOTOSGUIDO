package com.moto.controladores;

import com.moto.modelos.*;
import com.moto.repositorios.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import java.util.*;

@RestController
@RequestMapping("/api/carrito")
public class CarritoController {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private CarritoRepository carritoRepository;
    @Autowired
    private DetalleCarritoRepository detalleCarritoRepository;
    @Autowired
    private ProductoRepository productoRepository;

    private final String jwtSecret = "secreto123";

    // Obtener el carrito y sus productos
    @GetMapping("/me")
    public ResponseEntity<?> getMyCarrito(HttpServletRequest request) {
        Cliente cliente = getClienteFromToken(request);
        if (cliente == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        Carrito carrito = carritoRepository.findByClienteId(cliente.getId());
        if (carrito == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        }
        List<DetalleCarrito> detalles = detalleCarritoRepository.findByCarritoId(carrito.getId());
        return ResponseEntity.ok(Map.of("carrito", carrito, "productos", detalles));
    }

    // Añadir producto al carrito
    @PostMapping("/productos")
    public ResponseEntity<?> addProducto(@RequestBody Map<String, Object> body, HttpServletRequest request) {
        Cliente cliente = getClienteFromToken(request);
        if (cliente == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        Carrito carrito = carritoRepository.findByClienteId(cliente.getId());
        if (carrito == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        }
        Long idProducto = Long.valueOf(body.get("idProducto").toString());
        Integer cantidad = Integer.valueOf(body.get("cantidad").toString());
        Producto producto = productoRepository.findById(idProducto).orElse(null);
        if (producto == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
        }
        List<DetalleCarrito> detalles = detalleCarritoRepository.findByCarritoId(carrito.getId());
        Optional<DetalleCarrito> existente = detalles.stream().filter(d -> d.getProducto().getId().equals(idProducto)).findFirst();
        if (existente.isPresent()) {
            DetalleCarrito detalle = existente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
            detalleCarritoRepository.save(detalle);
        } else {
            DetalleCarrito detalle = new DetalleCarrito();
            detalle.setCarrito(carrito);
            detalle.setProducto(producto);
            detalle.setCantidad(cantidad);
            detalleCarritoRepository.save(detalle);
        }
        return ResponseEntity.ok(Map.of("mensaje", "Producto añadido al carrito"));
    }

    // Vaciar el carrito
    @DeleteMapping("/productos")
    public ResponseEntity<?> vaciarCarrito(HttpServletRequest request) {
        Cliente cliente = getClienteFromToken(request);
        if (cliente == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        Carrito carrito = carritoRepository.findByClienteId(cliente.getId());
        if (carrito == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        }
        detalleCarritoRepository.deleteByCarritoId(carrito.getId());
        return ResponseEntity.ok(Map.of("mensaje", "Carrito vaciado correctamente"));
    }

    // Simular pago y vaciar carrito
    @PostMapping("/pagar")
    public ResponseEntity<?> pagar(HttpServletRequest request) {
        Cliente cliente = getClienteFromToken(request);
        if (cliente == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        Carrito carrito = carritoRepository.findByClienteId(cliente.getId());
        if (carrito == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        }
        detalleCarritoRepository.deleteByCarritoId(carrito.getId());
        return ResponseEntity.ok(Map.of("mensaje", "Pago realizado y carrito vaciado"));
    }

    // Utilidad para extraer el cliente desde el token
    private Cliente getClienteFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            String username = claims.getSubject();
            Usuario usuario = clienteRepository.findAll().stream()
                .map(Cliente::getUsuario)
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
            if (usuario == null) return null;
            return clienteRepository.findByUsuarioId(usuario.getId());
        } catch (Exception e) {
            return null;
        }
    }
}
