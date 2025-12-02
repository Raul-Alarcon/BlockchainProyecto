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
    public static String formatLastBlock(BlockChain blockchain, Block ultimoBloque) { 
        if (ultimoBloque == null) {
             return "La cadena está vacía.";
        }
        
        StringBuilder sb = new StringBuilder();
        // ... (resto del formato del encabezado del bloque)

        // Usamos el método de reporte que está dentro de BlockChain
        String reporteDatos = blockchain.contratosReport(ultimoBloque.getId()); 
        sb.append("DATOS (Contratos): \n").append(reporteDatos).append("\n");
        
        return sb.toString();
    }
}
