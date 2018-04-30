package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

public class PubMed {

	private int id;
	private int pubMedId;
	private String title;
	private String description;


	public PubMed(int id, int pubMedId, String title, String description) {
		this.id = id;
		this.pubMedId = pubMedId;
		this.title = title;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getPubMedId() {
		return pubMedId;
	}

	public void setPubMedId(int pubMedId) {
		this.pubMedId = pubMedId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
