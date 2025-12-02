-- ============================================
-- SCRIPT DE CONFIGURACIÓN DE BASE DE DATOS
-- Proyecto: Blockchain de Contratos Inteligentes
-- ============================================

-- 1. Crear base de datos (ejecutar como superusuario)
-- DROP DATABASE IF EXISTS blockchain_db;
CREATE DATABASE blockchain_db;

-- 2. Conectarse a la base de datos
\c blockchain_db

-- 3. Crear tabla de nonces
CREATE TABLE IF NOT EXISTS nonces (
    id SERIAL PRIMARY KEY,
    block_id INT NOT NULL,
    prev_hash VARCHAR(64) NOT NULL,
    nonce INT NOT NULL,
    hash VARCHAR(64) NOT NULL,
    timestamp BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_block_prev_hash UNIQUE(block_id, prev_hash)
);

-- 4. Crear índices para optimización
CREATE INDEX IF NOT EXISTS idx_prev_hash ON nonces(prev_hash);
CREATE INDEX IF NOT EXISTS idx_block_id ON nonces(block_id);
CREATE INDEX IF NOT EXISTS idx_timestamp ON nonces(timestamp);

-- 5. Insertar datos de prueba (opcional)
INSERT INTO nonces (block_id, prev_hash, nonce, hash, timestamp) 
VALUES 
    (0, '0000000000000000000000000000000000000000000000000000000000000000', 12345, '0000abcd1234567890abcdef1234567890abcdef1234567890abcdef12345678', 1234567890),
    (1, '0000abcd1234567890abcdef1234567890abcdef1234567890abcdef12345678', 67890, '00001234abcdef567890abcdef1234567890abcdef1234567890abcdef123456', 1234567900)
ON CONFLICT (block_id, prev_hash) DO NOTHING;

-- 6. Verificar datos
SELECT * FROM nonces;

-- 7. Mostrar estructura de la tabla
\d nonces

-- ============================================
-- COMANDOS ÚTILES
-- ============================================
-- Ver todas las tablas: \dt
-- Ver estructura: \d nonces
-- Eliminar tabla: DROP TABLE nonces;
-- Eliminar BD: DROP DATABASE blockchain_db;
