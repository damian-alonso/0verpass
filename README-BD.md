# 0verpass — Base de datos y app Java (registro de pagos)

Registro de personas que pagan el muro **mensualmente** o **diariamente**, para uso del empleador.

## Requisitos

- [Docker](https://www.docker.com/get-started) (para MySQL)
- [Java 17+](https://adoptium.net/) y [Maven](https://maven.apache.org/)

## 1. Levantar MySQL con Docker

Desde la carpeta del proyecto (donde está `docker-compose.yml`):

```bash
cd C:\Users\drub\climbing-wall
docker-compose up -d
```

Comprueba que el contenedor está en marcha:

```bash
docker ps
```

La base de datos `0verpass` se crea automáticamente y se ejecuta el script `init.sql` (tablas `persona` y `pago` + datos de ejemplo).

**Conexión MySQL:**

| Dato       | Valor     |
|-----------|-----------|
| Host      | localhost |
| Puerto    | 3306      |
| Base      | 0verpass  |
| Usuario   | 0verpass  |
| Contraseña| 0verpass  |
| Root      | root      |

## 2. Ejecutar la aplicación Java

Desde la misma carpeta del proyecto (`climbing-wall`):

```bash
mvn compile exec:java -q -Dexec.mainClass="com.overpass.Main"
```

O compilar y ejecutar el JAR:

```bash
mvn package -q
java -jar target/0verpass-registro-1.0.0.jar
```

La primera vez, Maven descargará las dependencias (MySQL Connector, HikariCP).

## Estructura de la base de datos

- **persona**: datos de cada cliente (nombre, apellido, email, teléfono, documento, etc.).
- **pago**: cada cobro con `tipo` = `MENSUAL` o `DIARIO`, monto, fechas de vigencia y medio de pago.

La aplicación Java incluye:

- **PersonaDao**: listar, buscar por id/email, alta y modificación de personas.
- **PagoDao**: listar pagos (con nombre de persona), por tipo (mensual/diario), por persona, y **resumen** de cobros en un rango de fechas para el empleador.
- **Main**: ejemplo que lista personas, últimos pagos y resumen del mes.

## Detener MySQL

```bash
docker-compose down
```

Para borrar también los datos del volumen:

```bash
docker-compose down -v
```
