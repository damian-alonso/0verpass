-- Base de datos 0verpass: registro de pagos mensuales y diarios del muro

SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

USE 0verpass;

-- Personas que usan el muro (clientes)
CREATE TABLE persona (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  nombre        VARCHAR(100) NOT NULL,
  apellido      VARCHAR(100) NOT NULL,
  email         VARCHAR(255) NOT NULL,
  telefono      VARCHAR(30)  NULL,
  documento     VARCHAR(30)  NULL COMMENT 'DNI o documento de identidad',
  fecha_alta    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  activo        TINYINT(1)   NOT NULL DEFAULT 1,
  observaciones TEXT        NULL,
  UNIQUE KEY uk_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Registro de cada pago (mensual o diario)
CREATE TABLE pago (
  id              INT AUTO_INCREMENT PRIMARY KEY,
  persona_id      INT          NOT NULL,
  tipo            ENUM('MENSUAL', 'DIARIO') NOT NULL,
  monto           DECIMAL(10,2) NOT NULL,
  fecha_pago      DATE         NOT NULL,
  vigencia_desde  DATE         NOT NULL COMMENT 'Para mensual: inicio del mes; para diario: mismo día',
  vigencia_hasta  DATE         NOT NULL COMMENT 'Para mensual: fin del mes; para diario: mismo día',
  medio_pago      VARCHAR(50)  NULL COMMENT 'efectivo, transferencia, tarjeta, etc.',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pago_persona FOREIGN KEY (persona_id) REFERENCES persona (id) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Índices para consultas frecuentes
CREATE INDEX idx_pago_persona   ON pago (persona_id);
CREATE INDEX idx_pago_tipo      ON pago (tipo);
CREATE INDEX idx_pago_fecha     ON pago (fecha_pago);
CREATE INDEX idx_pago_vigencia  ON pago (vigencia_desde, vigencia_hasta);
CREATE INDEX idx_persona_activo ON persona (activo);

-- Datos de ejemplo (opcional)
INSERT INTO persona (nombre, apellido, email, telefono) VALUES
  ('María', 'García', 'maria.garcia@ejemplo.com', '+54 11 1234-5678'),
  ('Juan', 'Pérez', 'juan.perez@ejemplo.com', '+54 11 8765-4321'),
  ('Lucía', 'Fernández', 'lucia.f@ejemplo.com', NULL);

INSERT INTO pago (persona_id, tipo, monto, fecha_pago, vigencia_desde, vigencia_hasta, medio_pago) VALUES
  (1, 'MENSUAL', 15000.00, '2025-02-01', '2025-02-01', '2025-02-28', 'transferencia'),
  (2, 'DIARIO',  2500.00,  '2025-02-15', '2025-02-15', '2025-02-15', 'efectivo'),
  (3, 'MENSUAL', 15000.00, '2025-02-10', '2025-02-10', '2025-03-09', 'tarjeta');
