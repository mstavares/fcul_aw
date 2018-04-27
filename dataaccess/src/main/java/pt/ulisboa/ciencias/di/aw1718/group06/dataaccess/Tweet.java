package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess;

public class Tweet {

	private int id;

	private String url;

	private String description;
	
	private int implicitFeedback;
	
	private int explicitFeedback;


	public Tweet(int id, String url, String description) {
		this.id = id;
		this.url = url;
		this.description = description;
	}


	public int getId() {
		return id;
	}


	public String getUrl() {
		return url;
	}


	public String getDescription() {
		return description;
	}

}
