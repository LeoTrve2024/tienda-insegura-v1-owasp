-- ============================================================
-- INICIALIZACIÓN PORTABLE - TIENDA INSEGURA v1
-- Ejecutar sobre una base PostgreSQL vacía llamada tienda_insegura.
-- ============================================================

BEGIN;

-- ============================================================
-- TIENDA INSEGURA v1 - esquema reproducible
-- SOLO PARA LABORATORIO LOCAL AUTORIZADO
-- ============================================================

CREATE TABLE IF NOT EXISTS usuarios (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(120) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL, -- texto plano a propósito (A04)
    full_name VARCHAR(150),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(80) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS productos (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio NUMERIC(10,2) NOT NULL,
    stock INTEGER NOT NULL DEFAULT 0,
    imagen_url VARCHAR(255),
    categoria_id BIGINT REFERENCES categorias(id),
    CONSTRAINT uq_productos_nombre UNIQUE (nombre)
);

CREATE TABLE IF NOT EXISTS pedidos (
    id BIGSERIAL PRIMARY KEY,
    usuario_id BIGINT NOT NULL REFERENCES usuarios(id),
    total NUMERIC(10,2) NOT NULL,
    estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
    created_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS pedido_items (
    id BIGSERIAL PRIMARY KEY,
    pedido_id BIGINT NOT NULL REFERENCES pedidos(id) ON DELETE CASCADE,
    producto_id BIGINT NOT NULL REFERENCES productos(id),
    cantidad INTEGER NOT NULL,
    precio_unitario NUMERIC(10,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pedidos_usuario ON pedidos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_pedido_items_pedido ON pedido_items(pedido_id);
CREATE INDEX IF NOT EXISTS idx_productos_categoria ON productos(categoria_id);

-- ============================================================
-- TIENDA INSEGURA v1 - datos semilla
-- Passwords en TEXTO PLANO a propósito (A04).
-- Las credenciales se usan únicamente en el laboratorio local.
-- ============================================================

INSERT INTO usuarios (username, email, password, full_name, role)
VALUES
    ('admin', 'admin@tienda.local', 'admin123', 'Administrador General', 'ADMIN'),
    ('jperez', 'jperez@correo.com', 'password1', 'Juan Perez', 'USER'),
    ('mgarcia', 'mgarcia@correo.com', 'qwerty123', 'Maria Garcia', 'USER')
ON CONFLICT (username) DO NOTHING;

INSERT INTO categorias (nombre)
VALUES
    ('Electronica'),
    ('Hogar'),
    ('Ropa'),
    ('Deportes')
ON CONFLICT (nombre) DO NOTHING;

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url, categoria_id)
SELECT 'Audifonos Bluetooth',
       'Audifonos inalambricos con cancelacion de ruido',
       149.90, 25, NULL, c.id
FROM categorias c
WHERE c.nombre = 'Electronica'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Audifonos Bluetooth');

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url, categoria_id)
SELECT 'Cafetera Electrica',
       'Cafetera de goteo 12 tazas',
       89.50, 15, NULL, c.id
FROM categorias c
WHERE c.nombre = 'Hogar'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Cafetera Electrica');

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url, categoria_id)
SELECT 'Zapatillas Running',
       'Zapatillas livianas para correr',
       199.00, 40, NULL, c.id
FROM categorias c
WHERE c.nombre = 'Deportes'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Zapatillas Running');

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url, categoria_id)
SELECT 'Polo Basico',
       'Polo de algodon 100%',
       29.90, 100, NULL, c.id
FROM categorias c
WHERE c.nombre = 'Ropa'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Polo Basico');

INSERT INTO productos (nombre, descripcion, precio, stock, imagen_url, categoria_id)
SELECT 'Smartwatch',
       'Reloj inteligente con monitor cardiaco',
       349.00, 10, NULL, c.id
FROM categorias c
WHERE c.nombre = 'Electronica'
  AND NOT EXISTS (SELECT 1 FROM productos p WHERE p.nombre = 'Smartwatch');

-- Pedidos semilla para que la demostración IDOR sea reproducible sin
-- depender de compras manuales previas.
INSERT INTO pedidos (usuario_id, total, estado, created_at)
SELECT u.id, 149.90, 'PAGADO', NOW() - INTERVAL '2 days'
FROM usuarios u
WHERE u.username = 'jperez'
  AND NOT EXISTS (
      SELECT 1
      FROM pedidos p
      WHERE p.usuario_id = u.id
        AND p.total = 149.90
        AND p.estado = 'PAGADO'
  );

INSERT INTO pedidos (usuario_id, total, estado, created_at)
SELECT u.id, 89.50, 'PENDIENTE', NOW() - INTERVAL '1 day'
FROM usuarios u
WHERE u.username = 'mgarcia'
  AND NOT EXISTS (
      SELECT 1
      FROM pedidos p
      WHERE p.usuario_id = u.id
        AND p.total = 89.50
        AND p.estado = 'PENDIENTE'
  );

INSERT INTO pedido_items (pedido_id, producto_id, cantidad, precio_unitario)
SELECT p.id, pr.id, 1, 149.90
FROM pedidos p
JOIN usuarios u ON u.id = p.usuario_id
JOIN productos pr ON pr.nombre = 'Audifonos Bluetooth'
WHERE u.username = 'jperez'
  AND p.total = 149.90
  AND p.estado = 'PAGADO'
  AND NOT EXISTS (
      SELECT 1 FROM pedido_items pi
      WHERE pi.pedido_id = p.id AND pi.producto_id = pr.id
  );

INSERT INTO pedido_items (pedido_id, producto_id, cantidad, precio_unitario)
SELECT p.id, pr.id, 1, 89.50
FROM pedidos p
JOIN usuarios u ON u.id = p.usuario_id
JOIN productos pr ON pr.nombre = 'Cafetera Electrica'
WHERE u.username = 'mgarcia'
  AND p.total = 89.50
  AND p.estado = 'PENDIENTE'
  AND NOT EXISTS (
      SELECT 1 FROM pedido_items pi
      WHERE pi.pedido_id = p.id AND pi.producto_id = pr.id
  );

COMMIT;
