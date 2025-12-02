-- ============================================
-- SCRIPT DE VERIFICACIÓN RÁPIDA
-- ============================================

-- Conectarse a la base de datos
\c blockchain_db

-- Verificar que la tabla existe
SELECT 'Tabla nonces existe' AS status 
FROM information_schema.tables 
WHERE table_name = 'nonces';

-- Ver estructura de la tabla
\d nonces

-- Contar registros
SELECT COUNT(*) as total_registros FROM nonces;

-- Ver todos los datos
SELECT * FROM nonces ORDER BY created_at DESC;

-- Información de la conexión
SELECT current_database() as base_datos, 
       current_user as usuario,
       version() as version_postgres;
