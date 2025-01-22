package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import model.BDD;
import model.Produits;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    private Connection connexion;

    @FXML
    private TableView<Produits> produitsTable;

    @FXML
    private TableColumn<Produits, String> typeColumn;
    @FXML
    private TableColumn<Produits, String> produitColumn;
    @FXML
    private TableColumn<Produits, Integer> prixColumn;
    @FXML
    private TableColumn<Produits, Integer> qteColumn;
    @FXML
    private TableColumn<String, String> fournisseurColumn;

    @FXML
    private Button btnAddFournisseur;
    @FXML
    private Button btnAddAchat;
    @FXML
    private Button btnRefresh;

    public MainController() {
        try {
            connexion = BDD.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Erreur de connexion", "Impossible de se connecter à la base de données.");
        }
    }

    @FXML
    public void initialize() {
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        produitColumn.setCellValueFactory(new PropertyValueFactory<>("produit"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        qteColumn.setCellValueFactory(new PropertyValueFactory<>("qte"));
        fournisseurColumn.setCellValueFactory(new PropertyValueFactory<>("fournisseurNomPrenom"));
        
        prixColumn.setCellFactory(new Callback<TableColumn<Produits, Integer>, TableCell<Produits, Integer>>() {
            @Override
            public TableCell<Produits, Integer> call(TableColumn<Produits, Integer> param) {
                return new TableCell<Produits, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item + " €");
                        }
                    }
                };
            }
        });
        qteColumn.setCellFactory(new Callback<TableColumn<Produits, Integer>, TableCell<Produits, Integer>>() {
            @Override
            public TableCell<Produits, Integer> call(TableColumn<Produits, Integer> param) {
                return new TableCell<Produits, Integer>() {
                    @Override
                    protected void updateItem(Integer item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item.toString());

                            if (item == 0) {
                                setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                            } else {
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });
        
        loadProduits();
    }

    private void loadProduits() {
        if (connexion == null) {
            showAlert("Erreur", "La connexion à la base de données n'est pas établie.");
            return;
        }

        String query =
                "SELECT produits.*, " +
                "CONCAT(fournisseur.nom, ' ', fournisseur.prenom) AS fournisseur_nom_prenom " +
                "FROM produits " +
                "JOIN fournisseur ON produits.fournisseurId = fournisseur.id ORDER BY produits.qte DESC";

        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            List<Produits> produits = new ArrayList<>();

            while (rs.next()) {
            	int id = rs.getInt("id");
                String type = rs.getString("type");
                String prod = rs.getString("produit");
                int prix = rs.getInt("prix");
                int qte = rs.getInt("qte");
                int fournisseurID = rs.getInt("fournisseurId");
                String fournisseurNomPrenom = rs.getString("fournisseur_nom_prenom");

                Produits produit = new Produits(id, type, prod, prix, qte, fournisseurID);

                produit.setFournisseurNomPrenom(fournisseurNomPrenom);

                produits.add(produit);
                
            }

            produitsTable.getItems().clear();
            produitsTable.getItems().addAll(produits);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("Erreur SQL", "Une erreur s'est produite lors de la récupération des produits");
        }
    }
    
    @FXML
    private void handleFournisseur() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/fournisseur.fxml"));
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
    private void handleAchat() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/view/achat.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = new Stage();
            stage.setTitle("Achats");
            stage.initModality(Modality.APPLICATION_MODAL); 
            stage.initOwner(btnAddAchat.getScene().getWindow()); 
            stage.setScene(new Scene(root));
            stage.showAndWait(); 

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Erreur", "Impossible de charger le formulaire d'ajout d'achat.");
        }
    }
    
    @FXML
    private void refresh() throws SQLException {
        loadProduits();
        
        produitsTable.refresh();
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
