package users;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import data.DBConnection;

public class AuthService {

    public static boolean login(String username, String password) {
        boolean autenticado = false;

        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT * FROM User WHERE username = ? AND password = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                autenticado = true;
                System.out.println("✅ Login correcto. Usuario: " + username + " | Rol: " + rs.getString("role"));
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("❌ Error de conexión: " + e.getMessage());
        }

        return autenticado;
    }

    public static String getRole(String username) {
        String role = null;
        try {
            Connection conn = DBConnection.connect();
            String sql = "SELECT role FROM User WHERE username = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                role = rs.getString("role");
            }

            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.err.println("❌ Error al obtener rol: " + e.getMessage());
        }

        return role;
    }
}
