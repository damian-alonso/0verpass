# 0verpass — Documentación del proyecto

Proyecto completo del **muro de escalada 0verpass**, ubicado frente a las vías de Haedo (Buenos Aires). Incluye una landing page pública, base de datos para el registro de pagos (mensual/diario) y una aplicación Java para el empleador.

---

## Índice

1. [Qué es este proyecto](#qué-es-este-proyecto)
2. [Estructura de archivos](#estructura-de-archivos)
3. [Landing page (sitio web)](#landing-page-sitio-web)
4. [Base de datos MySQL](#base-de-datos-mysql)
5. [Aplicación Java (registro de pagos)](#aplicación-java-registro-de-pagos)
6. [Scripts y utilidades](#scripts-y-utilidades)
7. [Poner todo en marcha](#poner-todo-en-marcha)
8. [Subir a GitHub y ver la página](#subir-a-github-y-ver-la-página)

---

## Qué es este proyecto

- **0verpass** es el nombre del muro de escalada.
- **Landing page**: sitio de una sola página con información del muro, gimnasio, horarios, registro con Google y contacto.
- **Base de datos**: MySQL en Docker para llevar el registro de personas que pagan **mensualmente** o **diariamente**.
- **App Java**: programa de consola para el empleador que lista personas, pagos y genera resúmenes de cobros.

Todo está en una sola carpeta (`climbing-wall`) para tener el proyecto ordenado.

---

## Estructura de archivos

```
climbing-wall/
│
├── index.html              # Landing page del muro (abrir en navegador)
├── docker-compose.yml      # Configuración de MySQL en Docker
├── init.sql                # Script SQL: crea tablas y datos de ejemplo
├── pom.xml                 # Proyecto Maven (Java): dependencias y build
├── servir.bat              # En Windows: sirve la web en http://localhost:8080
├── README.md               # Este archivo — documentación general
├── README-BD.md            # Instrucciones rápidas de base de datos y Java
│
└── src/main/
    ├── java/com/overpass/          # Código Java
    │   ├── Main.java               # Punto de entrada: lista personas, pagos y resumen
    │   ├── dao/
    │   │   ├── PersonaDao.java     # Acceso a datos de personas
    │   │   └── PagoDao.java        # Acceso a datos de pagos
    │   ├── db/
    │   │   └── DataSource.java     # Conexión a MySQL (HikariCP)
    │   └── model/
    │       ├── Persona.java        # Modelo: cliente del muro
    │       └── Pago.java           # Modelo: un pago (mensual o diario)
    │
    └── resources/
        └── application.properties  # URL, usuario y contraseña de MySQL
```

---

## Landing page (sitio web)

**Archivo:** `index.html`

Página única con diseño violeta y negro (estilo “Violent Daimyo”). Todo el CSS está dentro del mismo archivo.

### Diseño y tipografía

- **Paleta:** fondo negro (#0a0a0c), acentos violetas (#7c3aed, #8b5cf6, #a78bfa), texto claro (#f5f3ff) — inspirada en la skin Violent Daimyo de CS.
- **Nombre “0verpass” (logo y título principal):** tipografía **Permanent Marker** (Google Fonts) con efecto tipo graffiti/spray de Counter-Strike: sombras negras de contorno, halo violeta (overspray), borde con `-webkit-text-stroke` y `drop-shadow` para simular pintura en la pared. El resto de títulos de sección usa **Bebas Neue**; el cuerpo **Outfit**.
- **Contacto y footer:** contenido centrado (`.cta-inner` en la sección de contacto, footer en columna centrada).

### Secciones

| Sección      | Contenido |
|-------------|-----------|
| **Hero**    | Nombre “0verpass”, ubicación (frente a las vías de Haedo), eslogan y botones a reserva y actividades. |
| **Servicios** | Búlder, vías con cuerda, cursos y talleres. |
| **Gimnasio**  | Zona de entrenamiento: fingerboard, Pan Gullich/campus, pesas y kettlebells, mobility y estiramiento. |
| **Horarios**  | Lunes a viernes y fines de semana. |
| **Registro**  | “Registrarse / Continuar con Google”. Si no hay Client ID configurado, se muestra “Registrarse por email”. |
| **Contacto**  | Email (fondo@overpass.com), ubicación y enlace a WhatsApp para consultas. |
| **Footer**    | Copyright y teléfono. |
| **WhatsApp**  | Botón flotante (esquina inferior derecha) «Consultas por WhatsApp» que abre un chat con mensaje predeterminado. El número se configura en el `href` del enlace (formato `wa.me/5491112345678`). |

### Registro con Google

- Usa **Google Identity Services** (cuenta de Google).
- Para que funcione el botón “Continuar con Google” hay que crear un **Client ID** en [Google Cloud Console](https://console.cloud.google.com/apis/credentials) (tipo “Aplicación web”) y reemplazar en `index.html` la variable `GOOGLE_CLIENT_ID`.
- Si no se configura, se muestra un botón de “Registrarse por email” (fondo@overpass.com) y un texto explicando cómo activar Google.
- Al iniciar sesión con Google se guardan nombre, email y foto en `localStorage` y se muestra un “badge” de usuario; hay opción de cerrar sesión.

### Cómo ver la web

- **Opción 1:** Doble clic en `servir.bat` (necesitas Python instalado). Abre el navegador en `http://localhost:8080`.
- **Opción 2:** Abrir `index.html` directamente con el navegador (archivo local).

---

## Base de datos MySQL

**Archivos:** `docker-compose.yml`, `init.sql`

MySQL se ejecuta en un contenedor Docker. Al crear el contenedor por primera vez, se ejecuta `init.sql` y se crean las tablas y datos de ejemplo.

### docker-compose.yml

- Servicio: **MySQL 8.0**.
- Puerto expuesto: **3306**.
- Base de datos: **0verpass**.
- Usuario: **0verpass** / contraseña: **0verpass**.
- El script `init.sql` se monta en `/docker-entrypoint-initdb.d/` para que se ejecute al inicializar el contenedor.

### init.sql

- Crea la base `0verpass` y las tablas:

| Tabla    | Uso |
|----------|-----|
| **persona** | Clientes: nombre, apellido, email, teléfono, documento, fecha de alta, activo, observaciones. |
| **pago**    | Cada cobro: persona, tipo (MENSUAL o DIARIO), monto, fecha de pago, vigencia desde/hasta, medio de pago. |

- Incluye índices para búsquedas por persona, tipo de pago y fechas.
- Inserta 3 personas y 3 pagos de ejemplo.

### Conexión

| Dato        | Valor      |
|-------------|------------|
| Host        | localhost  |
| Puerto      | 3306       |
| Base        | 0verpass   |
| Usuario     | 0verpass   |
| Contraseña  | 0verpass   |

---

## Aplicación Java (registro de pagos)

**Carpeta/código:** `src/main/java/...`, `pom.xml`, `src/main/resources/application.properties`

Aplicación de consola para el empleador: lista personas, listado de pagos y resumen de cobros en un rango de fechas.

### pom.xml

- **Java 17**
- Dependencias: **MySQL Connector/J**, **HikariCP** (pool de conexiones).
- Plugins: `maven-jar-plugin` (JAR ejecutable), `exec-maven-plugin` (ejecutar con `mvn exec:java`).
- Clase principal: `com.overpass.Main`.

### application.properties

- URL JDBC a `localhost:3306/0verpass`, usuario y contraseña.
- Zona horaria: `America/Argentina/Buenos_Aires`.
- Tamaño del pool HikariCP (por defecto 5).

### Modelos

- **Persona:** id, nombre, apellido, email, teléfono, documento, fecha de alta, activo, observaciones.
- **Pago:** id, persona, tipo (MENSUAL/DIARIO), monto, fechas de pago y vigencia, medio de pago; opcionalmente nombre de la persona para listados.

### DAOs (acceso a datos)

- **PersonaDao:** `findAll`, `findById`, `findByEmail`, `insert`, `update`.
- **PagoDao:** `findAllWithPersona`, `findByTipo`, `findByPersonaId`, `findById`, `insert`, `resumenEntre(desde, hasta)` (cantidad y total de mensuales y diarios en un rango de fechas).

### DataSource

- **DataSource.java:** lee `application.properties`, configura HikariCP y expone un `DataSource` para que los DAOs obtengan conexiones.

### Main.java

- Obtiene listas de personas y de pagos (con nombre de persona).
- Calcula el resumen del mes actual (mensuales y diarios).
- Imprime todo por consola.

### Cómo ejecutar

Desde la carpeta `climbing-wall` (con MySQL en marcha):

```bash
mvn compile exec:java -q -Dexec.mainClass="com.overpass.Main"
```

O generar el JAR y ejecutarlo:

```bash
mvn package -q
java -jar target/0verpass-registro-1.0.0.jar
```

---

## Scripts y utilidades

### servir.bat

- **Qué hace:** inicia un servidor HTTP con Python en el puerto **8080** y abre el navegador en `http://localhost:8080`.
- **Requisito:** tener Python instalado y en el PATH.
- **Uso:** ejecutar por doble clic o desde la terminal dentro de `climbing-wall`.

### README-BD.md

- Resumen rápido: levantar MySQL con Docker, ejecutar la app Java, estructura de tablas y comandos útiles.

---

## Poner todo en marcha

### 1. Ver la landing

```bash
# Opción A: con script (abre navegador)
servir.bat

# Opción B: abrir index.html con el navegador
```

### 2. Base de datos (para la app Java)

```bash
docker-compose up -d
docker ps   # comprobar que el contenedor está en marcha
```

### 3. App Java (registro de pagos)

```bash
mvn compile exec:java -q -Dexec.mainClass="com.overpass.Main"
```

### 4. Detener MySQL

```bash
docker-compose down
# Con datos incluidos: docker-compose down -v
```

---

## Subir a GitHub y ver la página

Para subir el proyecto a GitHub y dejar la landing visible en internet con **GitHub Pages**:

### 1. Crear un repositorio en GitHub

1. Entrá a [github.com](https://github.com) e iniciá sesión (o creá una cuenta).
2. Clic en **“New”** / **“New repository”**.
3. Nombre del repo (ej. `0verpass` o `climbing-wall`).
4. Dejalo **público**, sin marcar “Add a README” (ya tenés uno).
5. Clic en **“Create repository”**.

### 2. Subir el proyecto desde tu PC

En la terminal, desde la carpeta del proyecto:

```bash
cd C:\Users\drub\climbing-wall

git init
git add .
git commit -m "Proyecto 0verpass: landing, BD MySQL, app Java"
git branch -M main
git remote add origin https://github.com/TU_USUARIO/NOMBRE_DEL_REPO.git
git push -u origin main
```

Reemplazá `TU_USUARIO` por tu usuario de GitHub y `NOMBRE_DEL_REPO` por el nombre del repositorio que creaste. Si GitHub te pide autenticación, usá un **Personal Access Token** (Settings → Developer settings → Personal access tokens) en lugar de la contraseña.

### 3. Activar GitHub Pages para que se vea la web

1. En GitHub, abrí el repositorio.
2. **Settings** → en el menú izquierdo **Pages**.
3. En **“Source”** elegí **“Deploy from a branch”**.
4. En **“Branch”** elegí **main** y carpeta **/ (root)**.
5. Guardá con **Save**.

En unos minutos la página quedará publicada en:

**`https://TU_USUARIO.github.io/NOMBRE_DEL_REPO/`**

Por ejemplo: `https://miusuario.github.io/0verpass/`

Ahí se verá el `index.html` (la landing de 0verpass). Cada vez que hagas `git push` a `main`, la página se actualizará sola.

---

## Resumen por archivo

| Archivo / carpeta | Qué es |
|-------------------|--------|
| **index.html** | Landing del muro: diseño Violent Daimyo, tipografía graffiti (Permanent Marker) para “0verpass”, servicios, gimnasio, horarios, registro con Google, contacto (fondo@overpass.com), WhatsApp flotante. |
| **docker-compose.yml** | Define el contenedor MySQL (puerto 3306, base 0verpass). |
| **init.sql** | Crea tablas `persona` y `pago` e inserta datos de ejemplo. |
| **pom.xml** | Proyecto Maven (Java 17, MySQL, HikariCP). |
| **application.properties** | Configuración de conexión a MySQL para la app Java. |
| **Main.java** | Entrada de la app: lista personas, pagos y resumen del mes. |
| **PersonaDao.java** | CRUD y búsquedas de personas. |
| **PagoDao.java** | Altas y consultas de pagos; resumen por fechas. |
| **Persona.java** / **Pago.java** | Modelos de datos. |
| **DataSource.java** | Pool de conexiones a MySQL. |
| **servir.bat** | Sirve la web en el puerto 8080 y abre el navegador. |
| **README.md** | Este documento. |
| **README-BD.md** | Guía corta de base de datos y Java. |

Si querés ampliar alguna parte (por ejemplo solo la web, solo la BD o solo Java), se puede agregar una sección más detallada en este mismo README.
