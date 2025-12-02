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
    private static final String URL = "jdbc:postgresql://localhost:5432/blockchain_db?currentSchema=public"; 
    private static final String USER = "postgres"; 
    private static final String PASSWORD = "root"; 

    private Connection conectar() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver"); 
        } catch (ClassNotFoundException e) {
            throw new SQLException("Error: No se encontró el driver de PostgreSQL.");
        }
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
    
    public void guardarNonce(int blockId, String prevHash, int nonce, String hash, long timestamp) throws SQLException {
        String SQL = "INSERT INTO public.nonces (block_id, prev_hash, nonce, hash, timestamp) " +
                     "VALUES (?, ?, ?, ?, ?) " +
                     "ON CONFLICT (block_id, prev_hash) DO UPDATE " +
                     "SET nonce = EXCLUDED.nonce, hash = EXCLUDED.hash, timestamp = EXCLUDED.timestamp";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, blockId);
            pstmt.setString(2, prevHash);
            pstmt.setInt(3, nonce);
            pstmt.setString(4, hash);
            pstmt.setLong(5, timestamp);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error al guardar nonce: " + e.getMessage());
            throw e;
        }
    }

    public Integer obtenerNonce(int blockId, String prevHash) throws SQLException {
        String SQL = "SELECT nonce FROM public.nonces WHERE block_id = ? AND prev_hash = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setInt(1, blockId);
            pstmt.setString(2, prevHash);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("nonce");
                }
            }
        }
        return null;
    }
    
    public boolean testConnection() {
        try (Connection conn = conectar()) {
            if (conn != null && !conn.isClosed()) {
                // Mostrar información de la conexión
                System.out.println("  📍 Base de datos: " + conn.getCatalog());
                System.out.println("  👤 Usuario: " + conn.getMetaData().getUserName());
                System.out.println("  🔗 URL: " + conn.getMetaData().getURL());
                
                // Verificar si la tabla existe
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(
                    "SELECT COUNT(*) FROM information_schema.tables " +
                    "WHERE table_schema = 'public' AND table_name = 'nonces'"
                );
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println("  📊 Tabla 'nonces' encontrada: " + (count > 0 ? "SÍ" : "NO"));
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error de conexión: " + e.getMessage());
            return false;
        }
    }
}
