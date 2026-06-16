# Guía de Hibernate / Spring Data JPA — CuidaRed

**Equipo 7 · Ingeniería de Software MAR–JUL 2026 · UCAB**

Esta guía documenta cómo trabajar con la capa de persistencia del proyecto, que usa **Spring Data JPA + Hibernate** sobre **PostgreSQL**.
No es una introducción a JPA en general: está enfocada en los patrones y convenciones que ya están en el código de CuidaRed.

---

## Índice

1. [Arquitectura de la capa de persistencia](#1-arquitectura-de-la-capa-de-persistencia)
2. [Los repositorios — qué tienes gratis](#2-los-repositorios--qué-tienes-gratis)
3. [Leer datos (GET)](#3-leer-datos-get)
4. [Escribir datos (POST / PUT)](#4-escribir-datos-post--put)
5. [Borrar datos (DELETE)](#5-borrar-datos-delete)
6. [Transacciones](#6-transacciones)
7. [Anotaciones JPA usadas en el proyecto](#7-anotaciones-jpa-usadas-en-el-proyecto)
8. [Esquema de la base de datos](#8-esquema-de-la-base-de-datos)
9. [Configuración](#9-configuración)
10. [Errores comunes y cómo evitarlos](#10-errores-comunes-y-cómo-evitarlos)
11. [Setup para nuevos integrantes del equipo](#11-setup-para-nuevos-integrantes-del-equipo)
12. [Cómo maneja Hibernate los cambios al esquema (vs. Alembic)](#12-cómo-maneja-hibernate-los-cambios-al-esquema-vs-alembic)
10. [Errores comunes y cómo evitarlos](#10-errores-comunes-y-cómo-evitarlos)

---

## 1. Arquitectura de la capa de persistencia

```
Controller (@RestController)
    |  recibe JSON, responde HTTP — no toca lógica de negocio
    v
Service (@Service)
    |  valida reglas de negocio, llama al repositorio
    v
Repository (JpaRepository)
    |  interfaz — Spring genera la implementación en tiempo de arranque
    v
Hibernate (ORM)
    |  traduce objetos Java a SQL automáticamente
    v
PostgreSQL (localhost:5432 / cuidared)
```

**Regla de oro:** los controladores no llaman directamente al repositorio. Los servicios tampoco
escriben SQL. Hibernate traduce todo.

---

## 2. Los repositorios — qué tienes gratis

Cada repositorio en el proyecto es una **interfaz vacía** que extiende `JpaRepository`:

```java
// repositories/SolicitudRepository.java
@Repository
public interface SolicitudRepository extends JpaRepository<Solicitud, String> {
    // nada aquí → ya tienes findAll, findById, save, deleteById, count, etc.
}
```

El parámetro `<Solicitud, String>` le dice a Spring: *"la entidad es Solicitud y su @Id es de tipo String"*.

### Métodos disponibles sin escribir nada

| Método | Qué hace | SQL equivalente |
|--------|----------|-----------------|
| `findAll()` | Trae todas las filas | `SELECT * FROM solicitudes` |
| `findById(id)` | Una fila por PK → `Optional<T>` | `SELECT ... WHERE id = ?` |
| `save(entidad)` | Inserta **o** actualiza | `INSERT` / `UPDATE` |
| `saveAll(lista)` | Guarda una lista | varios `INSERT` / `UPDATE` |
| `deleteById(id)` | Borra por PK | `DELETE WHERE id = ?` |
| `existsById(id)` | ¿Existe? → `boolean` | `SELECT count(*) WHERE id = ?` |
| `count()` | Cantidad de filas | `SELECT count(*)` |

Repositorios disponibles en el proyecto:

| Interfaz | Entidad | Tabla |
|----------|---------|-------|
| `UsuarioRepository` | `Usuario` (abstract) | `usuarios` |
| `SolicitudRepository` | `Solicitud` | `solicitudes` |
| `CalificacionRepository` | `Calificacion` | `calificaciones` |
| `NotificacionRepository` | `Notificacion` | `notificaciones` |

---

## 3. Leer datos (GET)

### 3.1 Traer todo — `findAll()`

```java
List<Solicitud> todas = solicitudRepository.findAll();
```

### 3.2 Traer por ID — `findById()` → `Optional`

`findById` devuelve un `Optional<T>` porque la fila puede no existir.
El patrón estándar en los servicios del proyecto es:

```java
Solicitud solicitud = solicitudRepository.findById(id)
        .orElseThrow(() -> new ReglaNegocioException("Solicitud no encontrada: " + id));
```

Si el ID existe devuelve la entidad; si no, lanza la excepción de negocio (que el controlador
captura y convierte en un 400 / 404).

### 3.3 Query methods derivados — filtrar sin SQL

Spring lee el **nombre del método** y genera el SQL. Solo hay que declarar la firma en la interfaz:

```java
public interface SolicitudRepository extends JpaRepository<Solicitud, String> {

    // SELECT * FROM solicitudes WHERE padre_id = ?
    List<Solicitud> findByPadreId(String padreId);

    // SELECT * FROM solicitudes WHERE estado = ?
    List<Solicitud> findByEstado(EstadoSolicitud estado);

    // SELECT * FROM solicitudes WHERE padre_id = ? AND estado = ?
    List<Solicitud> findByPadreIdAndEstado(String padreId, EstadoSolicitud estado);

    // SELECT count(*) FROM solicitudes WHERE cuidador_id = ? AND estado = ?
    long countByCuidadorIdAndEstado(String cuidadorId, EstadoSolicitud estado);
}
```

Esto es más eficiente que filtrar con streams en memoria (la BD filtra antes de enviar datos).
Palabras clave que entiende Spring: `And`, `Or`, `Between`, `LessThan`, `GreaterThan`,
`Like`, `OrderBy`, `In`, `IsNull`, `IsNotNull`, `True`, `False`.

**Ejemplo con ordenamiento:**

```java
// SELECT * FROM calificaciones WHERE cuidador_id = ? ORDER BY fecha DESC
List<Calificacion> findByCuidadorIdOrderByFechaDesc(String cuidadorId);
```

**Ejemplo con booleano:**

```java
// SELECT * FROM notificaciones WHERE usuario_id = ? AND silenciada = false
List<Notificacion> findByUsuarioIdAndSilenciadaFalse(String usuarioId);
```

### 3.4 `@Query` — cuando el nombre del método no alcanza

Se puede escribir JPQL (como SQL pero sobre clases Java, no tablas):

```java
// JPQL — usa nombres de clase y campo Java, no de tabla y columna
@Query("SELECT s FROM Solicitud s WHERE s.padreId = :padreId AND s.estado IN :estados")
List<Solicitud> buscarPorEstados(
        @Param("padreId") String padreId,
        @Param("estados") List<EstadoSolicitud> estados);
```

Y si de verdad necesitas SQL nativo de PostgreSQL:

```java
@Query(value = "SELECT * FROM solicitudes WHERE fecha >= CURRENT_DATE", nativeQuery = true)
List<Solicitud> solicitudesFuturas();
```

Usa `nativeQuery = true` solo cuando JPQL no alcance — pierde portabilidad entre bases de datos.

---

## 4. Escribir datos (POST / PUT)

### 4.1 `save()` — inserta o actualiza

```java
Solicitud guardada = solicitudRepository.save(solicitud);
```

`save()` detecta automáticamente si debe hacer INSERT o UPDATE:
- Si el `id` **no existe** en la tabla → `INSERT`
- Si **ya existe** → `UPDATE`

En CuidaRed los IDs se generan en el constructor del modelo con `UUID.randomUUID().toString()`,
por lo que un objeto nuevo siempre llega con ID. Hibernate hace un SELECT rápido para
verificar y luego decide.

### 4.2 Patrón leer → modificar → guardar (UPDATE)

Es la forma correcta de actualizar. **No** se crea un objeto nuevo con el mismo ID:

```java
// En el service — patrón estándar para modificar
public Solicitud modificarSolicitud(String id, Solicitud datos) {
    // 1. Leer el existente (lanza excepción si no existe)
    Solicitud existente = solicitudRepository.findById(id)
            .orElseThrow(() -> new ReglaNegocioException("No existe"));

    // 2. Modificar los campos que corresponda
    existente.setEstado(datos.getEstado());
    existente.setDescripcion(datos.getDescripcion());

    // 3. Guardar — Hibernate detecta el ID y hace UPDATE
    return solicitudRepository.save(existente);
}
```

### 4.3 Guardar listas — `saveAll()`

```java
List<Calificacion> nuevas = List.of(cal1, cal2, cal3);
calificacionRepository.saveAll(nuevas);
```

---

## 5. Borrar datos (DELETE)

```java
// Por ID (lanza EmptyResultDataAccessException si no existe)
solicitudRepository.deleteById(id);

// Por objeto (si ya tienes la entidad cargada)
solicitudRepository.delete(entidad);

// Borrar todo (usar con cuidado)
solicitudRepository.deleteAll();
```

Si quieres verificar que existe antes de borrar:

```java
if (!solicitudRepository.existsById(id)) {
    throw new ReglaNegocioException("No existe la solicitud: " + id);
}
solicitudRepository.deleteById(id);
```

---

## 6. Transacciones

Una transacción garantiza que un conjunto de operaciones sea **todo o nada**: si algo falla
en el medio, PostgreSQL revierte los cambios anteriores y no queda nada a medias.

Anota el método del service con `@Transactional` cuando toque más de una tabla:

```java
@Service
public class SolicitudService {

    @Transactional
    public Solicitud crearSolicitud(Solicitud solicitud) {
        // Operación 1: guardar la solicitud
        Solicitud guardada = solicitudRepository.save(solicitud);

        // Operación 2: agregar el ID al padre
        Padre padre = (Padre) usuarioRepository.findById(solicitud.getPadreId())
                .orElseThrow(() -> new ReglaNegocioException("Padre no encontrado"));
        padre.getSolicitudesIds().add(guardada.getId());
        usuarioRepository.save(padre);

        // Si algo falla aquí → ambas operaciones se revierten automáticamente
        return guardada;
    }
}
```

Sin `@Transactional`, cada `save()` es su propia transacción independiente y podrías
quedar con la solicitud guardada pero el padre sin actualizarse.

---

## 7. Anotaciones JPA usadas en el proyecto

### Entidades principales

| Anotación | Dónde se usa | Qué hace |
|-----------|-------------|----------|
| `@Entity` | `Usuario`, `Solicitud`, `Calificacion`, `Notificacion` | Marca la clase como tabla en la BD |
| `@Table(name="...")` | Todas las entidades | Nombre explícito de la tabla |
| `@Id` | Campo `id` en todas las entidades | Clave primaria |
| `@Column(name="...")` | Campos con nombre diferente al atributo | Nombre de columna explícito |
| `@Enumerated(EnumType.STRING)` | `estado`, `tipo` en `Solicitud`; `habilidades` en `Cuidador` | Guarda el enum como texto, no como número |

### Herencia — `Usuario → Padre / Cuidador`

```java
@Entity
@Table(name = "usuarios")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)   // una sola tabla para los 3 tipos
@DiscriminatorColumn(name = "tipo_usuario")              // columna que distingue el tipo
public abstract class Usuario { ... }

@Entity
@DiscriminatorValue("PADRE")     // valor en tipo_usuario para esta subclase
public class Padre extends Usuario { ... }

@Entity
@DiscriminatorValue("CUIDADOR")
public class Cuidador extends Usuario { ... }
```

Una sola tabla `usuarios` tiene todos los campos. Las columnas exclusivas de `Cuidador`
(`disponible`, `tarifa_hora`, etc.) son `NULL` para las filas de `Padre`, y viceversa.

### Colecciones simples — `@ElementCollection`

Para listas de valores que no son entidades propias (no tienen `@Id`):

```java
// En Cuidador.java
@ElementCollection
@CollectionTable(name = "cuidador_habilidades", joinColumns = @JoinColumn(name = "cuidador_id"))
@Enumerated(EnumType.STRING)
private List<TipoAsistencia> habilidades;

@ElementCollection
@CollectionTable(name = "cuidador_horarios", joinColumns = @JoinColumn(name = "cuidador_id"))
private List<Horario> horariosDisponibles;   // Horario es @Embeddable
```

Hibernate crea una tabla separada por cada `@ElementCollection` con FK al padre.

### Objetos embebidos — `@Embeddable / @Embedded`

`Horario` no es una tabla propia — sus campos se "incrustan" directamente en la tabla que lo use:

```java
@Embeddable          // en Horario.java
public class Horario {
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    // métodos de dominio (seSolapaCon, contiene) — JPA los ignora
}

@Embedded            // en Solicitud.java
private Horario horario;   // agrega fecha_inicio y fecha_fin a la tabla solicitudes
```

---

## 8. Esquema de la base de datos

El diagrama completo está en [`docs/diagrama-bd.puml`](diagrama-bd.puml) (PlantUML).
Resumen de tablas y su origen:

| Tabla | Origen en Java | FK reales |
|-------|---------------|-----------|
| `usuarios` | `Usuario` (SINGLE_TABLE) | — |
| `solicitudes` | `Solicitud` + `Horario @Embedded` | — |
| `calificaciones` | `Calificacion` | — |
| `notificaciones` | `Notificacion` | — |
| `cuidador_habilidades` | `Cuidador.habilidades @ElementCollection` | `cuidador_id → usuarios.id` |
| `cuidador_horarios` | `Cuidador.horariosDisponibles @ElementCollection` | `cuidador_id → usuarios.id` |
| `padre_solicitudes` | `Padre.solicitudesIds @ElementCollection` | `padre_id → usuarios.id` |

> Las relaciones entre `solicitudes`, `calificaciones` y `notificaciones` con `usuarios`
> son **lógicas** (String IDs) — no tienen constraint FK en la BD. Hibernate no las valida.

---

## 9. Configuración

Toda la configuración de persistencia está en
`cuidared-backend/src/main/resources/application.properties`:

```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/cuidared}
spring.datasource.username=${DB_USER:postgres}
spring.datasource.password=${DB_PASSWORD:12345}
spring.datasource.driver-class-name=org.postgresql.Driver

spring.jpa.hibernate.ddl-auto=update       # crea/actualiza tablas al arrancar
spring.jpa.show-sql=true                   # imprime el SQL generado en consola
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.open-in-view=true               # mantiene sesión abierta al serializar JSON
```

Las credenciales se pueden sobreescribir con variables de entorno sin tocar el archivo:

```bash
export DB_USER=miusuario
export DB_PASSWORD=miclave
mvn spring-boot:run
```

---

## 10. Errores comunes y cómo evitarlos

### `LazyInitializationException`

**Qué es:** ocurre cuando Jackson intenta serializar una colección (`@ElementCollection`)
después de que la sesión de Hibernate ya cerró.

**Cómo está resuelto:** `spring.jpa.open-in-view=true` en `application.properties`.
Mantiene la sesión abierta hasta que termina la serialización HTTP. No tocar esta línea.

---

### `TransientPropertyValueException` al guardar

```
object references an unsaved transient instance
```

**Causa:** intentaste guardar una entidad que referencia otra que no está guardada todavía.

**Solución:** guarda primero la entidad referenciada, luego la que la referencia.
O usa `@Transactional` en el método del service para que Spring maneje el orden.

---

### `findById` devuelve `Optional.empty()` inesperadamente

**Causa más común:** el ID que estás buscando no existe en la BD porque el objeto se
guardó en memoria pero nunca se llamó a `save()`.

**Checklist:**
1. ¿Llamaste a `repository.save(objeto)` después de crear el objeto?
2. ¿El ID que usas en la búsqueda es exactamente el mismo que se guardó? (son String, sensibles a mayúsculas/minúsculas y espacios)

---

### Cambios no persisten entre requests

**Causa:** modificaste el objeto Java pero olvidaste llamar a `save()`.

```java
// INCORRECTO — el cambio queda solo en memoria
solicitud.setEstado(EstadoSolicitud.CANCELADA);

// CORRECTO
solicitud.setEstado(EstadoSolicitud.CANCELADA);
solicitudRepository.save(solicitud);  // ← sin esto no va a la BD
```

---

### `ddl-auto=create` borra todos los datos

Si alguien cambia `ddl-auto=update` por `ddl-auto=create` en `application.properties`,
Hibernate **borra y recrea todas las tablas** al arrancar. Para el entorno de desarrollo
usa siempre `update`. Solo usa `create` cuando quieras empezar de cero intencionalmente.

---

### Orden de los horarios del cuidador — por qué NO usamos `@OrderColumn`

Los endpoints de modificar/eliminar horario identifican cada bloque por su **índice** en la
lista, así que el orden de `Cuidador.horariosDisponibles` debe ser estable.

La forma "obvia" sería `@OrderColumn` (una columna física de posición), **pero no funciona**
con esta colección: Hibernate genera esa columna como `NOT NULL` (ignora `nullable = true`),
y al modificar la colección inserta las filas primero y rellena la posición en un segundo
paso — ese primer insert viola el `NOT NULL` y revienta con:

```
ERROR: null value in column "posicion" violates not-null constraint
```

**La solución que usamos es `@OrderBy`:**

```java
@ElementCollection
@CollectionTable(name = "cuidador_horarios", joinColumns = @JoinColumn(name = "cuidador_id"))
@OrderBy("fechaInicio ASC")   // ordena al leer, sin columna física
private List<Horario> horariosDisponibles;
```

Ventajas: orden estable y cronológico, **sin columna extra**, sin migración, y sin el bug de
`@OrderColumn`. Como bonus, los bloques se muestran ordenados por hora de inicio en la UI.

> Si vienes de una versión vieja que llegó a crear la columna `posicion`, bórrala:
> `ALTER TABLE cuidador_horarios DROP COLUMN IF EXISTS posicion;`

---

---

## 11. Setup para nuevos integrantes del equipo

### Dónde está todo lo relevante

| Qué | Dónde |
|-----|-------|
| Cadena de conexión (host, puerto, BD) | `cuidared-backend/src/main/resources/application.properties` → `spring.datasource.url` |
| Usuario y contraseña | Mismo archivo → `spring.datasource.username` / `spring.datasource.password` |
| Comportamiento del esquema al arrancar | Mismo archivo → `spring.jpa.hibernate.ddl-auto` |
| Entidades (= definición de las tablas) | `cuidared-backend/src/main/java/com/cuidared/models/` |
| Repositorios (= acceso a datos) | `cuidared-backend/src/main/java/com/cuidared/repositories/` |
| Diagrama de la BD | `docs/diagrama-bd.puml` |

No hay una carpeta de migraciones ni archivos `.sql` que mantener. El esquema vive
en las clases Java del paquete `models/`.

### Pasos para levantar la BD por primera vez

> Esto solo se hace una vez por máquina. Después de eso, arrancar el backend es suficiente.

**1. Instalar PostgreSQL**

- Windows: descargarlo del instalador oficial de PostgreSQL.
- Linux (Fedora/RHEL): `sudo dnf install postgresql postgresql-server`
- Linux (Debian/Ubuntu): `sudo apt install postgresql`
- Mac: `brew install postgresql`

Verificar que el servicio esté corriendo:

```bash
# Linux (systemd)
sudo systemctl start postgresql
sudo systemctl enable postgresql   # para que arranque con el sistema

# Mac (Homebrew)
brew services start postgresql
```

**2. Crear la base de datos**

Solo hay que crear la base de datos vacía. Las tablas las crea Hibernate:

```bash
psql -U postgres -c "CREATE DATABASE cuidared;"
```

Si postgres requiere contraseña, la del entorno de desarrollo es `12345`.

**3. Verificar la configuración**

Abrir `cuidared-backend/src/main/resources/application.properties` y confirmar que
los valores coinciden con el entorno local. Si el usuario o contraseña de tu PostgreSQL
es diferente, hay dos formas de cambiarlo:

- **Sin tocar el archivo** (recomendado para no subir credenciales a Git):

  ```bash
  export DB_USER=mi_usuario
  export DB_PASSWORD=mi_clave
  mvn spring-boot:run
  ```

- Editando el archivo directamente (solo para uso local, no hacer commit de credenciales personales).

**4. Arrancar el backend**

```bash
cd cuidared-backend
mvn spring-boot:run
```

Al arrancar, verás en la consola los `CREATE TABLE` que Hibernate ejecuta. Después de
la primera vez, como las tablas ya existen, no aparecen más (solo los `SELECT`/`INSERT`
de las peticiones reales).

---

## 12. Cómo maneja Hibernate los cambios al esquema (vs. Alembic)

Si antes usaste **SQLAlchemy + Alembic** (Python), este modelo es diferente.
En Alembic generabas archivos de migración (`alembic revision --autogenerate`) y los
aplicabas (`alembic upgrade head`). Aquí **no existe nada de eso**.

### La clave: `ddl-auto=update`

Este parámetro en `application.properties` le indica a Hibernate qué hacer con el
esquema al arrancar la aplicación:

| Valor | Comportamiento |
|-------|---------------|
| `update` | Compara entidades Java con la BD y aplica solo los cambios necesarios. **No borra datos.** |
| `create` | Borra todas las tablas y las vuelve a crear. Útil para empezar de cero. **Destruye datos.** |
| `create-drop` | Igual que `create`, pero también borra las tablas al apagar. Solo para tests. |
| `validate` | Solo verifica que el esquema coincide; lanza error si hay diferencias. Para producción. |
| `none` | No toca el esquema. Para producción con migraciones manuales. |

El proyecto usa `update`, que es el balance correcto para desarrollo en equipo.

### Qué pasa cuando un compañero hace cambios al modelo

| Cambio en el código Java | Lo que Hibernate hace automáticamente con `update` |
|--------------------------|---------------------------------------------------|
| Se agrega un campo nuevo a una entidad | Agrega la columna a la tabla |
| Se agrega una entidad nueva (`@Entity`) | Crea la tabla nueva |
| Se agrega un `@ElementCollection` | Crea la tabla de colección con su FK |
| Se elimina un campo de una entidad | **No borra la columna** (para no perder datos) |
| Se renombra un campo | Agrega la columna nueva, deja la vieja — quedan las dos |

### Cuándo sí hay que intervenir manualmente

Si alguien renombró una columna, cambió el tipo de un campo, o hizo refactorizaciones
grandes al modelo, `update` puede dejar el esquema en un estado inconsistente (columnas
viejas sobrando, tipos mezclados, etc.).

**La solución más simple en desarrollo es resetear la BD:**

```bash
psql -U postgres -c "DROP DATABASE cuidared;" -c "CREATE DATABASE cuidared;"
```

Y luego arrancar el backend normalmente — Hibernate recrea todo desde cero.

> No hay datos importantes que perder en desarrollo, así que este comando es seguro
> de usar cuando algo falla con el esquema. En producción (con datos reales de usuarios)
> nunca se haría esto; ahí se usaría Flyway o Liquibase para migraciones controladas.

### Resumen de diferencias con Alembic

| | Alembic (Python/SQLAlchemy) | Hibernate `ddl-auto=update` |
|-|-----------------------------|------------------------------|
| Archivos de migración | Sí — un `.py` por cambio | No existen |
| Comando para aplicar cambios | `alembic upgrade head` | Ninguno — solo arrancar el servidor |
| Comando para generar migración | `alembic revision --autogenerate` | No aplica |
| Rollback controlado | Sí — `alembic downgrade` | No — solo reset manual |
| Apto para producción | Sí | No (se usaría Flyway/Liquibase) |
| Apto para desarrollo en equipo | Sí | Sí, con resets ocasionales |

---

*Generado el 2026-06-15. Para cambios en el esquema o en las entidades, actualizar también [`diagrama-bd.puml`](diagrama-bd.puml) y el [`README.md`](../README.md).*
