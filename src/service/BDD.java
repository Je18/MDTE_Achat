package service;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class BDD {
    private Connection conn;

    public static Connection getConnection() throws Exception {
        try {
            String url = "jdbc:mysql://localhost:3306/achat?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8";
            String username = "root";
            String password = "";
            Class.forName("com.mysql.jdbc.Driver");

            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected");
            return conn;
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception("Database connection failed.");
        }
    }

    public void exportCommandeStock(String filepath) {
        String query = "SELECT * FROM achat WHERE status = 0"; 
        try (PreparedStatement ps = conn.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            FileWriter writer = new FileWriter(filepath);
            while (rs.next()) {
                int numero = rs.getInt("numero");
                String composants = rs.getString("composants");
                double prix = rs.getDouble("prix");

                writer.write("Commande N°: " + numero + ", Composant: " + composants + ", Prix: " + prix + "\n");

                updateCommandeEnvoyee(numero);
            }
            writer.close();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void updateCommandeEnvoyee(int numeroCommande) {
        String query = "UPDATE achats SET envoyee = 1 WHERE numero = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, numeroCommande);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Connection closed.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
