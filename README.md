# Backmoto API – Documentación y Flujo de Pruebas

## ⚠️ Importante
- Este proyecto es solo para desarrollo y pruebas.
- El endpoint `/api/usuarios/borrar-todo` elimina todos los datos y resetea los IDs. ¡No lo expongas en producción!

---

## Usuarios y Roles
- Al iniciar el proyecto, se crea automáticamente un usuario admin:
  - **Usuario:** `admin`
  - **Contraseña:** `123`
- Roles posibles: `ADMIN`, `VENDEDOR`, `CLIENTE`
- Solo `ADMIN` y `VENDEDOR` pueden crear productos.

---

## Endpoints principales

### 1. Borrar toda la base de datos (solo para pruebas)
- **DELETE** `/api/usuarios/borrar-todo`
- Respuesta:
  ```json
  { "mensaje": "Todos los datos han sido eliminados y los IDs reseteados" }
  ```

---

### 2. Registro y login

#### Registrar usuario (rol CLIENTE, crea cliente y carrito vacío)
- **POST** `/api/usuarios`
- Body:
  ```json
  { "username": "usuario1", "clave": "miclave" }
  ```
- Respuesta:
  ```json
  { "mensaje": "USUARIO CREADO EXITOSAMENTE. TIPO: CLIENTE" }
  ```

#### Login (devuelve JWT)
- **POST** `/api/usuarios/login`
- Body:
  ```json
  { "username": "usuario1", "clave": "miclave" }
  ```
- Respuesta:
  ```json
  { "token": "..." }
  ```

---

### 3. Gestión de datos personales (cliente)

#### Consultar datos personales
- **GET** `/api/clientes/me`
- Header: `Authorization: Bearer {token}`
- Respuesta:
  ```json
  { "cliente": { "id": 1, "nombre": "...", "apellido": "...", "cedula": "...", "usuario": { ... } } }
  ```

#### Actualizar datos personales
- **PUT** `/api/clientes/me`
- Header: `Authorization: Bearer {token}`
- Body:
  ```json
  { "nombre": "Juan", "apellido": "Pérez", "cedula": "1234567890" }
  ```
- Respuesta:
  ```json
  { "mensaje": "Datos personales actualizados correctamente" }
  ```

---

### 4. Gestión de productos

#### Listar productos (público)
- **GET** `/api/productos`
- Respuesta:
  ```json
  { "productos": [ { "id": 1, "nombre": "...", "precio": 100.0, "stock": 10, "imagenUrl": "https://..." }, ... ] }
  ```

#### Obtener producto por ID (público)
- **GET** `/api/productos/{id}`
- Respuesta:
  ```json
  { "producto": { "id": 1, "nombre": "...", "precio": 100.0, "stock": 10, "imagenUrl": "https://..." } }
  ```

#### Crear producto (solo ADMIN o VENDEDOR)
- **POST** `/api/productos`
- Header: `Authorization: Bearer {token_admin_o_vendedor}`
- Body:
  ```json
  { "nombre": "Producto X", "precio": 100.0, "stock": 10, "imagenUrl": "https://..." }
  ```
- Respuesta:
  ```json
  { "mensaje": "Producto creado correctamente" }
  ```

---

### 5. Carrito de compras

#### Consultar carrito y productos
- **GET** `/api/carrito/me`
- Header: `Authorization: Bearer {token}`
- Respuesta:
  ```json
  {
    "carrito": { "id": 1, "cliente": { ... } },
    "productos": [
      { "id": 1, "carrito": { ... }, "producto": { ... }, "cantidad": 2 }
    ]
  }
  ```

#### Añadir producto al carrito
- **POST** `/api/carrito/productos`
- Header: `Authorization: Bearer {token}`
- Body:
  ```json
  { "idProducto": 1, "cantidad": 2 }
  ```
- Respuesta:
  ```json
  { "mensaje": "Producto añadido al carrito" }
  ```

#### Vaciar carrito
- **DELETE** `/api/carrito/productos`
- Header: `Authorization: Bearer {token}`
- Respuesta:
  ```json
  { "mensaje": "Carrito vaciado correctamente" }
  ```

#### Simular pago (vacía el carrito)
- **POST** `/api/carrito/pagar`
- Header: `Authorization: Bearer {token}`
- Respuesta:
  ```json
  { "mensaje": "Pago realizado y carrito vaciado" }
  ```

---

## Notas de seguridad y pruebas

- Todas las respuestas son JSON.
- Solo ADMIN y VENDEDOR pueden crear productos.
- El usuario admin se crea automáticamente al iniciar el proyecto.
- El endpoint de borrado masivo elimina y resetea todo (usuarios, roles, clientes, productos, carritos, detalle_carrito).
- Los productos ahora incluyen stock e imagenUrl.
- No expongas el endpoint de borrado masivo en producción.
