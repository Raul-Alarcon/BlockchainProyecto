package models;

import controller.postgresDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class BlockChain {
    
    // Asumo que tienes Block.java y UtilBlockchain.java en el mismo paquete
    
    private final List<Block> cadena;
    private static final int DIFICULTAD = 4;
    private final postgresDAO db;

    public BlockChain() {
        this.cadena = new ArrayList<>();
        this.db = new postgresDAO(); // Inicializar el DAO
        
        // Crear bloque génesis
        Block genesis = new Block("Bloque génesis - smartContracts v1.0");
        this.cadena.add(genesis);
        
        // Guardar el nonce del génesis
        try {
            db.guardarNonce(genesis.getIndice(), genesis.getNonce(), genesis.getHashAnterior());
        } catch (SQLException e) {
             System.err.println("ADVERTENCIA: No se pudo guardar el Nonce del Génesis en la DB. " + e.getMessage());
        }
    }

    public Block obtenerUltimoBloque() {
        return cadena.get(cadena.size() - 1);
    }

    public void minarBloque(String datos) {
        Block ultimo = obtenerUltimoBloque();
        Block nuevoBloque = new Block(ultimo.getIndice() + 1, ultimo.getHash(), datos);

        // Prueba de trabajo (minado)
        while (!nuevoBloque.getHash().startsWith("0".repeat(DIFICULTAD))) {
            nuevoBloque.setNonce(nuevoBloque.getNonce() + 1);
        }

        cadena.add(nuevoBloque);
        System.out.println("Bloque minado: " + nuevoBloque.getHash());
        
        try {
            db.guardarNonce(
            nuevoBloque.getIndice(), 
            nuevoBloque.getNonce(), 
            nuevoBloque.getHashAnterior()
        );
            System.out.println("Nonce guardado en PostgreSQL para bloque #" + nuevoBloque.getIndice());
        } catch (SQLException e) {
            System.err.println("ERROR FATAL: No se pudo guardar el Nonce en la DB. " + e.getMessage());
        }
    }
    
    public boolean esValidaConBaseDeDatos() throws SQLException {
        // Empezamos desde el bloque 1 para dejar al génesis como ancla
        for (int i = 1; i < cadena.size(); i++) { 
            Block actual = cadena.get(i);
            Block anterior = cadena.get(i - 1);
            
            // 1. Obtener el Nonce de la Base de Datos
            int nonceDB = db.obtenerNonce(actual.getIndice());

            // 2. Recalcular el hash usando el nonce de la DB (Requisito b)
            // Creamos una copia para el recálculo
            Block bloqueCopia = new Block(
                actual.getIndice(), 
                actual.getHashAnterior(), 
                actual.getDatos()
            );
            bloqueCopia.setNonce(nonceDB); // Aplica el nonce de la DB
            String hashRecalculado = bloqueCopia.getHash(); 

            // 3. Comprobar la Integridad del Encadenamiento
            if (!actual.getHashAnterior().equals(anterior.getHash())) {
                System.out.println("Encadenamiento roto en bloque #" + actual.getIndice());
                return false;
            }
            
            // 4. Comprobar que el hash recalculado es igual al hash guardado (Requisito c)
            if (!hashRecalculado.equals(actual.getHash())) {
                System.out.println("Alteración detectada en el bloque #" + actual.getIndice());
                return false;
            }
            
            // 5. Comprobar que la Prueba de Trabajo (PoW) es válida
            String prefijoRequerido = "0".repeat(DIFICULTAD);
            if (!hashRecalculado.startsWith(prefijoRequerido)) {
                System.out.println("Prueba de Trabajo (PoW) inválida en bloque #" + actual.getIndice());
                return false;
            }
        }
        return true; 
    }

    public List<Block> getCadena() {
        return new ArrayList<>(cadena);
    }

    public int size() {
        return cadena.size();
    }
    
    public boolean esValida() { 
        for (int i = 1; i < cadena.size(); i++) {
            Block actual = cadena.get(i);
            Block anterior = cadena.get(i - 1);

            if (!actual.getHash().equals(actual.calcularHash())) {
                System.out.println("Hash inválido en bloque " + actual.getIndice());
                return false;
            }
            if (!actual.getHashAnterior().equals(anterior.getHash())) {
                System.out.println("Encadenamiento roto en bloque " + actual.getIndice());
                return false;
            }
        }
        return true;
    }
    // En BlockChain.java

@Override
public String toString() {
    StringBuilder cadenaCompleta = new StringBuilder();
    cadenaCompleta.append("====================== CADENA DE BLOQUES ======================\n");
    
    // Iterar sobre cada bloque en la lista 'cadena'
    for (Block bloque : this.cadena) {
        cadenaCompleta.append("\n---------------------------------------------------------------\n");
        // Usar la información del bloque (índice, hash, datos, etc.)
        cadenaCompleta.append("Bloque #").append(bloque.getIndice()).append("\n");
        cadenaCompleta.append("Hash: ").append(bloque.getHash()).append("\n");
        cadenaCompleta.append("Nonce: ").append(bloque.getNonce()).append("\n");
        cadenaCompleta.append("Hash Anterior: ").append(bloque.getHashAnterior()).append("\n");
        cadenaCompleta.append("Contratos:\n").append(bloque.getDatos()).append("\n");
    }
    
    cadenaCompleta.append("====================== FIN DE LA CADENA =======================");
    return cadenaCompleta.toString();
}
}