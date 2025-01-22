package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Achat;
import model.Fournisseur;
import model.BDD;

public class FormulaireAchatController {
	
	private Connection connexion;

    @FXML private TextField numeroField;
    @FXML private ComboBox<Fournisseur> fournisseurComboBox;
    @FXML private TextField composantsField;
    @FXML private TextField prixField;
    @FXML private Button enregistrerButton;

    @FXML
    public void initialize() throws Exception {
        
        enregistrerButton.setOnAction(e -> {
			try {
				connexion = BDD.getConnection();
				enregistrerAchat();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
    }
    
    public void recupererNumero() throws SQLException {
        String query = "SELECT MAX(numero) AS max_numero FROM achat";
        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int maxNumero = rs.getInt("max_numero");
                numeroField.setText(String.valueOf(maxNumero + 1));
            } else {
                numeroField.setText("1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    private void enregistrerAchat() throws Exception {
        int numero = Integer.parseInt(numeroField.getText());
        Fournisseur fournisseur = fournisseurComboBox.getValue();
        String composantsString = composantsField.getText();  
        double prix = Double.parseDouble(prixField.getText());

        if (composantsString.isEmpty() || prix <= 0 || fournisseur == null) {
            Alert alert = new Alert(AlertType.ERROR, "Veuillez remplir tous les champs correctement.");
            alert.showAndWait();
            return;
        }
        
        List<String> composants = new ArrayList<>();
        String[] composantsArray = composantsString.split(","); 
        for (String composant : composantsArray) {
            composants.add(composant.trim()); 
        }

        Achat achat = new Achat(numero, composants, (int)prix, fournisseur.getNom());

        if (connexion == null) {
            System.out.println("La connexion à la base de données n'a pas été initialisée.");
            return;
        }

        String query = "INSERT INTO achat (numero, composants, prix, status, fournisseurId) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setInt(1, achat.getNumero()); 
            ps.setString(2, String.join(",", composants)); 
            ps.setDouble(3, achat.getPrix());
            ps.setInt(4, 0);
            ps.setString(5, fournisseur.getCode()); 
            
            ps.executeUpdate();
            System.out.println("Achat ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        Alert alert = new Alert(AlertType.INFORMATION, "Achat enregistré avec succès !");
        alert.showAndWait();
        
        numeroField.getScene().getWindow().hide();
    }

}

