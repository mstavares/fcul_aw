package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

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

	public Tweet(int id, String url, String description, int implicitFeedback, int explicitFeedback) {
		this(id, url, description);
		this.implicitFeedback = implicitFeedback;
		this.explicitFeedback = explicitFeedback;
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

	public int getImplicitFeedback() {
		return implicitFeedback;
	}

	public void incrementImplicitFeedback() {
		implicitFeedback++;
	}

	public void decrementImplicitFeedback() {
		implicitFeedback--;
	}

	public int getExplicitFeedback() {
		return explicitFeedback;
	}

	public void incrementExplicitFeedback() {
		explicitFeedback++;
	}

	public void decrementExplicitFeedback() {
		explicitFeedback--;
	}

}
