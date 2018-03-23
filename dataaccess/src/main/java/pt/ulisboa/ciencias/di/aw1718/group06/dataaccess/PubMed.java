package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess;

public class PubMed {

	private int id;
	
	private int pubMedID;

	private String title;

	private String description;


	protected PubMed(int id, int pubMedID, String title, String description) {
		this.id = id;
		this.title = title;
		this.description = description;
	}


	public int getId() {
		return id;
	}
	
	public int getPubMedId() {
		return pubMedID;
	}


	public String getTitle() {
		return title;
	}


	public String getDescription() {
		return description;
	}

}
