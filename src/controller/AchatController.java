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
import model.Achat;
import model.Produits;
import service.BDD;
import service.CSV;
import service.FTPService;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
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
    private TableColumn<Achat, Integer> prixColumn; 
    @FXML
    private TableColumn<Achat, String> statusColumn;

    @FXML
    private Button btnAddFournisseur;
    @FXML
    private Button btnAddAchat;
    @FXML
    private Button btnRefresh;
    @FXML 
    private Button btnImportCSV;

    public AchatController() {
        try {
            connexion = BDD.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Impossible de se connecter à la base de données.");
        }
    }

    @FXML
    public void initialize() {
        numeroColumn.setCellValueFactory(new PropertyValueFactory<>("numero"));
        composantsColumn.setCellValueFactory(new PropertyValueFactory<>("composants"));
        prixColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        statusColumn.setCellFactory(new Callback<TableColumn<Achat, String>, TableCell<Achat, String>>() {
            @Override
            public TableCell<Achat, String> call(TableColumn<Achat, String> param) {
                return new TableCell<Achat, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setStyle("");
                        } else {
                            setText(item);

                            if ("0".equals(item)) {
                            	setText("En attente");
                                setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                            } else if("1".equals(item)){
                            	setText("Envoyé");
                                setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            }
                        }
                    }
                };
            }
        });
        prixColumn.setCellFactory(new Callback<TableColumn<Achat, Integer>, TableCell<Achat, Integer>>() {
            @Override
            public TableCell<Achat, Integer> call(TableColumn<Achat, Integer> param) {
                return new TableCell<Achat, Integer>() {
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
        loadAchats();
    }

    private void loadAchats() {
        if (connexion == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La connexion à la base de données n'est pas établie.");
            return;
        }

        String query = "SELECT * FROM achat ORDER BY status ASC";

        try (Statement stmt = connexion.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            List<Achat> achats = new ArrayList<>();

            while (rs.next()) {
                int numero = rs.getInt("numero");
                String composantsString = rs.getString("composants");
                List<String> composants = new ArrayList<>();
                
                if (composantsString != null && !composantsString.isEmpty()) {
                    String[] composantsArray = composantsString.split(",");
                    for (String composant : composantsArray) {
                        String[] parts = composant.trim().split("\\(");
                        if (parts.length == 2) {
                            int produitId = Integer.parseInt(parts[0]);
                            String quantite = parts[1].replace(")", "");
                            
                            String produitNom = getNomProduitById(produitId);
                            composants.add(produitNom + "(" + quantite + ")");
                        }
                    }
                }

                int prix;
                try {
                    prix = Integer.parseInt(rs.getString("prix"));
                } catch (NumberFormatException e) {
                    prix = 0;
                    e.printStackTrace();
                }

                String status = rs.getString("status");

                Achat achat = new Achat(numero, composants, prix, status);
                achats.add(achat);
            }

            achatsTable.getItems().clear();
            achatsTable.getItems().addAll(achats);

        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Une erreur s'est produite lors de la récupération des achats : " + e.getMessage());
        }
    }

    private String getNomProduitById(int id) {
        String query = "SELECT produit FROM produits WHERE id = ?";
        try (PreparedStatement pstmt = connexion.prepareStatement(query)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("produit");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Inconnu";
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
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger le formulaire d'ajout d'un achat");
        }
    }
    
    @FXML
	private void handleFTPImport() {
	    FTPService ftpService = new FTPService();
	    CSV importCSV = new CSV(connexion);

	    String remoteFile = "/CCI/StockManquantAchat.csv";
	    String localFile = "C:/Users/Eleve/Downloads/StockManquantAchat.csv"; 

	    String downloadResult = ftpService.downloadCSV(remoteFile, localFile);
	    if (!downloadResult.contains("succès")) {
	    	showAlert(Alert.AlertType.ERROR, "Erreur FTP", "Impossible de télécharger le fichier.");
	        return;
	    } else {
	    	showAlert(Alert.AlertType.INFORMATION ,"Succès", "Importation importé avec succès !");
	    }

	    boolean importSuccess = importCSV.importCSVToDatabase(localFile);


	    if (importSuccess) {
	    	showAlert(Alert.AlertType.INFORMATION, "Succès", "Le fichier a été importé avec succès dans la base de données.");
	        loadAchats();
	    } else {
	    	showAlert(Alert.AlertType.ERROR, "Erreur", "L'importation du fichier dans la base de données a échoué.");
	    }
	}

    @FXML
    private void refresh() {
        loadAchats();
        achatsTable.refresh();
    }

    private void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
