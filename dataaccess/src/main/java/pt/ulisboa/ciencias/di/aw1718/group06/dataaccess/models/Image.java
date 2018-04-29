package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

public class Image {

	private int id;
	private String url;
	private int implicitFeedback;
	private int explicitFeedback;

	public Image(int id, String url) {
		this.id = id;
		this.url = url;
	}

	public Image(int id, String url, int implicitFeedback, int explicitFeedback) {
		this(id, url);
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
