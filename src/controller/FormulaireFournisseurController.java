package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Fournisseur;
import service.BDD;

public class FormulaireFournisseurController {
	
	private Connection connexion;
	
	@FXML private TextField idField;
    @FXML private TextField codeField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField adresseField;
    @FXML private Button enregistrerButton;
    
    @FXML
    public void initialize() {
    	try {
            connexion = BDD.getConnection();
            if (connexion == null) {
                throw new SQLException("Connexion à la base de données échouée.");
            }
            recupererNumero();

            enregistrerButton.setOnAction(e -> {
                try {
                    enregistrerFournisseur();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement.");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Initialisation échouée.");
        }
    }
    
    public void recupererNumero() {
        String query = "SELECT MAX(id) AS max_id FROM fournisseur";
        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                int maxNumero = rs.getInt("max_id");
                idField.setText(String.valueOf(maxNumero + 1));
            } else {
            	idField.setText("1");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void enregistrerFournisseur() throws Exception {
    	int id = Integer.valueOf(idField.getText());
        String code = codeField.getText();
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String adresse = adresseField.getText();

        if (code.isEmpty() || nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR, "Tous les champs doivent être remplis !");
            alert.showAndWait();
            return;
        }

        Fournisseur fournisseur = new Fournisseur(id, code, nom, prenom, adresse);
        if (connexion == null) {
            System.out.println("La connexion à la base de données n'a pas été initialisée.");
            return;
        }

        if (fournisseur.getCode() == null || fournisseur.getNom() == null || fournisseur.getPrenom() == null || fournisseur.getAdresse() == null) {
            System.out.println("Certains champs du fournisseur sont null.");
            return;
        }

        String query = "INSERT INTO fournisseur (Code, Nom, Prenom, Adresse) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            ps.setString(1, fournisseur.getCode());
            ps.setString(2, fournisseur.getNom());
            ps.setString(3, fournisseur.getPrenom());
            ps.setString(4, fournisseur.getAdresse());
            ps.executeUpdate();
            
            System.out.println("Fournisseur ajouté avec succès.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        Alert alert = new Alert(AlertType.INFORMATION, "Fournisseur enregistré avec succès !");
        alert.showAndWait();
        
        codeField.getScene().getWindow().hide();
    }
    
    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

