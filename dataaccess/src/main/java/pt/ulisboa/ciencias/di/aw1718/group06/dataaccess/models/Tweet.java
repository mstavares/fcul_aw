package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

public class Tweet {

	private int id;
	private String url;
	private String description;


	public Tweet(int id, String url, String description) {
		this.id = id;
		this.url = url;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}
