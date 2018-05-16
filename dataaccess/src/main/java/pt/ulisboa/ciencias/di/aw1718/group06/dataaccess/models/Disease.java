package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Disease {

	private int id;
	
	//private String doid;
	
	private String name;

	private String description; // abstract

	private String derivedFrom;


	public Disease() {}

	public Disease(int id, String name, String description, String derivedFrom) {
		this.setId(id);
		this.setName(name);
		this.setDescription(description);
		this.setDerivedFrom(derivedFrom);
	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(String derivedFrom) {
		this.derivedFrom = derivedFrom;
	}
}
