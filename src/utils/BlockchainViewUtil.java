/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import models.Block;
import models.BlockChain;
/**
 *
 * @author Raul
 */
public class BlockchainViewUtil {
    public static String formatLastBlock(Block ultimoBloque) {
        if (ultimoBloque == null) {
             return "La cadena está vacía.";
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append("--- ÚLTIMO BLOQUE DE LA CADENA ---\n");
        sb.append("  Índice: ").append(ultimoBloque.getIndice()).append("\n");
        sb.append("  Timestamp: ").append(ultimoBloque.getTimestamp()).append(" seg\n");
        // ... (resto de las líneas de formato)
        sb.append("  Hash Anterior: ").append(ultimoBloque.getHashAnterior()).append("\n");
        sb.append("  Hash Actual: ").append(ultimoBloque.getHash()).append("\n");
        sb.append("--------------------------------------\n");
        sb.append("DATOS (Contratos): \n").append(ultimoBloque.getDatos()).append("\n");
        return sb.toString();
    }
}
