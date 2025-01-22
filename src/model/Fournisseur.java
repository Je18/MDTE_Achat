package model;

public class Fournisseur {
    private String code;
    private String nom;
    private String prenom;
    private String adresse;

    public Fournisseur(String code, String nom, String prenom, String adresse) {
        this.code = code;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
    }
    
    public String getCode() {
        return code;
    }

    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public String getAdresse() {
        return adresse;
    }
}
