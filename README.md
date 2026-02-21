# 0verpass (climbing-wall) — Documentación técnica

Proyecto de muro de escalada: landing page estática (con tienda de venta/alquiler de material), base de datos MySQL para registro de pagos (mensual/diario) y aplicación Java de consola.

---

## Stack

| Componente   | Tecnología |
|-------------|------------|
| Frontend    | HTML5, CSS (inline), JavaScript (vanilla). Google Identity Services (opcional). |
| Base de datos | MySQL 8.0 (Docker). |
| Backend     | Java 17, Maven. MySQL Connector/J 8.4.0, HikariCP 5.1.0. |

---

## Estructura del proyecto

```
climbing-wall/
├── index.html
├── docker-compose.yml
├── init.sql
├── pom.xml
├── servir.bat
└── src/main/
    ├── java/com/overpass/
    │   ├── Main.java
    │   ├── dao/PersonaDao.java
    │   ├── dao/PagoDao.java
    │   ├── db/DataSource.java
    │   └── model/Persona.java, Pago.java
    └── resources/application.properties
```

---

## Base de datos

**Servicio:** MySQL 8.0 en contenedor. Puerto **3306**. Base **0verpass**. Usuario/contraseña definidos en `docker-compose.yml` y en `application.properties`. Inicialización vía `init.sql` en `/docker-entrypoint-initdb.d/`.

### Esquema

**Tabla `persona`**

| Columna       | Tipo         | Restricciones |
|---------------|--------------|---------------|
| id            | INT          | PK, AUTO_INCREMENT |
| nombre        | VARCHAR(100) | NOT NULL |
| apellido      | VARCHAR(100) | NOT NULL |
| email         | VARCHAR(255) | NOT NULL, UNIQUE |
| telefono      | VARCHAR(30)  | NULL |
| documento     | VARCHAR(30)  | NULL |
| fecha_alta    | DATETIME     | NOT NULL, DEFAULT CURRENT_TIMESTAMP |
| activo        | TINYINT(1)   | NOT NULL, DEFAULT 1 |
| observaciones | TEXT         | NULL |

**Tabla `pago`**

| Columna        | Tipo           | Restricciones |
|----------------|----------------|---------------|
| id             | INT            | PK, AUTO_INCREMENT |
| persona_id     | INT            | NOT NULL, FK → persona(id), ON DELETE RESTRICT |
| tipo           | ENUM('MENSUAL','DIARIO') | NOT NULL |
| monto          | DECIMAL(10,2)  | NOT NULL |
| fecha_pago     | DATE           | NOT NULL |
| vigencia_desde | DATE           | NOT NULL |
| vigencia_hasta | DATE           | NOT NULL |
| medio_pago     | VARCHAR(50)    | NULL |
| created_at     | DATETIME       | NOT NULL, DEFAULT CURRENT_TIMESTAMP |

**Índices:** `idx_pago_persona` (persona_id), `idx_pago_tipo` (tipo), `idx_pago_fecha` (fecha_pago), `idx_pago_vigencia` (vigencia_desde, vigencia_hasta), `idx_persona_activo` (activo). `init.sql` incluye datos de ejemplo.

---

## Aplicación Java

- **GroupId / ArtifactId:** `com.overpass` / `0verpass-registro`. **Versión:** 1.0.0.
- **Clase principal:** `com.overpass.Main`.
- **Encoding:** UTF-8. **Java:** 17.

### Dependencias (pom.xml)

- `com.mysql:mysql-connector-j:8.4.0`
- `com.zaxxer:HikariCP:5.1.0`

### Paquetes y responsabilidades

- **`com.overpass.db.DataSource`** — Carga `application.properties`, configura HikariCP y expone un `javax.sql.DataSource` singleton.
- **`com.overpass.model.Persona`** — Entidad: id, nombre, apellido, email, telefono, documento, fechaAlta (LocalDateTime), activo (boolean), observaciones.
- **`com.overpass.model.Pago`** — Entidad: id, personaId, tipo (enum MENSUAL/DIARIO), monto (BigDecimal), fechaPago, vigenciaDesde, vigenciaHasta (LocalDate), medioPago, createdAt (LocalDateTime). Opcional: `nombrePersona` para listados con JOIN.
- **`com.overpass.dao.PersonaDao`** — `findAll()`, `findById(int)`, `findByEmail(String)`, `insert(Persona)`, `update(Persona)`.
- **`com.overpass.dao.PagoDao`** — `findAllWithPersona()`, `findByTipo(Tipo)`, `findByPersonaId(int)`, `findById(int)`, `insert(Pago)`, `resumenEntre(LocalDate, LocalDate)` (cantidad y total por tipo en el rango).
- **`com.overpass.Main`** — Punto de entrada: usa los DAOs para listar personas, pagos (con nombre) y resumen del mes actual por consola.

### Configuración Java

**Archivo:** `src/main/resources/application.properties`

| Clave             | Uso |
|-------------------|-----|
| jdbc.url          | URL JDBC (host, puerto, base, `serverTimezone=America/Argentina/Buenos_Aires`, encoding). |
| jdbc.user         | Usuario MySQL. |
| jdbc.password     | Contraseña MySQL. |
| jdbc.pool.size    | Tamaño del pool HikariCP (por defecto 5). |

---

## Landing (`index.html`)

- **Tipo:** página única; CSS y JS en el mismo archivo. Sin build.
- **Servidor local:** `servir.bat` inicia un servidor HTTP (Python) en el puerto **8080** y opcionalmente abre el navegador.

### Configuración en código

- **WhatsApp:** enlace flotante con `href` tipo `https://wa.me/<código_país><número_sin_+>?text=...`. Reemplazar el número en el `href` por el deseado.
- **Google Sign-In:** variable `GOOGLE_CLIENT_ID` en el script. Si está vacía o con valor placeholder (`TU_CLIENT_ID_...`), se muestra el botón “Registrarse por email” (mailto) en lugar del botón de Google. Script externo: `https://accounts.google.com/gsi/client`.

### Secciones

- Hero, Servicios, Gimnasio, **Tienda**, Horarios, Registro, Contacto. **Tienda:** grid de productos (arnés, zapatillas de escalada, asegurador, cuerda) con precio de venta y alquiler por día; botón «Consultar» abre WhatsApp con mensaje predefinido por producto.

### Comportamiento

- Sesión de usuario (nombre, email, foto) en `localStorage` bajo la clave `0verpass_user`. Badge de usuario y opción de cerrar sesión cuando hay sesión guardada.
