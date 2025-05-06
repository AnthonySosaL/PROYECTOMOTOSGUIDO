# Backmoto API - Guía de Pruebas con Postman

## Endpoints disponibles

### 1. Registrar usuario (rol CLIENTE por defecto)
- **Método:** POST
- **URL:** http://localhost:9000/api/usuarios
- **Body (JSON):**
```json
{
  "username": "usuario1",
  "clave": "miclave"
}
```
- **Respuesta exitosa:**
```json
{
  "id": 1,
  "username": "usuario1",
  "clave": "$2a$10$...", // hash
  "rol": {
    "id": 1,
    "nombre": "CLIENTE"
  }
}
```

### 2. Login de usuario (devuelve JWT)
- **Método:** POST
- **URL:** http://localhost:9000/api/usuarios/login
- **Body (JSON):**
```json
{
  "username": "usuario1",
  "clave": "miclave"
}
```
- **Respuesta exitosa:**
```
"eyJhbGciOiJIUzUxMiJ9..." // token JWT
```

### 3. Listar usuarios (requiere JWT)
- **Método:** GET
- **URL:** http://localhost:9000/api/usuarios
- **Headers:**
  - Authorization: Bearer {token}
- **Respuesta:**
```json
[
  {
    "id": 1,
    "username": "usuario1",
    "clave": "$2a$10$...",
    "rol": {
      "id": 1,
      "nombre": "CLIENTE"
    }
  }
]
```

### 4. Obtener usuario por ID (requiere JWT)
- **Método:** GET
- **URL:** http://localhost:9000/api/usuarios/1
- **Headers:**
  - Authorization: Bearer {token}

### 5. Eliminar usuario (requiere JWT)
- **Método:** DELETE
- **URL:** http://localhost:9000/api/usuarios/1
- **Headers:**
  - Authorization: Bearer {token}

---

## Notas
- Para endpoints protegidos, primero haz login y copia el token JWT devuelto.
- En Postman, agrega el header:
  - `Authorization: Bearer {token}`
- Si tienes dudas, revisa los logs de la app o consulta este README.
