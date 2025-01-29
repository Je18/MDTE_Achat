package service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CSV {
    private Connection connexion;

    public CSV(Connection connexion) {
        this.connexion = connexion;
    }

    public int recupererNumero() {
        String query = "SELECT MAX(numero) AS max_numero FROM achat";
        try (PreparedStatement stmt = connexion.prepareStatement(query); ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("max_numero") + 1;
            } else {
                return 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean importCSVToDatabase(String filePath) {
        String sqlInsert = "INSERT INTO achat (numero, composants, prix, status, fournisseurId) VALUES (?, ?, ?, ?, ?)";
        String sqlSelectProduit = "SELECT fournisseurId, prix, qte FROM produits WHERE id = ?";
        String sqlUpdateQte = "UPDATE produits SET qte = ? WHERE id = ?";

        try (BufferedReader br = new BufferedReader(new FileReader(filePath));
             PreparedStatement pstmtInsert = connexion.prepareStatement(sqlInsert);
             PreparedStatement pstmtSelect = connexion.prepareStatement(sqlSelectProduit);
             PreparedStatement pstmtUpdateQte = connexion.prepareStatement(sqlUpdateQte)) {

            String line;
            List<String> composantsList = new ArrayList<>();
            int totalPrix = 0;
            int fournisseurId = -1;
            int numeroAchat = recupererNumero();

            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 2) {
                    int produitId = Integer.parseInt(values[0].trim());
                    int quantite = Integer.parseInt(values[1].trim());

                    pstmtSelect.setInt(1, produitId);
                    try (ResultSet rs = pstmtSelect.executeQuery()) {
                        if (rs.next()) {
                            fournisseurId = rs.getInt("fournisseurId");
                            int prix = rs.getInt("prix");
                            int ancienneQte = rs.getInt("qte");
                            int nouvelleQte = ancienneQte - quantite;

                            totalPrix += prix * quantite;
                            composantsList.add(produitId + "(" + quantite + ")");

                            pstmtUpdateQte.setInt(1, nouvelleQte);
                            pstmtUpdateQte.setInt(2, produitId);
                            pstmtUpdateQte.executeUpdate();
                        }
                    }
                }
            }

            if (!composantsList.isEmpty()) {
                String composants = String.join(",", composantsList);
                pstmtInsert.setInt(1, numeroAchat);
                pstmtInsert.setString(2, composants);
                pstmtInsert.setInt(3, totalPrix);
                pstmtInsert.setInt(4, 0);
                pstmtInsert.setInt(5, fournisseurId);
                pstmtInsert.executeUpdate();
            }

            return true;
        } catch (IOException | SQLException | NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }
}
