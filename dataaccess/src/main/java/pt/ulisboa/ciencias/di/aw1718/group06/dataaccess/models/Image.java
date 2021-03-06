package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

public class Image {

	private int id;

	private String url;
	
	private int implicitFeedback;
	
	private int explicitFeedback;

	public Image(int id, String url) {
		super();
		this.id = id;
		this.url = url;
	}


	public int getId() {
		return id;
	}


	public String getUrl() {
		return url;
	}

}
