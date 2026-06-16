# Módulo de Autenticación (Login + Roles) — CuidaRed

**Equipo 7 · Ingeniería de Software MAR–JUL 2026 · UCAB**

Este documento explica, parte por parte, el módulo de inicio de sesión: qué hace cada
archivo, cómo se conectan y por qué se tomaron las decisiones. Está pensado para
entenderlo y poder defenderlo.

---

## Índice

1. [Qué resuelve el módulo](#1-qué-resuelve-el-módulo)
2. [Visión general del flujo](#2-visión-general-del-flujo)
3. [Conceptos clave: ¿qué es un JWT?](#3-conceptos-clave-qué-es-un-jwt)
4. [Backend — parte por parte](#4-backend--parte-por-parte)
5. [Frontend — parte por parte](#5-frontend--parte-por-parte)
6. [Recorrido completo de una sesión](#6-recorrido-completo-de-una-sesión)
7. [Decisiones de diseño y sus trade-offs](#7-decisiones-de-diseño-y-sus-trade-offs)
8. [Cómo probarlo (demo)](#8-cómo-probarlo-demo)
9. [Preguntas frecuentes de defensa](#9-preguntas-frecuentes-de-defensa)

---

## 1. Qué resuelve el módulo

Antes no había login: el "acceso" era pegar un ID a mano. Ahora:

1. Al entrar, la app muestra una **landing page pública** con botones de **Iniciar sesión** y
   **Registrarse** arriba a la derecha.
2. El usuario inicia sesión con **correo + contraseña**.
3. El backend valida y, si es correcto, emite un **token JWT** que incluye el **rol** del usuario.
4. El frontend guarda la sesión y **redirige según el rol**: cuidador → su dashboard,
   padre/tutor → el suyo.
5. Las rutas y la navegación quedan **protegidas por rol**; si alguien intenta entrar a una
   ruta sin sesión se le **avisa** y se le redirige al login, y las llamadas al backend viajan
   con el token (el servidor rechaza con 401 las que no lo tengan).

Roles del sistema (ya existían en el modelo): `CUIDADOR` y `PADRE`, ambos heredan de `Usuario`.

---

## 2. Visión general del flujo

```
┌─────────────┐   1. correo+clave    ┌──────────────────┐
│  LoginView  │ ───────────────────► │  AuthController  │
│  (Vue)      │                      │  /auth/login     │
└─────────────┘ ◄─────────────────── └──────────────────┘
       │           2. { token, usuario }        │ valida contra la BD
       │                                         │ y firma el token (JwtService)
       ▼
┌─────────────┐
│ store auth  │  guarda token + usuario (localStorage)
└─────────────┘
       │ 3. redirige según rol
       ▼
┌──────────────────────┐      4. cada petición lleva el token
│ Dashboard del rol     │ ─────────────────────────────────────►  JwtAuthFilter
│ (cuidador / padre)    │      Authorization: Bearer <token>       valida el token
└──────────────────────┘ ◄─────────────────────────────────────  200 OK / 401
```

---

## 3. Conceptos clave: ¿qué es un JWT?

Un **JWT (JSON Web Token)** es una cadena firmada que representa la sesión. Tiene 3 partes
separadas por puntos: `header.payload.firma`.

- **Payload**: datos no secretos. En nuestro caso: `sub` (id del usuario), `tipoUsuario`,
  `nombre`, `iat` (emitido), `exp` (expira).
- **Firma**: se calcula con una **clave secreta** que solo conoce el servidor. Si alguien
  altera el payload, la firma deja de coincidir y el token se rechaza.

Idea central: el token **no se guarda en el servidor**. El servidor solo necesita su clave
secreta para verificar que el token que le mandan es legítimo. Por eso es "sin estado"
(stateless): no hay tabla de sesiones.

> El payload se puede leer (está en Base64, no cifrado), pero **no se puede falsificar** sin
> la clave. Por eso la contraseña nunca va dentro del token.

---

## 4. Backend — parte por parte

### 4.1 `Usuario` — campo de contraseña
**Archivo:** `models/Usuario.java`

```java
@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
private String contrasena;
```

- Se agregó el campo `contrasena`. Hibernate crea la columna automáticamente (`ddl-auto=update`).
- `@JsonProperty(WRITE_ONLY)`: Jackson **acepta** la contraseña cuando llega (registro/login)
  pero **nunca la incluye** al serializar un usuario en una respuesta. Así, aunque esté en
  texto plano, no se filtra al cliente.

### 4.2 Buscar usuario por correo
**Archivo:** `repositories/UsuarioRepository.java`

```java
Optional<Usuario> findByCorreo(String correo);
```

Spring Data genera la consulta `SELECT ... WHERE correo = ?` a partir del nombre del método
(query method derivado). Es lo que usa el login para encontrar al usuario.

### 4.3 `JwtService` — emitir y validar tokens
**Archivo:** `security/JwtService.java`

- `generarToken(id, tipoUsuario, nombre)`: crea el JWT firmado con HS256, con el id como
  `subject` y el rol/nombre como *claims*. Le pone fecha de expiración (24 h por defecto).
- `validarYObtenerClaims(token)`: parsea y verifica la firma; si el token está manipulado o
  vencido, **lanza excepción**.
- La clave secreta y la expiración salen de `application.properties`
  (`jwt.secret`, `jwt.expiration-ms`), configurables por variable de entorno.

### 4.4 `AuthController` — el endpoint de login
**Archivo:** `controllers/AuthController.java` — `POST /api/v1/auth/login`

Pasos del método `login`:
1. Lee `correo` y `contrasena` del cuerpo.
2. Busca el usuario por correo (`findByCorreo`).
3. Compara la contraseña. **Si el usuario no existe o la clave no coincide, responde 401**
   con el **mismo mensaje** ("Correo o contraseña incorrectos") — a propósito, para no
   revelar si el correo existe.
4. Si todo va bien, genera el token y responde `{ token, usuario }`.

### 4.5 `JwtAuthFilter` — el guardia del backend
**Archivo:** `security/JwtAuthFilter.java`

Es un filtro (`OncePerRequestFilter`) que se ejecuta **antes** de llegar a cualquier
controlador, en cada petición:

1. Si es `OPTIONS` (preflight CORS del navegador) → lo deja pasar.
2. Si la ruta es **pública** → la deja pasar. Públicas:
   - `/api/v1/auth/**` (login)
   - `POST /api/v1/perfiles` (registro de un nuevo usuario)
   - cualquier cosa fuera de `/api/v1/`
3. Para el resto, exige el header `Authorization: Bearer <token>`. Si falta o el token es
   inválido → responde **401** y corta la petición.
4. Si el token es válido, guarda `usuarioId` y `tipoUsuario` en la petición (por si un
   controlador los necesita) y deja continuar.

> Detalle importante: en el 401 también pone el header `Access-Control-Allow-Origin: *`,
> para que el navegador muestre el 401 real y no lo enmascare como un error de CORS.

### 4.6 Configuración
**Archivo:** `resources/application.properties`

```properties
jwt.secret=${JWT_SECRET:cuidared-clave-secreta-academica-...}   # ≥ 32 caracteres (HS256)
jwt.expiration-ms=${JWT_EXPIRATION_MS:86400000}                 # 24 horas
```

### 4.7 Dependencia
**Archivo:** `pom.xml` — se agregó la librería **jjwt** (`io.jsonwebtoken`, versión 0.11.5)
para generar y validar los tokens. No se usó Spring Security completo: con jjwt + un filtro
propio es suficiente y más fácil de explicar.

---

## 5. Frontend — parte por parte

### 5.1 Store de sesión
**Archivo:** `stores/auth.js`

Un objeto reactivo (sin Pinia) que mantiene `{ token, usuario }` y lo **persiste en
localStorage** (clave `cuidared_auth`), para que la sesión sobreviva al recargar la página.
Expone:
- `auth.autenticado`, `auth.tipoUsuario`, `auth.usuario`
- `iniciarSesion(token, usuario)` / `cerrarSesion()`
- `rutaDashboard()` → devuelve `/dashboard-cuidador` o `/dashboard-padre` según el rol.

### 5.2 Cliente HTTP con token
**Archivo:** `services/api.js` (función `apiFetch`)

Es el punto único por donde pasan casi todas las llamadas al backend. Se le agregó:
- Adjunta `Authorization: Bearer <token>` automáticamente (salvo que se pida `auth: false`,
  como en el propio login).
- Si el backend responde **401 teniendo sesión**, asume que el token expiró: cierra sesión y
  redirige a `/login`.

### 5.3 Servicio de login
**Archivo:** `services/authService.js`

```js
loginApi(correo, contrasena) → POST /auth/login  (con auth:false)
```

### 5.4 Pantalla de login
**Archivo:** `views/LoginView.vue`

Formulario de correo + contraseña. Al enviar:
1. Llama a `loginApi`.
2. Guarda la sesión con `auth.iniciarSesion(token, usuario)`.
3. Redirige con `router.push(auth.rutaDashboard())` → **aquí ocurre la dirección por rol**.
4. Si falla, muestra el mensaje de error del backend.

### 5.5 Dashboards por rol
**Archivos:** `views/DashboardCuidadorView.vue` y `views/DashboardPadreView.vue`

Cada uno es la página de inicio de su rol: saluda al usuario por su nombre y muestra tarjetas
con accesos a las acciones de ese rol (el cuidador ve Agenda; el padre ve Buscar Cuidadores; etc.).

### 5.6 Rutas y guard (protección de rutas)
**Archivo:** `router/index.js`

- Cada ruta lleva un `meta`:
  - `meta.publica: true` → landing (`/`), login y registro (no requieren sesión).
  - `meta.roles: ["CUIDADOR"]` / `["PADRE"]` / ambos → quién puede entrar.
- Un **guard global** `beforeEach` se ejecuta antes de **cada** navegación:
  - **Ruta pública** → pasa. Pero si ya hay sesión y va a `/` o `/login` (no tienen sentido
    logueado), lo manda a su dashboard.
  - **Ruta protegida sin sesión** → lo redirige a `/login?auth=required`. Ese `auth=required`
    es la señal para que el login muestre el aviso de "debes iniciar sesión".
  - **Ruta con un rol que no le corresponde** (ej. un padre entrando a `/agenda`) → lo manda a
    **su propio** dashboard.
- La raíz `/` es la **landing pública**; el guard la redirige al dashboard solo si ya hay sesión.

> Esto cubre el caso de "acceso manual": aunque alguien escriba la URL de una ruta protegida
> directamente en el navegador, el guard la intercepta antes de cargar la página.

### 5.7 Navbar por rol y header público
**Archivo:** `App.vue`

Hay **dos cabeceras** que se muestran según el estado de sesión:

- **Header público** (sin sesión, salvo en la pantalla de login): logo a la izquierda y los
  botones **Iniciar sesión** y **Registrarse** arriba a la derecha.
- **Navbar de la app** (con sesión): enlaces **según el rol** (Agenda solo al cuidador, Buscar
  Cuidadores solo al padre; Solicitudes, Calificaciones, Notificaciones y Perfil a ambos), el
  **nombre del usuario** y un botón **Salir** (cierra sesión y va a `/login`).

Se controla con dos `computed`: `mostrarHeaderPublico` (`!autenticado && ruta !== /login`) y
`mostrarNav` (`autenticado`).

### 5.8 Registro con contraseña
**Archivo:** `views/PerfilView.vue`

Al formulario de registro se le agregó el campo **contraseña** (mínimo 4 caracteres). Tras
registrarse, redirige a `/login` para que el usuario entre con sus credenciales.

### 5.9 Landing page y aviso de acceso restringido
**Archivos:** `views/HomeView.vue` y `views/LoginView.vue`

- `HomeView` es la **landing pública** en `/` (hero con descripción del proyecto, servicios y
  llamadas a la acción). Sus botones dirigen a **Registrarme** (`/perfil`) e **Iniciar sesión**
  (`/login`).
- `LoginView` lee `route.query.auth`: si vale `required` (porque el guard redirigió desde una
  ruta protegida), muestra un **aviso amarillo**: *"Debes iniciar sesión para acceder a esa
  página."* Así el usuario entiende por qué fue redirigido, en lugar de un salto silencioso.

---

## 6. Recorrido completo de una sesión

1. El usuario abre la app en `/` → ve la **landing pública** con los botones Iniciar sesión / Registrarse.
2. Pulsa **Iniciar sesión** (o si intenta abrir una ruta protegida a mano, el guard lo manda al
   login con el aviso "debes iniciar sesión").
3. Escribe correo y clave → `LoginView` llama a `POST /auth/login`.
4. `AuthController` valida contra la BD y, con `JwtService`, **firma un token** que incluye el rol.
5. El frontend guarda `{ token, usuario }` en el store + localStorage.
6. Redirige a `/dashboard-cuidador` o `/dashboard-padre` según `usuario.tipoUsuario`.
7. El usuario navega: cada llamada al backend incluye `Authorization: Bearer <token>`.
8. `JwtAuthFilter` valida el token en cada petición protegida (200 si es válido, 401 si no).
9. Si el token expira, la próxima llamada da 401 → el frontend cierra sesión y vuelve a login.
10. "Salir" borra la sesión del localStorage y regresa a login.

---

## 7. Decisiones de diseño y sus trade-offs

| Decisión | Por qué | Trade-off / alternativa |
|----------|---------|--------------------------|
| **JWT (stateless)** | El servidor no guarda sesiones; escala fácil y es estándar. | Hay que cuidar la expiración; no se puede "revocar" un token antes de que venza sin lógica extra. |
| **Contraseña en texto plano** | Decisión académica para simplificar. | **Inseguro**: lo correcto en producción es hashear con BCrypt. Es un cambio pequeño si se requiere. |
| **Filtro propio + jjwt** (sin Spring Security) | Más fácil de entender y defender; menos "magia". | Spring Security daría más features (roles por endpoint, etc.) listas. |
| **Roles en el frontend** | Cubre el requisito de dirigir y proteger la navegación por rol. | El backend autentica pero no restringe por rol cada endpoint; sería el siguiente paso. |
| **Sesión en localStorage** | Sobrevive recargas, simple. | Vulnerable a XSS; en producción se valoran cookies httpOnly. |

---

## 8. Cómo probarlo (demo)

```bash
# 1. Backend
cd cuidared-backend && mvn spring-boot:run     # http://localhost:8080

# 2. Frontend
cd cuidared-frontend && npm run dev            # http://localhost:5173
```

Credenciales de prueba (clave `12345` en ambas):
- **Cuidador:** `caldera12@gmail.com` → entra al dashboard del cuidador.
- **Padre:** `jesus@gmail.com` → entra al dashboard del padre.

Pruebas para mostrar en la defensa:
1. Abrir `http://localhost:5173/` → se ve la **landing pública** con los botones de Iniciar
   sesión / Registrarse arriba a la derecha.
2. Escribir a mano una ruta protegida sin sesión (ej. `http://localhost:5173/agenda`) → te
   redirige al login con el **aviso amarillo** "Debes iniciar sesión para acceder a esa página".
3. Login con cada rol → se ve cómo cambian el dashboard y los enlaces del navbar.
4. Estando logueado como padre, escribir la URL de otro rol (`/agenda`) → el guard lo regresa a
   su dashboard.
5. (Técnico) Llamar a un endpoint protegido sin token → **401**.

Verificación por consola (sin navegador):
```bash
# Login → devuelve el token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"correo":"jesus@gmail.com","contrasena":"12345"}'

# Endpoint protegido sin token → 401
curl -i http://localhost:8080/api/v1/agenda/horarios/ID
```

---

## 9. Preguntas frecuentes de defensa

**¿Dónde se guarda la sesión?**
En el cliente (localStorage), como token JWT. El servidor no guarda sesiones: solo verifica
la firma del token con su clave secreta.

**¿Cómo sabe el sistema si eres padre o cuidador?**
El rol (`tipoUsuario`) viaja **dentro del token** (firmado) y también en el objeto `usuario`.
El frontend lo lee para dirigir al dashboard y filtrar el navbar.

**¿Qué pasa si alguien modifica el token para cambiar su rol?**
La firma deja de coincidir con la que el servidor recalcula con su clave secreta, así que
`JwtService` lo rechaza y el filtro responde 401.

**¿Qué pasa si entro a mano a una ruta sin haber iniciado sesión?**
El guard global del router (`beforeEach`) intercepta la navegación antes de cargar la página
y te redirige a `/login?auth=required`, donde se muestra el aviso "Debes iniciar sesión para
acceder a esa página". Además, si esa página intenta llamar al backend, el `JwtAuthFilter`
responde 401. Son dos capas de protección: una en el frontend (navegación) y otra en el backend (API).

**¿Y si intento entrar a una ruta de otro rol (ej. un padre a la Agenda del cuidador)?**
El guard ve que el rol del usuario no está en `meta.roles` de esa ruta y lo devuelve a su
propio dashboard.

**¿Por qué la contraseña no aparece en las respuestas?**
Por `@JsonProperty(WRITE_ONLY)`: Jackson la acepta de entrada pero nunca la serializa de salida.

**¿Es seguro guardar la contraseña en texto plano?**
No para producción. Fue una decisión del proyecto para simplificar; lo correcto es BCrypt
(hash irreversible). Migrar es de bajo esfuerzo.

**¿Por qué no usaron Spring Security?**
Para mantener el código entendible y bajo nuestro control. Con jjwt + un filtro propio se
cubre el requisito; Spring Security se podría adoptar si el alcance crece.

**¿Qué arquitectura sigue esto?**
Sigue la **arquitectura de capas** del proyecto: `AuthController` (presentación/HTTP),
validación + `JwtService` (negocio), `UsuarioRepository` (persistencia), PostgreSQL (datos).
El `JwtAuthFilter` es un *interceptor* transversal previo a la capa de presentación.

---

*Generado el 2026-06-16. Si cambian las entidades o el flujo de auth, actualizar también el
[README](../README.md) y la [guía de Hibernate](guia-hibernate-jpa.md).*
