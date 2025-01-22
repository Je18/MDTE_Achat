package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.Achat;
import model.BDD;
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

public class AchatController {

    private Connection connexion;

    @FXML
    private TableView<Achat> achatsTable;

    @FXML
    private TableColumn<Achat, Integer> numeroColumn;
    @FXML
    private TableColumn<Achat, String> composantsColumn;
    @FXML
    private TableColumn<Achat, String> prixColumn;
    @FXML
    private TableColumn<Achat, String> statusColumn;

    @FXML
    private Button btnAddFournisseur;
    @FXML
    private Button btnAddAchat;
    @FXML
    private Button btnRefresh;

    public AchatController() {
        try {
            connexion = BDD.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données.");
        }
    }

    @FXML
    public void initialize() {
    	numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
    	composantsColumn.setCellValueFactory(new PropertyValueFactory<>("composants"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));
        
        loadAchats();
    }

    private void loadAchats() {
        if (connexion == null) {
            showAlert("Erreur", "La connexion à la base de données n'est pas établie.");
            return;
        }

        String query =
                "SELECT * FROM achat";

        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            List<Achat> achats = new ArrayList<>();

            while (rs.next()) {
                int numero = rs.getInt("numero");
                String composantsString = rs.getString("composants");
                List<String> composants = new ArrayList<>();
                if (composantsString != null && !composantsString.isEmpty()) {
                    String[] composantsArray = composantsString.split(",");
                    for (String composant : composantsArray) {
                        composants.add(composant.trim());
                    }
                }

                String prix = rs.getString("prix");
                String status = rs.getString("status");

                Achat achat = new Achat(numero, composants, Integer.parseInt(prix), status);
                achats.add(achat);
            }

            achatsTable.getItems().clear();
            achatsTable.getItems().addAll(achats);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Une erreur s'est produite lors de la récupération des achats");
        }
    }
    
    @FXML
    private void handleAddAchat() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/formulaireAchat.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Achat");
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.initOwner(btnAddFournisseur.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout d'un achat");
        }
    }
    
    @FXML
    private void refresh() throws SQLException {
    	loadAchats();
        
        achatsTable.refresh();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
