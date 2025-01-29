package main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import service.BDD;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BDD bdd = null;
        try {
            bdd = new BDD(); 
            System.out.println("Base de données connectée avec succès.");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur de connexion à la base de données.");
            return;
        }

        Parent rootFXML = FXMLLoader.load(getClass().getResource("/view/main.fxml"));
		BorderPane root = new BorderPane();
		Scene scene = new Scene(rootFXML);
		primaryStage.setScene(scene);
		primaryStage.setTitle("MDTE Achat");
		primaryStage.show();
    }
}
