package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess;

public class Disease {

	private int id;
	
	//private String doid;
	
	private String name;

	private String description; // abstract

	private String derivedFrom;


	public Disease(int id, String name, String description, String derivedFrom) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.derivedFrom = derivedFrom;
	}


	public int getId() {
		return id;
	}


	public String getName() {
		return name;
	}


	public String getDescription() {
		return description;
	}


	public String getDerivedFrom() {
		return derivedFrom;
	}

}
