package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.BDD;
import model.Fournisseur;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class FournisseurController {

    private Connection connexion;

    @FXML
    private TableView<Fournisseur> produitsTable;

    @FXML
    private TableColumn<Fournisseur, String> codeColumn;
    @FXML
    private TableColumn<Fournisseur, String> nomColumn;
    @FXML
    private TableColumn<Fournisseur, String> prenomColumn;
    @FXML
    private TableColumn<Fournisseur, String> adresseColumn;

    @FXML
    private Button btnAddFournisseur;
    @FXML
    private Button btnAddAchat;
    @FXML
    private Button btnRefresh;

    public FournisseurController() {
        try {
            connexion = BDD.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données.");
        }
    }

    @FXML
    public void initialize() {
    	codeColumn.setCellValueFactory(new PropertyValueFactory<>("code"));
    	nomColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        prenomColumn.setCellValueFactory(new PropertyValueFactory<>("prenom"));
        adresseColumn.setCellValueFactory(new PropertyValueFactory<>("adresse"));
        
        loadFournisseurs();
    }

    private void loadFournisseurs() {
        if (connexion == null) {
            showAlert("Erreur", "La connexion à la base de données n'est pas établie.");
            return;
        }

        String query =
                "SELECT * FROM fournisseur";

        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            List<Fournisseur> fournisseurs = new ArrayList<>();

            while (rs.next()) {
            	int id = rs.getInt("id");
            	String code = rs.getString("code");
            	String nom = rs.getString("nom");
            	String prenom = rs.getString("prenom");
            	String adresse = rs.getString("adresse");

                Fournisseur fournisseur = new Fournisseur(id, code, nom, prenom, adresse);

                fournisseurs.add(fournisseur);
                
            }

            produitsTable.getItems().clear();
            produitsTable.getItems().addAll(fournisseurs);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Une erreur s'est produite lors de la récupération des produits");
        }
    }
    
    @FXML
    private void handleAddFournisseur() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/formulaireFournisseur.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Fournisseurs");
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.initOwner(btnAddFournisseur.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout de fournisseur.");
        }
    }
    
    @FXML
    private void refresh() throws SQLException {
        loadFournisseurs();
        
        produitsTable.refresh();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
