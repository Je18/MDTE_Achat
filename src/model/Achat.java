package model;

import java.util.List;

public class Achat {
    private int numero;
    private List<String> composants;
    private int prix;
    private String status;

    public Achat(int numero, List<String> composants, int prix, String status) {
        this.numero = numero;
        this.composants = composants;
        this.prix = prix;
        this.status = status;
    }

    // Getters et setters
    public int getNumero() {
        return numero;
    }

    public List<String> getComposants() {
        return composants;
    }

    public String getComposantsAsString() {
        return String.join(",", composants); 
    }

    public int getPrix() {
        return prix;
    }

    public String getStatus() {
        return status;
    }
}


