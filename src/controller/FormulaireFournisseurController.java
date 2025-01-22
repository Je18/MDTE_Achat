package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import model.Fournisseur;
import model.BDD;

public class FormulaireFournisseurController {
	
	private Connection connexion;

    @FXML private TextField codeField;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField adresseField;
    @FXML private Button enregistrerButton;
    
    @FXML
    public void initialize() {
        enregistrerButton.setOnAction(e -> {
			try {
				connexion = BDD.getConnection();
				enregistrerFournisseur();
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		});
    }

    private void enregistrerFournisseur() throws Exception {
        String code = codeField.getText();
        String nom = nomField.getText();
        String prenom = prenomField.getText();
        String adresse = adresseField.getText();

        if (code.isEmpty() || nom.isEmpty() || prenom.isEmpty() || adresse.isEmpty()) {
            Alert alert = new Alert(AlertType.ERROR, "Tous les champs doivent �tre remplis !");
            alert.showAndWait();
            return;
        }

        Fournisseur fournisseur = new Fournisseur(code, nom, prenom, adresse);
        if (connexion == null) {
            System.out.println("La connexion � la base de donn�es n'a pas �t� initialis�e.");
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
            
            System.out.println("Fournisseur ajout� avec succ�s.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        Alert alert = new Alert(AlertType.INFORMATION, "Fournisseur enregistr� avec succ�s !");
        alert.showAndWait();
        
        codeField.getScene().getWindow().hide();
    }
}

