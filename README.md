# CuidaRed — Planificación de Cuidados y Asistencia Domiciliaria

**Equipo 7 · Ingeniería de Software MAR–JUL 2026 · UCAB**

Plataforma web P2P que conecta a padres/tutores con cuidadores domiciliarios en Caracas, permitiendo publicar solicitudes de cuidado, gestionar disponibilidad, calificar servicios y recibir notificaciones en tiempo real.

---

## Integrantes

| Nombre              | Rol/Módulo principal              |
|---------------------|-----------------------------------|
| Mauricio Caldera    | Gestión de Perfil / Agenda        |
| Julio Figueredo     | Gestión de Intercambio / Notifs.  |
| Jesús Figueroa      | Gestión de Calificaciones         |
| Isaac Rodríguez     | Gestión de Intercambio / Agenda   |
| Christian Vilera    | Gestión de Agenda (ERS)           |

---

## Stack Tecnológico

| Capa       | Tecnología                          |
|------------|-------------------------------------|
| Backend    | Java 17 · Spring Boot 3.2.4 · Maven |
| Frontend   | Vue.js 3 · Vite · Vue Router        |
| Persistencia | **PostgreSQL + Hibernate (Spring Data JPA)** |
| Librerías UI | FullCalendar · SweetAlert2        |

