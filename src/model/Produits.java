package model;

public class Produits {
	public int id;
	public String type;
	public String produit;
	public int prix;
	public int qte;
	public int fournisseurID;
	private String fournisseurNomPrenom;
	
	
	public Produits(int id, String type, String produit, int prix, int qte, int fournisseurID) {
		this.id = id;
		this.type = type;
		this.produit = produit;
		this.prix = prix;
		this.qte = qte;
		this.fournisseurID = fournisseurID;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getProduit() {
		return produit;
	}

	public void setProduit(String produit) {
		this.produit = produit;
	}

	public int getPrix() {
		return prix;
	}

	public void setPrix(int prix) {
		this.prix = prix;
	}

	public int getQte() {
		return qte;
	}

	public void setQte(int qte) {
		this.qte = qte;
	}

	public int getFournisseurID() {
		return fournisseurID;
	}

	public void setFournisseurID(int fournisseurID) {
		this.fournisseurID = fournisseurID;
	}

	public String getFournisseurNomPrenom() {
        return fournisseurNomPrenom;
    }

    public void setFournisseurNomPrenom(String fournisseurNomPrenom) {
        this.fournisseurNomPrenom = fournisseurNomPrenom;
    }
	
	

}
