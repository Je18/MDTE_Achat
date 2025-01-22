package model;

public class Fournisseur {
	private int id;
    private String code;
    private String nom;
    private String prenom;
    private String adresse;

    public Fournisseur(int id, String code, String nom, String prenom, String adresse) {
    	this.id = id;
        this.code = code;
        this.nom = nom;
        this.prenom = prenom;
        this.adresse = adresse;
    }
    
    public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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
