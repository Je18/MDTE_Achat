package controller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import model.Fournisseur;
import model.BDD;

public class FormulaireAchatController {

	private Connection connexion;

	@FXML
	private TextField numeroField;
	@FXML
	private ComboBox<Fournisseur> fournisseurComboBox;
	@FXML
	private ComboBox<String> composantsComboBox;
	@FXML
	private TextField prixField;
	@FXML
	private Button enregistrerButton;
	@FXML
	private Button annulerButton;
	@FXML
	private ListView<String> listViewComposants;
	@FXML
	private Button btnAjouterProduit;

	private ObservableList<String> produitsAjoutes = FXCollections.observableArrayList();
	private Map<String, Integer> produitPrixMap = new HashMap<>();

	@FXML
	public void initialize() {
		try {
			connexion = BDD.getConnection();
			if (connexion == null) {
				throw new SQLException("Connexion à la base de données échouée.");
			}
			recupererNumero();
			chargerFournisseurs();

			enregistrerButton.setOnAction(e -> enregistrerAchat());
			annulerButton.setOnAction(e -> reinitialiserFormulaire());
			btnAjouterProduit.setOnAction(e -> ajouterProduitAListe());

		} catch (Exception e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Erreur", "Initialisation échouée : " + e.getMessage());
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
			showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de récupérer le numéro.");
		}
	}

	private void chargerFournisseurs() {
		String query = "SELECT * FROM fournisseur \r\n" + "WHERE id IN (SELECT DISTINCT fournisseurId FROM produits)";
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
					return fournisseur != null
							? fournisseur.getNom() + " " + fournisseur.getPrenom() + " (" + fournisseur.getCode() + ")"
							: "";
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
			showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les fournisseurs.");
		}
	}

	private void chargerComposantsPourFournisseur(Fournisseur fournisseur) {
		String query = "SELECT produit, prix, qte FROM produits WHERE fournisseurId = ? AND qte != 0";
		composantsComboBox.getItems().clear();
		produitPrixMap.clear();

		try (PreparedStatement ps = connexion.prepareStatement(query)) {
			ps.setInt(1, fournisseur.getId());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					String produit = rs.getString("produit");
					int prix = rs.getInt("prix");
					int qte = rs.getInt("qte");

					String produitAvecQuantite = produit + " (" + qte + "qte)";
					composantsComboBox.getItems().add(produitAvecQuantite);

					produitPrixMap.put(produitAvecQuantite, prix);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showAlert(Alert.AlertType.ERROR, "Erreur",
					"Impossible de charger les composants pour le fournisseur sélectionné.");
		}
	}

	@FXML
	private void ajouterProduitAListe() {
		String produitSelectionne = composantsComboBox.getValue();

		if (produitSelectionne == null) {
			showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner un produit avant de l'ajouter.");
			return;
		}

		produitsAjoutes.add(produitSelectionne);
		listViewComposants.setItems(produitsAjoutes);
		mettreAJourPrixTotal();
	}

	private void mettreAJourPrixTotal() {
        int prixTotal = produitsAjoutes.stream()
                                       .mapToInt(produit -> produitPrixMap.getOrDefault(produit, 0))
                                       .sum();
        prixField.setText(Integer.toString(prixTotal));
    }

	private void reinitialiserFormulaire() {
		fournisseurComboBox.setDisable(false);
		fournisseurComboBox.getSelectionModel().clearSelection();
		composantsComboBox.getItems().clear();
		listViewComposants.getItems().clear();
		produitsAjoutes.clear();
		prixField.clear();
		numeroField.clear();
		recupererNumero();
	}

	private void enregistrerAchat() {
	    if (produitsAjoutes.isEmpty()) {
	        showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez ajouter au moins un produit.");
	        return;
	    }

	    Fournisseur fournisseur = fournisseurComboBox.getValue();
	    if (fournisseur == null) {
	        showAlert(Alert.AlertType.WARNING, "Avertissement", "Veuillez sélectionner un fournisseur.");
	        return;
	    }

	    String numero = numeroField.getText();
	    if (numero == null || numero.trim().isEmpty()) {
	        showAlert(Alert.AlertType.WARNING, "Avertissement", "Le numéro de l'achat est manquant.");
	        return;
	    }

	    String prix = prixField.getText();
	    if (prix == null || prix.trim().isEmpty() || !prix.matches("\\d+")) {
	        showAlert(Alert.AlertType.WARNING, "Avertissement", "Le prix total est invalide.");
	        return;
	    }

	    Map<String, Integer> produitQuantiteMap = new HashMap<>();
	    for (String produitAvecQuantite : produitsAjoutes) {
	        String produit = produitAvecQuantite.split(" \\(")[0];
	        produitQuantiteMap.put(produit, produitQuantiteMap.getOrDefault(produit, 0) + 1);
	    }

	    StringBuilder composantsBuilder = new StringBuilder();
	    for (Map.Entry<String, Integer> entry : produitQuantiteMap.entrySet()) {
	        composantsBuilder.append(entry.getKey()).append(" (").append(entry.getValue()).append("), ");
	    }
	    String composants = composantsBuilder.toString().replaceAll(", $", ""); 

	    String insertQuery = "INSERT INTO achat (numero, composants, prix, status, fournisseurId) VALUES (?, ?, ?, ?, ?)";
	    String updateProduitQuery = "UPDATE produits SET qte = qte - ? WHERE produit = ? AND qte >= ?";

	    try (PreparedStatement insertPs = connexion.prepareStatement(insertQuery);
	         PreparedStatement updatePs = connexion.prepareStatement(updateProduitQuery)) {

	        insertPs.setInt(1, Integer.parseInt(numero));
	        insertPs.setString(2, composants);
	        insertPs.setInt(3, Integer.parseInt(prix));
	        insertPs.setInt(4, 0); 
	        insertPs.setInt(5, fournisseur.getId());

	        int rowsAffected = insertPs.executeUpdate();
	        if (rowsAffected > 0) {
	            for (Map.Entry<String, Integer> entry : produitQuantiteMap.entrySet()) {
	                String produit = entry.getKey();
	                int quantite = entry.getValue();

	                updatePs.setInt(1, quantite);
	                updatePs.setString(2, produit); 
	                updatePs.setInt(3, quantite);
	                updatePs.executeUpdate();
	            }

	            showAlert(Alert.AlertType.INFORMATION, "Succès", "L'achat a été enregistré et les quantités mises à jour.");
	            reinitialiserFormulaire();
	        } else {
	            showAlert(Alert.AlertType.ERROR, "Erreur", "L'enregistrement de l'achat a échoué.");
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	        showAlert(Alert.AlertType.ERROR, "Erreur", "Une erreur est survenue lors de l'enregistrement de l'achat : " + e.getMessage());
	    }
	}



	private void showAlert(Alert.AlertType alertType, String title, String content) {
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
