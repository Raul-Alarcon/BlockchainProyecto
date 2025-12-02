package models;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.io.Serializable;

import models.contratos;

public class BlockChain implements Serializable {

    private ArrayList<Block> blockChain;
    private int complexity;
    private String proofOfWork;

    public BlockChain(int iComplexity, String proofChar) {
        this.blockChain = new ArrayList<>();
        this.complexity = iComplexity;
        this.proofOfWork = "";
        for (int i = 0; i < this.complexity; i++) {
            this.proofOfWork += proofChar;
        }
    }

    public List<Block> getBlockChain() {
        return this.blockChain;
    }

    public boolean blockExist(Block blk) {
        for (int i = 0; i < this.blockChain.size(); i++) {
            if (this.blockChain.get(i).getId() == blk.getId()) {
                return true;
            }
        }
        return false;
    }

    public Block getBlock(int index) {
        return this.blockChain.get(index);
    }

    public Block getLastBlock() {
        return this.blockChain.get(this.blockChain.size() - 1);
    }

    public int size() {
        return this.blockChain.size();
    }

    public boolean createGenesis(ArrayList<contratos> dataContratos) {
        if (this.size() < 1) {
            // Usa el nuevo constructor
            Block tmpBlock = new Block(0, "0000000000000000000000000000000000000000000000000000000000000000", dataContratos); // <-- ¡Contratos aquí!
            this.blockChain.add(tmpBlock);
            this.mineBlock();
            return true; // Debería retornar true si fue exitoso
        }
        return false;
    }

    public void createBlock(ArrayList<contratos> dataContratos) {
        String prevHash = this.getLastBlock().getHash();
        // Usa el nuevo constructor
        this.blockChain.add(new Block(this.blockChain.size(), prevHash, dataContratos)); // <-- ¡Contratos aquí!
    }

    public boolean getProofOfWork_overBlock(Block blk) {
        String cad = blk.toString();
        int nonce = blk.getNonce();
        String sHash = "";
        sHash = this.generateHash(cad + Integer.toString(nonce));
        
        // Verificar que el hash calculado coincida con el almacenado
        if (!sHash.equals(blk.getHash())) {
            return false;
        }
        
        // Verificar que el hash cumpla con la prueba de trabajo (comience con los ceros requeridos)
        if (!sHash.substring(0, complexity).equals(this.proofOfWork)) {
            return false;
        }
        
        return true;
    }

    public boolean addProvedBlock(Block blk) {
        if (!this.blockExist(blk)) {
            if (this.getProofOfWork_overBlock(blk)) {
                this.blockChain.add(blk);
                return true;
            }
        }
        return false;
    }

    public void mineBlock() {
        Block ultimoBloque = this.blockChain.get(this.blockChain.size() - 1);
        String cad = ultimoBloque.toString();
        
        System.out.println("\n=== MINANDO BLOQUE #" + ultimoBloque.getId() + " ===");
        System.out.println("Data COMPLETA para minar: " + cad);
        System.out.println("Longitud data: " + cad.length());
        
        // Intentar obtener nonce de BD
        controller.postgresDAO dao = new controller.postgresDAO();
        try {
            Integer nonceGuardado = dao.obtenerNonce(
                ultimoBloque.getId(), 
                ultimoBloque.getPreviousHash()
            );
            
            if (nonceGuardado != null) {
                // VALIDAR que el nonce guardado funcione con los datos actuales
                String hashCalculado = this.generateHash(cad + nonceGuardado);
                if (hashCalculado.substring(0, complexity).equals(this.proofOfWork)) {
                    ultimoBloque.register(nonceGuardado, hashCalculado);
                    System.out.println("✅ Nonce recuperado de BD y validado");
                    System.out.println("Hash final: " + hashCalculado);
                    return;
                } else {
                    System.out.println("⚠️ Nonce de BD no válido para estos datos, minando nuevo...");
                }
            }
        } catch (Exception e) {
            System.out.println("⚠️ BD no disponible, minando normalmente");
        }
        
        // Minar normalmente
        int nonce = 0;
        String sHash = "";
        
        while (true) {
            sHash = this.generateHash(cad + Integer.toString(nonce));
            if (sHash.substring(0, complexity).equals(this.proofOfWork)) {
                ultimoBloque.register(nonce, sHash);
                System.out.println("✅ Bloque minado exitosamente");
                System.out.println("Nonce encontrado: " + nonce);
                System.out.println("Hash final: " + sHash);
                
                // Guardar en BD
                try {
                    dao.guardarNonce(
                        ultimoBloque.getId(),
                        ultimoBloque.getPreviousHash(),
                        nonce,
                        sHash,
                        ultimoBloque.getTimeStamp()
                    );
                    System.out.println("💾 Nonce guardado en BD");
                } catch (Exception e) {
                    System.err.println("Error guardando nonce: " + e.getMessage());
                }
                break;
            }
            nonce++;
        }
    }

