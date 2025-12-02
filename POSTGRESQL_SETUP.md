# 🗄️ Guía de Configuración PostgreSQL

## 📋 Requisitos Previos
- PostgreSQL 12+ instalado
- Usuario: `postgres`
- Contraseña: `root` (o cambiar en `postgresDAO.java`)

## 🚀 Instalación Rápida

### Paso 1: Instalar PostgreSQL
```bash
# Windows: Descargar desde https://www.postgresql.org/download/windows/
# Durante instalación, configurar:
# - Puerto: 5432
# - Usuario: postgres
# - Contraseña: root
```

### Paso 2: Ejecutar Script SQL
```bash
# Opción 1: Desde línea de comandos
psql -U postgres -f database_setup.sql

# Opción 2: Desde pgAdmin
# 1. Abrir pgAdmin
# 2. Conectarse al servidor
# 3. Click derecho en "Databases" → Create → Database
# 4. Nombre: blockchain_db
# 5. Abrir Query Tool
# 6. Copiar contenido de database_setup.sql
# 7. Ejecutar (F5)
```

### Paso 3: Verificar Conexión
```java
// En tu código Java:
postgresDAO dao = new postgresDAO();
if (dao.testConnection()) {
    System.out.println("✅ Conexión exitosa");
} else {
    System.out.println("❌ Error de conexión");
}
```

## 📊 Estructura de la Tabla

### Tabla: `nonces`
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | SERIAL | ID autoincremental |
| block_id | INT | ID del bloque |
| prev_hash | VARCHAR(64) | Hash del bloque anterior |
| nonce | INT | Nonce encontrado |
| hash | VARCHAR(64) | Hash resultante |
| timestamp | BIGINT | Timestamp del minado |
| created_at | TIMESTAMP | Fecha de creación |

### Restricciones
- **UNIQUE**: (block_id, prev_hash) - Evita duplicados
- **Índices**: prev_hash, block_id, timestamp - Optimiza búsquedas

## 🔧 Uso en el Código

### Guardar Nonce después de minar
```java
postgresDAO dao = new postgresDAO();
try {
    dao.guardarNonce(
        bloque.getId(),           // block_id
        bloque.getPreviousHash(), // prev_hash
        bloque.getNonce(),        // nonce
        bloque.getHash(),         // hash
        bloque.getTimeStamp()     // timestamp
    );
    System.out.println("Nonce guardado en BD");
} catch (SQLException e) {
    System.err.println("Error: " + e.getMessage());
}
```

### Buscar Nonce existente (optimización)
```java
postgresDAO dao = new postgresDAO();
try {
    Integer nonceGuardado = dao.obtenerNonce(
        blockId, 
        prevHash
    );
    
    if (nonceGuardado != null) {
        System.out.println("Nonce encontrado: " + nonceGuardado);
        // Usar nonce guardado en lugar de minar
    } else {
        System.out.println("No hay nonce guardado, minar nuevo bloque");
    }
} catch (SQLException e) {
    System.err.println("Error: " + e.getMessage());
}
```

## ⚠️ Solución de Problemas

### Error: "database does not exist"
```sql
-- Crear manualmente:
CREATE DATABASE blockchain_db;
```

### Error: "password authentication failed"
```java
// Cambiar en postgresDAO.java:
private static final String PASSWORD = "tu_contraseña";
```

### Error: "Connection refused"
```bash
# Verificar que PostgreSQL esté corriendo:
# Windows: Services → PostgreSQL
# Linux: sudo systemctl status postgresql
```

### Error: "Driver not found"
```
Verificar que postgresql-42.7.4.jar esté en:
- NetBeans: Libraries → Add JAR/Folder
- Proyecto: dist/lib/postgresql-42.7.4.jar
```

## 📝 Comandos Útiles PostgreSQL

```sql
-- Ver todas las bases de datos
\l

-- Conectarse a blockchain_db
\c blockchain_db

-- Ver todas las tablas
\dt

-- Ver estructura de tabla nonces
\d nonces

-- Ver todos los nonces guardados
SELECT * FROM nonces ORDER BY created_at DESC;

-- Limpiar tabla
TRUNCATE TABLE nonces;

-- Eliminar tabla
DROP TABLE nonces;

-- Eliminar base de datos
DROP DATABASE blockchain_db;
```

## ✅ Verificación Final

1. ✅ PostgreSQL instalado y corriendo
2. ✅ Base de datos `blockchain_db` creada
3. ✅ Tabla `nonces` creada con índices
4. ✅ Conexión desde Java exitosa
5. ✅ Métodos guardarNonce() y obtenerNonce() funcionando

## 🎯 Integración con BlockChain

Para integrar con tu clase BlockChain, modifica el método `mineBlock()`:

```java
public void mineBlock() {
    Block ultimoBloque = this.blockChain.get(this.blockChain.size() - 1);
    
    // Intentar obtener nonce de BD
    postgresDAO dao = new postgresDAO();
    try {
        Integer nonceGuardado = dao.obtenerNonce(
            ultimoBloque.getId(), 
            ultimoBloque.getPreviousHash()
        );
        
        if (nonceGuardado != null) {
            // Usar nonce guardado
            String hash = generateHash(ultimoBloque.toString() + nonceGuardado);
            ultimoBloque.register(nonceGuardado, hash);
            System.out.println("✅ Nonce recuperado de BD");
            return;
        }
    } catch (SQLException e) {
        System.out.println("⚠️ BD no disponible, minando normalmente");
    }
    
    // Minar normalmente si no hay nonce guardado
    String cad = ultimoBloque.toString();
    int nonce = 0;
    String sHash = "";
    
    while (true) {
        sHash = this.generateHash(cad + Integer.toString(nonce));
        if (sHash.substring(0, complexity).equals(this.proofOfWork)) {
            ultimoBloque.register(nonce, sHash);
            
            // Guardar en BD
            try {
                dao.guardarNonce(
                    ultimoBloque.getId(),
                    ultimoBloque.getPreviousHash(),
                    nonce,
                    sHash,
                    ultimoBloque.getTimeStamp()
                );
            } catch (SQLException e) {
                System.err.println("Error guardando nonce: " + e.getMessage());
            }
            break;
        }
        nonce++;
    }
}
```

---

**Nota**: La base de datos es OPCIONAL. El proyecto funciona perfectamente sin ella. Solo optimiza el minado reutilizando nonces.
