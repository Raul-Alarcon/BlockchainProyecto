/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controller;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author Raul
 */
public class postgresDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/smart_bc"; 
    private static final String USER = "postgres"; 
    private static final String PASSWORD = "root"; 

    private Connection conectar() throws SQLException {
        try {
            // Carga el driver JDBC (que ya agregaste a las librerías)
            Class.forName("org.postgresql.Driver"); 
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error: No se encontró el driver de PostgreSQL. ¿Está el JAR en el proyecto?");
        }
        // Crea la conexión
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
public void guardarNonce(int indiceBloque, int nonce, String hashAnterior) throws SQLException {
    
    // El SQL ahora incluye la nueva columna y el valor
    String SQL = "INSERT INTO nonces_blockchain (indice_bloque, nonce, hash_anterior) VALUES (?, ?, ?) " +
                 "ON CONFLICT (indice_bloque) DO UPDATE SET nonce = EXCLUDED.nonce, hash_anterior = EXCLUDED.hash_anterior"; 

    try (Connection conn = conectar();
         PreparedStatement pstmt = conn.prepareStatement(SQL)) {

        pstmt.setInt(1, indiceBloque);
        pstmt.setInt(2, nonce);
        pstmt.setString(3, hashAnterior);
        pstmt.executeUpdate();

    } catch (SQLException e) {
        System.err.println("Error al guardar Nonce/HashAnterior en DB: " + e.getMessage());
        throw e;
    }
}

    public int obtenerNonce(int indiceBloque) throws SQLException {
        String SQL = "SELECT nonce FROM nonces_blockchain WHERE indice_bloque = ?";
        int nonceDB = -1;

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {

            pstmt.setInt(1, indiceBloque);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    nonceDB = rs.getInt("nonce");
                }
            }
        } 
        
        if (nonceDB == -1) {
            throw new SQLException("El nonce del bloque #" + indiceBloque + " no se encontró en la base de datos.");
        }
        
        return nonceDB;
    }
}