    private String generateHash(String pCad) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pCad.getBytes("UTF-8"));
            StringBuffer hexadecimalString = new StringBuffer();
            for (int i = 0; i < hash.length; i++) {
                String hexadecimal = Integer.toHexString(0xff & hash[i]);
                if (hexadecimal.length() == 1) {
                    hexadecimalString.append('0');
                }
                hexadecimalString.append(hexadecimal);
            }
            return hexadecimalString.toString();
        } catch (Exception ee) {
            return null;
        }
    }

    // Dentro de BlockChain.java
    public double getContractualExposure(String pClient) {
        double positiveExposure = 0; // Parte B: valor a favor
        double negativeExposure = 0; // Parte A: valor en contra

        for (int i = 0; i < this.size(); i++) {
            models.Block currentBlock = this.getBlock(i);

            // Iteramos a través de la lista de contratos dentro de ese bloque
            for (models.contratos c : currentBlock.getListaContratos()) {
                // Si el cliente es la Parte B (Recibe valor/servicio)
                if (c.getParteB().equals(pClient)) {
                    positiveExposure += c.getValorTotal();
                } // Si el cliente es la Parte A (Otorga valor/paga)
                else if (c.getParteA().equals(pClient)) {
                    negativeExposure += c.getValorTotal();
                }
            }
        }

        // Retorna el saldo neto contractual
        return positiveExposure - negativeExposure;
    }

    public String contratosReport(int nBlock) // Usando el nombre adaptado
    {
        String sCad = "--- REPORTE DE CONTRATOS EN BLOQUE #" + nBlock + " ---\n";

        // 1. Obtenemos el bloque (asumiendo que getBlock(int) está disponible)
        models.Block blk = this.blockChain.get(nBlock);

        // 2. Iteramos a través de los contratos del bloque
        // Asumimos que los métodos en Block son getContrato() y countContratos()
        for (int i = 0; i < blk.countContratos(); i++) {
            // Obtenemos el objeto contrato
            models.contratos c = blk.getContrato(i);

            // 3. Imprimimos los detalles del Contrato
            sCad += "=================================================\n";
            sCad += "  CONTRATO ID: " + c.getIdContrato() + "\n";
            sCad += "  Valor Total: $" + String.format("%.2f", c.getValorTotal()) + "\n"; // Formato de dos decimales
            sCad += "  Partes: (" + c.getParteA() + " <---> " + c.getParteB() + ")\n";

            // 4. BUCLE ANIDADO: Agregamos los detalles de los SERVICIOS
            if (!c.getListaServicios().isEmpty()) {
                sCad += "  --- SERVICIOS ASOCIADOS (" + c.getListaServicios().size() + ") ---\n";

                // Iteramos sobre la lista de servicios dentro de CADA contrato
                for (models.servicio s : c.getListaServicios()) {
                    sCad += "\t- Servicio ID: " + s.getIdServicio() + "\n";
                    sCad += "\t  Descripción: " + s.getDescripcion() + "\n";
                    sCad += "\t  Estado: " + s.getEstado() + "\n";
                    sCad += "\t  Monto: $" + String.format("%.2f", s.getMontoServicio()) + "\n";
                    sCad += "=================================================\n";
                    sCad += "------------------ FIN REPORTE ------------------\n";
                }
            } else {
                sCad += "  --- Sin servicios asociados ---\n";
            }
        }
        return sCad;
    }

    public String getFullReport() {
        StringBuilder report = new StringBuilder();
        report.append("=================================================\n");
        report.append("         REPORTE COMPLETO DE LA CADENA           \n");
        report.append("=================================================\n");

        // Iteramos sobre todos los bloques, desde el 0 (Génesis) hasta el último.
        for (int i = 0; i < this.size(); i++) {
            // Información adicional del bloque (metadatos)
            report.append("  ID del Bloque: ").append(this.getBlock(i)).append("\n");
            report.append("  Hash del Bloque: ").append(this.getBlock(i).getHash()).append("\n");
            report.append("  Hash Anterior: ").append(this.getBlock(i).getPreviousHash()).append("\n");
            report.append("  timeStamp: ").append(this.getBlock(i).getTimeStamp()).append(" seg \n");
            report.append("  Nonce: ").append(this.getBlock(i).getNonce()).append("\n");
            report.append("-------------------------------------------------\n\n");
            report.append(this.contratosReport(i));
        }
        return report.toString();
    }

    public boolean saveChain(String fileName) {
        try (FileOutputStream fileOut = new FileOutputStream(fileName); ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
            out.writeObject(this);
            return true;
        } catch (IOException i) {
            System.out.println("Error al guardar la cadena: " + i.getMessage());
            i.printStackTrace(); // Para ver el detalle del error
            return false;
        }
    }

    public static BlockChain loadChain(String fileName) {
        BlockChain loadedChain = null;
        try (FileInputStream fileIn = new FileInputStream(fileName); ObjectInputStream in = new ObjectInputStream(fileIn)) {
            loadedChain = (BlockChain) in.readObject();
        } catch (IOException i) {
            // Error común si el archivo no existe o está corrupto
            System.out.println("Error de I/O al cargar: " + i.getMessage());
            return null;
        } catch (ClassNotFoundException c) {
            System.out.println("Clase de la cadena no encontrada.");
            return null;
        }
        return loadedChain;
    }

    public boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;

        for (int i = 1; i < this.blockChain.size(); i++) {
            currentBlock = this.blockChain.get(i);
            previousBlock = this.blockChain.get(i - 1);

            // --- 1. Verificar la integridad del Hash Propio (PoW) ---
            String blockData = currentBlock.toString();
            String calculatedHash = this.generateHash(blockData + currentBlock.getNonce());
            System.out.println("\n=== VALIDANDO BLOQUE #" + currentBlock.getId() + " ===");
            System.out.println("Hash almacenado: " + currentBlock.getHash());
            System.out.println("Hash calculado:  " + calculatedHash);
            System.out.println("Nonce: " + currentBlock.getNonce());
            System.out.println("Data COMPLETA: " + blockData);
            System.out.println("Longitud data: " + blockData.length());
            
            if (!this.getProofOfWork_overBlock(currentBlock)) {
                System.out.println("❌ Bloque #" + currentBlock.getId() + ": Hash no es válido (Falla PoW).");
                return false;
            }

            // --- 2. Verificar la integridad del Enlace ---
            if (!previousBlock.getHash().equals(currentBlock.getPreviousHash())) {
                System.out.println("❌ Bloque #" + previousBlock.getId() + " y #" + currentBlock.getId() + ": Enlace de Hash Roto.");
                return false;
            }
            System.out.println("✅ Bloque #" + currentBlock.getId() + " es válido");
        }
        return true;
    }

    @Override
    public String toString() {

        String blockChain = "";

        for (Block block : this.blockChain) {
            blockChain += block.toString() + "\n";
        }

        return blockChain;
    }
}
