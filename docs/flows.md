# Flujos del Orders API

Este documento describe los flujos principales del servicio, con enfasis en autenticacion, gestion de pedidos y auditoria de eventos.

## Flujo de autenticacion

### Login

1. Cliente envia `POST /auth/login` con `email` y `password`.
2. `AuthController` delega en `AuthenticationManager`.
3. `UserDetailsServiceImpl` obtiene el usuario desde `UserRepository`.
4. Si las credenciales son validas, `JwtUtil` genera `accessToken` y `refreshToken`.
5. Respuesta con `AuthResponse` (tokens).

### Refresh token

1. Cliente envia `POST /auth/refresh` con `refreshToken`.
2. `JwtUtil` extrae el `username` del token.
3. `UserDetailsServiceImpl` carga el usuario.
4. `JwtUtil` valida el refresh token y genera nuevos tokens.
5. Respuesta con nuevos tokens o `400` si es invalido.

### Registro (admin)

1. Cliente envia `POST /auth/register` con `email` y `password`.
2. `AuthController` crea usuario con rol `OPERATOR` y guarda en `UserRepository`.
3. Respuesta con el usuario creado.

## Flujo de autorizacion (JWT)

1. El cliente incluye `Authorization: Bearer <token>`.
2. `JwtAuthenticationFilter` extrae el token y obtiene `username`.
3. `UserDetailsServiceImpl` carga el usuario.
4. Si el token es valido, se establece autenticacion en el `SecurityContext`.
5. `SecurityConfig` controla acceso por rol en `/orders/**` y `/me`.

## Flujo de pedidos

### Crear pedido

1. Cliente envia `POST /orders` con `OrderDto`.
2. `OrderService` convierte a entidad `Order` y asigna estado `RECIBIDO`.
3. Se guarda el pedido en `OrderRepository`.
4. Se registra un `OrderEvent` con `fromStatus=null` y `toStatus=RECIBIDO`.
5. Respuesta con `OrderDto` creado.

### Consultar pedidos con filtros

1. Cliente envia `GET /orders` con filtros opcionales.
2. `OrderRepository.findWithFilters` aplica filtros de estado, nombre, telefono y fechas.
3. `OrderService` convierte a `OrderDto` y devuelve una pagina.

### Consultar pedido por ID

1. Cliente envia `GET /orders/{id}`.
2. `OrderRepository.findById` busca el pedido.
3. `OrderService` convierte a `OrderDto` o retorna `404`.

### Actualizar estado

1. Cliente envia `PATCH /orders/{id}/status` con `{ "to": "NUEVO_ESTADO", "notes": "..." }`.
2. `OrderService` valida transicion con `isValidTransition`.
3. Se actualiza el estado y se guarda.
4. Se registra `OrderEvent` con `fromStatus` y `toStatus`.
5. Respuesta con pedido actualizado.

#### Transiciones validas

- `RECIBIDO` -> `PREPARANDO`
- `PREPARANDO` -> `LISTO`
- `LISTO` -> `DESPACHADO`
- `DESPACHADO` -> `ENTREGADO`
- Cualquier estado -> `CANCELADO`
- Mismo estado -> mismo estado es valido

### Actualizar datos del pedido

1. Cliente envia `PATCH /orders/{id}` con datos actualizables.
2. `OrderService` actualiza `address`, `notes`, `paymentMethod`.
3. Se guarda y retorna el pedido actualizado.

### Linea de tiempo (eventos)

1. Cliente envia `GET /orders/{id}/timeline`.
2. `OrderEventRepository` devuelve eventos ordenados por `createdAt`.
3. Respuesta con lista de eventos.

## Flujo de usuario actual

1. Cliente envia `GET /me`.
2. `SecurityContext` aporta el email del usuario autenticado.
3. `UserRepository.findByEmail` obtiene el usuario.
4. Respuesta con el usuario actual.

## Notas de auditoria

- `OrderEvent` guarda `fromStatus`, `toStatus`, `notes` y `createdBy`.
- `createdBy` depende de la autenticacion presente en el request.
