package controller;

import models.Usuario;
import java.sql.*;
import java.security.MessageDigest;
import java.util.Base64;

public class UsuarioDAO {
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

    public void crearTablaUsuarios() throws SQLException {
        String SQL = "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id SERIAL PRIMARY KEY," +
                "usuario VARCHAR(50) UNIQUE NOT NULL," +
                "contrasena VARCHAR(255) NOT NULL," +
                "email VARCHAR(100)," +
                "rol VARCHAR(20) DEFAULT 'usuario'," +
                "activo BOOLEAN DEFAULT true," +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection conn = conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(SQL);
            System.out.println("✅ Tabla usuarios creada o ya existe");
        } catch (SQLException e) {
            System.err.println("Error al crear tabla usuarios: " + e.getMessage());
            throw e;
        }
    }

    public boolean registrarUsuario(Usuario usuario) throws SQLException {
        String SQL = "INSERT INTO usuarios (usuario, contrasena, email, rol) VALUES (?, ?, ?, ?)";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, usuario.getUsuario());
            pstmt.setString(2, hashPassword(usuario.getContrasena()));
            pstmt.setString(3, usuario.getEmail());
            pstmt.setString(4, usuario.getRol());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    public Usuario autenticar(String usuario, String contrasena) throws SQLException {
        String SQL = "SELECT id, usuario, email, rol, activo FROM usuarios WHERE usuario = ? AND contrasena = ? AND activo = true";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, usuario);
            pstmt.setString(2, hashPassword(contrasena));

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Usuario(
                            rs.getInt("id"),
                            rs.getString("usuario"),
                            rs.getString("email"),
                            rs.getString("rol"),
                            rs.getBoolean("activo")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al autenticar: " + e.getMessage());
        }
        return null;
    }

    public boolean usuarioExiste(String usuario) throws SQLException {
        String SQL = "SELECT COUNT(*) FROM usuarios WHERE usuario = ?";

        try (Connection conn = conectar();
             PreparedStatement pstmt = conn.prepareStatement(SQL)) {
            pstmt.setString(1, usuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }

    public boolean testConnection() {
        try (Connection conn = conectar()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("✅ Conexión a BD exitosa");
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("❌ Error de conexión: " + e.getMessage());
            return false;
        }
    }
}