> **Nota histórica:** en las primeras iteraciones la persistencia se hizo sobre
> archivos JSON. A partir de junio 2026 el sistema usa **PostgreSQL** vía
> **Hibernate / Spring Data JPA** (ver sección [Persistencia](#persistencia--postgresql--hibernate)).

---

## Arquitectura

El proyecto sigue el patrón **MVC** con una separación clara en capas:

```
cuidared-backend/
└── src/main/java/com/cuidared/
    ├── controllers/     ← Capa HTTP (reciben requests, devuelven responses)
    ├── services/        ← Lógica de negocio y validaciones de dominio
    ├── repositories/    ← Interfaces Spring Data JPA (JpaRepository)
    ├── models/          ← Entidades JPA del dominio (@Entity)
    ├── exceptions/      ← Excepciones de dominio personalizadas
    └── schedulers/      ← Tareas programadas (notificaciones)

cuidared-frontend/
└── src/
    ├── views/           ← Páginas/vistas principales (una por épica)
    ├── components/      ← Componentes reutilizables
    ├── services/        ← Clientes HTTP hacia el backend (axios)
    └── router/          ← Definición de rutas Vue Router
```

### Flujo de una petición

```
Vue View → service.js (axios) → Controller (@RestController)
         → Service (@Service)  → Repository (JpaRepository)
         → Hibernate → PostgreSQL
```

---

## Módulos implementados

### Sprint 1 — Crear y Consultar

| Épica               | Funcionalidad               | Endpoint                                          |
|---------------------|-----------------------------|---------------------------------------------------|
| Gestionar Perfil    | Crear perfil (Padre/Cuidador) | `POST /api/v1/perfiles`                         |
|                     | Consultar perfil             | `GET /api/v1/perfiles/{id}`                      |
| Gestionar Agenda    | Registrar horario disponible | `POST /api/v1/agenda/horarios/{cuidadorId}`      |
|                     | Consultar horarios           | `GET /api/v1/agenda/horarios/{cuidadorId}`       |
|                     | Buscar cuidadores disponibles| `POST /api/v1/agenda/buscar-cuidadores`          |
| Gestionar Intercambio | Crear solicitud de cuidado | `POST /api/v1/intercambio/solicitudes`           |
|                     | Listar solicitudes           | `GET /api/v1/intercambio/solicitudes`            |
|                     | Historial/futuras por padre  | `GET /api/v1/intercambio/solicitudes/padre/{id}` |
| Gestionar Calificaciones | Crear calificación      | `POST /api/v1/calificaciones`                    |
|                     | Ver calificaciones por cuidador | `GET /api/v1/calificaciones/cuidador/{id}`    |
| Gestionar Notificaciones | Listar notificaciones   | `GET /api/v1/notificaciones/{usuarioId}`         |
|                     | Silenciar notificación       | `PATCH /api/v1/notificaciones/{id}/silenciar`    |

### Sprint 2 — Modificar y Eliminar

| Épica               | Funcionalidad                   | Endpoint                                              |
|---------------------|---------------------------------|-------------------------------------------------------|
| Gestionar Perfil    | Modificar información           | `PUT /api/v1/perfiles/{id}`                          |
|                     | Eliminar perfil                 | `DELETE /api/v1/perfiles/{id}`                       |
| Gestionar Agenda    | Modificar horario disponible    | `PUT /api/v1/agenda/horarios/{cuidadorId}/{indice}`  |
|                     | Eliminar horario disponible     | `DELETE /api/v1/agenda/horarios/{cuidadorId}/{indice}` |
| Gestionar Intercambio | Modificar solicitud           | `PUT /api/v1/intercambio/solicitudes/{id}`           |
|                     | Cancelar solicitud              | `PATCH /api/v1/intercambio/solicitudes/{id}/cancelar`|
| Gestionar Calificaciones | Modificar calificación     | `PUT /api/v1/calificaciones/{id}`                    |
|                     | Eliminar calificación           | `DELETE /api/v1/calificaciones/{id}`                 |
| Gestionar Notificaciones | Programar recordatorio     | `POST /api/v1/notificaciones/programar`              |
|                     | Eliminar notificación           | `DELETE /api/v1/notificaciones/{id}`                 |

---

## Modelo de Dominio

```
Usuario (abstract)
├── Padre          — solicitudesIds: List<String>
└── Cuidador       — habilidades, horariosDisponibles, tarifaHora, rutaDocumentoAntecedentes

Solicitud          — padreId, cuidadorId, horario, tipo, estado, fecha, duracionHoras
Horario            — fechaInicio, fechaFin  [con seSolapaCon() y contiene()]
Calificacion       — solicitudId, cuidadorId, autorId, puntuacion (1–5), comentario
Notificacion       — usuarioId, titulo, mensaje, tipo, leida, silenciada, fechaProgramada

EstadoSolicitud    — PENDIENTE | ACEPTADA | FINALIZADA | CANCELADA
TipoAsistencia     — NINO | ADULTO_MAYOR | MASCOTA
```

---

## Cómo levantar el proyecto

### Prerrequisitos

- Java 17+
- Maven 3.8+ (o el wrapper `./mvnw` incluido)
- Node.js 18+
- **PostgreSQL 14+** corriendo localmente

### 1. Base de datos

Crea la base de datos una sola vez (las **tablas las genera Hibernate** automáticamente al arrancar, gracias a `ddl-auto=update`):

```bash
psql -U postgres -c "CREATE DATABASE cuidared;"
```

Credenciales por defecto (configurables en [application.properties](cuidared-backend/src/main/resources/application.properties)):

| Propiedad | Valor por defecto | Variable de entorno |
|-----------|-------------------|---------------------|
| URL       | `jdbc:postgresql://localhost:5432/cuidared` | `DB_URL` |
| Usuario   | `postgres`        | `DB_USER`           |
| Password  | `12345`           | `DB_PASSWORD`       |

Para usar otras credenciales sin tocar el código:

```bash
export DB_USER=miusuario
export DB_PASSWORD=miclave
```

### 2. Backend (Spring Boot)

```bash
cd cuidared-backend
./mvnw clean spring-boot:run     # o: mvn clean spring-boot:run
```

El servidor arranca en `http://localhost:8080`. Al iniciar, Hibernate crea/actualiza
las tablas en la base `cuidared`.

### 3. Frontend (Vue.js + Vite)

```bash
cd cuidared-frontend
npm install
npm run dev
```

La app queda disponible en `http://localhost:5173`.

---

## Restricciones del sistema

1. Disponible únicamente para usuarios en **Caracas**.
2. Idioma único: **español**.
3. Solo servicios de cuidado domiciliario: niños, adultos mayores y mascotas. No se admiten bienes físicos ni servicios de otra índole.
4. Plataforma **web únicamente** — no se desarrollará aplicación móvil.

---

## Rangos de calidad objetivo

- Carga de pantallas principales **< 3 segundos**.
- Interfaz **intuitiva y amigable**.
- Estructura **modular** para facilitar mantenimiento.
- **Confidencialidad** de los datos del usuario.

---

## Persistencia — PostgreSQL + Hibernate

El sistema persiste sobre **PostgreSQL** usando **Spring Data JPA / Hibernate**.
Los servicios siguen llamando a `findAll()`, `findById()`, `save()` y `deleteById()`,
por lo que la lógica de negocio **no cambió**: solo se sustituyó la capa de repositorio.

### Esquema generado

Hibernate crea automáticamente estas tablas a partir de las entidades:

| Tabla                  | Origen                                                        |
|------------------------|--------------------------------------------------------------|
| `usuarios`             | `Usuario` (herencia `SINGLE_TABLE`, discriminador `tipo_usuario`) |
| `solicitudes`          | `Solicitud` (con `Horario` como `@Embedded`)                 |
| `calificaciones`       | `Calificacion`                                               |
| `notificaciones`       | `Notificacion`                                               |
| `cuidador_horarios`    | `Cuidador.horariosDisponibles` (`@ElementCollection`)        |
| `cuidador_habilidades` | `Cuidador.habilidades` (`@ElementCollection` de enum)        |
| `padre_solicitudes`    | `Padre.solicitudesIds` (`@ElementCollection`)                |

### Decisiones de mapeo

- **Herencia `Usuario → Padre / Cuidador`**: estrategia `SINGLE_TABLE` con columna
  discriminadora `tipo_usuario`. El campo `tipoUsuario` se mapea **de solo lectura**
  sobre esa columna (`insertable=false, updatable=false`), evitando duplicar el dato.
- **`Horario`** es `@Embeddable`: se usa como `@Embedded` dentro de `Solicitud` y como
  elemento de la colección `cuidador_horarios`. Sus métodos de dominio
  (`seSolapaCon()`, `contiene()`) no se ven afectados por JPA.
- **IDs**: se mantienen como `String` (UUID generado en el constructor), conservando
  las firmas `findById(String)` existentes.
- **`open-in-view=true`**: mantiene la sesión de Hibernate abierta durante la
  serialización JSON para evitar `LazyInitializationException` en las colecciones.
  Es adecuado para la escala de este proyecto; en producción se preferirían DTOs.

### Configuración

Toda la configuración vive en
[application.properties](cuidared-backend/src/main/resources/application.properties).
El parámetro `spring.jpa.hibernate.ddl-auto=update` hace que Hibernate cree/actualice
el esquema al arrancar. Para empezar de cero se puede cambiar a `create`, o ejecutar:

```bash
psql -U postgres -c "DROP DATABASE cuidared;" -c "CREATE DATABASE cuidared;"
```

---

## Documentación académica de referencia

| Documento | Descripción |
|-----------|-------------|
| `Brief equipo7.pdf` | Mapa de impacto, backlog (User Story Map), restricciones y plan de la 1ra iteración |
| `ERS Equipo7.pdf` | Especificación de Requisitos de Software — historias de usuario con criterios de aceptación (Canvas) y requisitos suplementarios |
| `Anexo elementos primera entrega.pdf` | Diagramas de secuencia (PlantUML) por historia de usuario y diagrama de clases del Sprint 1 |
| Jira (Scrum board) | Plan de trabajo y backlog en `ingsoftwareucabgrupo7.atlassian.net` |
