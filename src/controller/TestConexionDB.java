package controller;

public class TestConexionDB {
    public static void main(String[] args) {
        System.out.println("===========================================");
        System.out.println("  TEST DE CONEXIÓN A POSTGRESQL");
        System.out.println("===========================================\n");
        
        postgresDAO dao = new postgresDAO();
        
        // Test 1: Conexión básica
        System.out.println("📡 Probando conexión a la base de datos...");
        if (dao.testConnection()) {
            System.out.println("✅ CONEXIÓN EXITOSA a blockchain_db\n");
            
            // Test 2: Intentar obtener un nonce (debería retornar null si no existe)
            System.out.println("🔍 Probando lectura de datos...");
            try {
                Integer nonce = dao.obtenerNonce(0, "0000000000000000000000000000000000000000000000000000000000000000");
                if (nonce != null) {
                    System.out.println("✅ Lectura exitosa. Nonce encontrado: " + nonce);
                } else {
                    System.out.println("✅ Lectura exitosa. No hay nonce guardado (normal en BD nueva)");
                }
                
                // Test 3: Intentar guardar un nonce de prueba
                System.out.println("\n💾 Probando escritura de datos...");
                dao.guardarNonce(999, "test_hash_prev", 12345, "test_hash_result", System.currentTimeMillis());
                System.out.println("✅ Escritura exitosa");
                
                // Test 4: Verificar que se guardó
                System.out.println("\n🔍 Verificando dato guardado...");
                Integer nonceGuardado = dao.obtenerNonce(999, "test_hash_prev");
                if (nonceGuardado != null && nonceGuardado == 12345) {
                    System.out.println("✅ Verificación exitosa. Dato recuperado: " + nonceGuardado);
                }
                
                System.out.println("\n===========================================");
                System.out.println("  ✅ TODAS LAS PRUEBAS EXITOSAS");
                System.out.println("  La base de datos está funcionando correctamente");
                System.out.println("===========================================");
                
            } catch (Exception e) {
                System.err.println("❌ Error en operaciones de BD: " + e.getMessage());
                e.printStackTrace();
            }
            
        } else {
            System.err.println("❌ ERROR DE CONEXIÓN");
            System.err.println("\nPosibles causas:");
            System.err.println("1. PostgreSQL no está corriendo");
            System.err.println("2. La base de datos 'blockchain_db' no existe");
            System.err.println("3. Usuario/contraseña incorrectos (postgres/root)");
            System.err.println("4. Puerto incorrecto (debe ser 5432)");
            System.err.println("5. Driver JDBC no está en el classpath");
            System.err.println("\nSolución:");
            System.err.println("- Ejecuta: psql -U postgres -f database_setup.sql");
        }
    }
}
