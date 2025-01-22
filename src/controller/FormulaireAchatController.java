package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.Button;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.StringConverter;
import model.Fournisseur;
import model.BDD;

public class FormulaireAchatController {

    private Connection connexion;

    @FXML private TextField numeroField;
    @FXML private ComboBox<Fournisseur> fournisseurComboBox;
    @FXML private ComboBox<String> composantsComboBox;
    @FXML private TextField prixField;
    @FXML private Button enregistrerButton;
    @FXML private Button annulerButton;
    @FXML private ListView<String> listViewComposants;

    @FXML
    public void initialize() {
        try {
            connexion = BDD.getConnection();
            if (connexion == null) {
                throw new SQLException("Connexion � la base de donn�es �chou�e.");
            }
            recupererNumero();
            chargerFournisseurs();
            selectionnerComposant();

            enregistrerButton.setOnAction(e -> {
                try {
                    System.out.println("Achat fait");
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement.");
                }
            });

            annulerButton.setOnAction(e -> reinitialiserFormulaire());

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Initialisation �chou�e.");
        }
    }

    public void recupererNumero() {
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
            showAlert(AlertType.ERROR, "Erreur", "Impossible de r�cup�rer le num�ro.");
        }
    }

    private void chargerFournisseurs() {
        String query = "SELECT * FROM fournisseur";
        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String code = rs.getString("code");
                String nom = rs.getString("nom");
                String prenom = rs.getString("prenom");
                String adresse = rs.getString("adresse");

                fournisseurComboBox.getItems().add(new Fournisseur(id, code, nom, prenom, adresse));
            }

            fournisseurComboBox.setConverter(new StringConverter<Fournisseur>() {
                @Override
                public String toString(Fournisseur fournisseur) {
                    return fournisseur != null ? fournisseur.getNom() + " " + fournisseur.getPrenom() + " (" + fournisseur.getCode() + ")" : "";
                }

                @Override
                public Fournisseur fromString(String string) {
                    return null;
                }
            });

            fournisseurComboBox.valueProperty().addListener((obs, oldFournisseur, newFournisseur) -> {
                if (newFournisseur != null) {
                    chargerComposantsPourFournisseur(newFournisseur);
                    fournisseurComboBox.setDisable(true); 
                }
            });

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger les fournisseurs.");
        }
    }

    private void chargerComposantsPourFournisseur(Fournisseur fournisseur) {
        String query = "SELECT produit, prix FROM produits WHERE fournisseurId = ?";
        composantsComboBox.getItems().clear();

        try (PreparedStatement ps = connexion.prepareStatement(query)) {
            System.out.println(fournisseur.getId());
            ps.setInt(1, fournisseur.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    composantsComboBox.getItems().add(rs.getString("produit") + " - " + rs.getString("prix") + " �");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger les composants pour le fournisseur s�lectionn�.");
        }
    }
    
    private void selectionnerComposant() {
    	composantsComboBox.getSelectionModel().selectedItemProperty().addListener((options, oldValue, newValue) -> {
    		   System.out.println(newValue);
    		   listViewComposants.getItems().add(newValue);
    	}); 
    }

    private void reinitialiserFormulaire() {
        fournisseurComboBox.setDisable(false); 
        fournisseurComboBox.getSelectionModel().clearSelection(); 
        composantsComboBox.getItems().clear(); 
        listViewComposants.getItems().clear(); 
        prixField.clear(); 
    }

    private void showAlert(AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
