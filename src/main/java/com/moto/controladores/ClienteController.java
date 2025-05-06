package com.moto.controladores;

import com.moto.modelos.Cliente;
import com.moto.modelos.Usuario;
import com.moto.repositorios.ClienteRepository;
import com.moto.repositorios.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Map;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    private final String jwtSecret = "secreto123";

    // Obtener datos personales del cliente autenticado
    @GetMapping("/me")
    public ResponseEntity<?> getMyData(HttpServletRequest request) {
        String username = getUsernameFromToken(request);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Cliente no encontrado"));
        }
        return ResponseEntity.ok(Map.of("cliente", cliente));
    }

    // Actualizar datos personales del cliente autenticado
    @PutMapping("/me")
    public ResponseEntity<?> updateMyData(@RequestBody Cliente datos, HttpServletRequest request) {
        String username = getUsernameFromToken(request);
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "No autorizado"));
        }
        Usuario usuario = usuarioRepository.findAll().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst().orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Usuario no encontrado"));
        }
        Cliente cliente = clienteRepository.findByUsuarioId(usuario.getId());
        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of("error", "Cliente no encontrado"));
        }
        cliente.setNombre(datos.getNombre());
        cliente.setApellido(datos.getApellido());
        cliente.setCedula(datos.getCedula());
        clienteRepository.save(cliente);
        return ResponseEntity.ok(Map.of("mensaje", "Datos personales actualizados correctamente"));
    }

    // Utilidad para extraer username del token
    private String getUsernameFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.replace("Bearer ", "");
        try {
            Claims claims = Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody();
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
}
