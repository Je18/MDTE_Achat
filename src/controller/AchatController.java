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
import model.BDD;
import model.FTPService;
import Service.CSV;
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
            showAlert(Alert.AlertType.ERROR, "Erreur de connexion", "Impossible de se connecter � la base de donn�es.");
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
                            	setText("Envoy�");
                                setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                            }
                        }
                    }
                };
            }
        });
        loadAchats();
    }

    private void loadAchats() {
        if (connexion == null) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "La connexion � la base de donn�es n'est pas �tablie.");
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
                        composants.add(composant.trim());
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
            showAlert(Alert.AlertType.ERROR, "Erreur SQL", "Une erreur s'est produite lors de la r�cup�ration des achats : " + e.getMessage());
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
	    if (!downloadResult.contains("succ�s")) {
	    	showAlert(Alert.AlertType.ERROR, "Erreur FTP", "Impossible de t�l�charger le fichier.");
	        return;
	    } else {
	    	showAlert(Alert.AlertType.INFORMATION ,"Succ�s", "Importation import� avec succ�s !");
	    }

	    boolean importSuccess = importCSV.importCSVToDatabase(localFile);


	    if (importSuccess) {
	    	showAlert(Alert.AlertType.INFORMATION, "Succ�s", "Le fichier a �t� import� avec succ�s dans la base de donn�es.");
	        loadAchats();
	    } else {
	    	showAlert(Alert.AlertType.ERROR, "Erreur", "L'importation du fichier dans la base de donn�es a �chou�.");
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
