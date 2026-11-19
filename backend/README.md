# PokéStore — Fullstack (React + Spring Boot + MySQL)

```
pokestore/
├── docker-compose.yml   ← MySQL 8.4 en el puerto 3307
├── backend/             ← Spring Boot 3.5 · Java 21 · JPA · Spring Security + JWT
└── frontend/            ← React 19 + Vite + TypeScript
```

## 1. Levantar MySQL en el puerto 3307

**Opción A: Docker (la recomendada)**

```bash
docker compose up -d
```

Crea la BD `pokestore` con el usuario `pokestore` / `pokestore123`. Queda en `localhost:3307` y solo se puede conectar desde el mismo PC.

**Opción B: MySQL instalado en el PC (Workbench/XAMPP)**

Si ya tienes un MySQL en 3306, puedes levantar otra instancia en 3307 cambiando `port=3307` en `my.ini`, o usar la que ya tienes. En los dos casos, define las variables de entorno del backend (`DB_USER=root`, `DB_PASSWORD=...`, `DB_PORT=...`). La BD se crea sola gracias a `createDatabaseIfNotExist=true`.

## 2. Backend

```bash
cd backend
mvn spring-boot:run
```

En IntelliJ también se puede abrir `backend/pom.xml` y ejecutar `PokestoreApplication`. Las variables de entorno opcionales están en `backend/.env.example` y se configuran en *Run > Edit Configurations > Environment variables*.

La API queda en `http://localhost:8080/api`. En el primer arranque crea los roles, los usuarios de prueba, 13 tipos y 12 Pokémon.

| Perfil | Email              | Contraseña |
|--------|--------------------|------------|
| ADMIN  | admin@pokestore.cl | Admin1234  |
| USER   | ash@pokemon.com    | Pikachu123 |

### Documentación Swagger

Con el backend corriendo:

- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/v3/api-docs (sirve para importarlo en Postman)

Para probar los endpoints protegidos:
1. Ejecuta `POST /api/auth/login` con *Try it out*. El ejemplo ya trae el admin de prueba.
2. Copia el `token` de la respuesta.
3. Presiona **Authorize 🔒** y pega solo el token, sin la palabra `Bearer`.

En producción, apágalo con `SWAGGER_ENABLED=false`: las rutas de la documentación pasan a dar 404.

### Endpoints

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/api/auth/login` · `/api/auth/register` | Público |
| GET | `/api/auth/me` | Logueado |
| GET | `/api/products` · `/api/products/{id}` | Público |
| POST/PUT/DELETE | `/api/products/**` | ADMIN |
| GET | `/api/categories` · `/api/categories/{id}` | Público |
| POST/PUT/DELETE | `/api/categories/**` | ADMIN |
| GET/POST/PUT/DELETE | `/api/users/**` | ADMIN |
| GET | `/api/roles` | ADMIN |
| POST | `/api/orders` | Logueado |
| GET | `/api/orders/me` | Logueado |
| GET | `/api/orders` | ADMIN |

Probar con curl:

```bash
TOKEN=$(curl -s -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" \
  -d '{"email":"admin@pokestore.cl","password":"Admin1234"}' | jq -r .token)
curl -H "Authorization: Bearer $TOKEN" localhost:8080/api/users
```

## 3. Frontend

```bash
cd frontend
npm install
cp .env.example .env    # VITE_API_URL=http://localhost:8080/api
npm run dev             # http://localhost:5173
```

Pantallas: Inicio, Login, Registro, Carrito y Mis compras. El ADMIN además tiene el panel **/admin** con los mantenedores de Pokémon, Tipos y Usuarios, y la lista de órdenes.

## 🔒 Seguridad: qué está cubierto

| Riesgo | Cómo se cubre |
|--------|---------------|
| Contraseñas legibles | Se guardan con hash **BCrypt**; ningún DTO devuelve la password ni el hash |
| Cualquiera se da rol ADMIN al registrarse | `/register` ignora el rol y siempre crea USER |
| Saltarse el login o el rol desde el front | Cada endpoint se valida en **Spring Security**. `ProtectedRoute` solo controla lo que se ve |
| Token de un usuario eliminado o degradado | El filtro JWT vuelve a buscar al usuario en la BD en cada petición |
| Manipular precios o el total | El cliente solo envía `productId` y `quantity`. Precio y total se calculan en el servidor |
| Comprar más de lo que hay (dos compras a la vez) | Transacción con `SELECT ... FOR UPDATE` sobre el stock |
| Quedarse sin administradores | Un admin no puede borrarse ni quitarse el rol a sí mismo, y no se puede borrar o degradar al último admin |
| Saber qué emails están registrados vía login | Mensaje genérico y tiempo de respuesta parejo (hash de relleno) |
| Filtrar errores internos | `GlobalExceptionHandler` responde JSON sin stack traces ni SQL |
| Datos basura o inyección | Bean Validation (`@Valid`), JPA con parámetros (sin SQL concatenado), color `#RRGGBB` e imágenes solo `https://` |
| Cualquier web llamando a la API | CORS con lista blanca (`CORS_ORIGINS`) |
| BD expuesta en la red de Duoc | Docker publica el 3307 solo en `127.0.0.1` |
| Secretos en el código | Todo se lee de variables de entorno. Si `JWT_SECRET` no está definido, se genera una clave aleatoria al arrancar |

## ⚠️ Pendientes antes de producción (AWS)

- **JWT en `localStorage`**: si hay un XSS, se pueden robar los tokens. React escapa el HTML (no se usa `dangerouslySetInnerHTML`), pero lo ideal es una cookie `HttpOnly` + `SameSite`, o tokens de corta duración con refresh.
- **Fuerza bruta en el login**: no hay rate limiting. Agregar Bucket4j o un WAF/API Gateway en AWS.
- **HTTPS**: obligatorio en producción (ALB/CloudFront con certificado ACM).
- **`ddl-auto: update`**: cambiar a `validate` y usar migraciones con Flyway.
- **Swagger público**: deja expuesto el mapa completo de la API (y muestra las credenciales de prueba en los ejemplos). Usa `SWAGGER_ENABLED=false` en producción.
- **Credenciales de prueba**: cambiar `ADMIN_PASSWORD` y definir `JWT_SECRET` (mínimo 32 caracteres).
- **`allowPublicKeyRetrieval=true&useSSL=false`**: solo para desarrollo local. En RDS se usa SSL.
