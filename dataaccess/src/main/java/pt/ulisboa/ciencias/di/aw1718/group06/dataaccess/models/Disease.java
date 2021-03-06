package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Disease {

	private int id;
	
	private String doid;
	
	private String name;

	private String description; // abstract

	private String derivedFrom;

	private String field;
	
	private String dead;
	
	public Disease() {}

	public Disease(int id, String doid, String name, String description, String derivedFrom, String field, String dead) {
		this.setId(id);
		this.setDoid(doid);
		this.setName(name);
		this.setDescription(description);
		this.setDerivedFrom(derivedFrom);
		this.setField(field);
		this.setDead(dead);
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
	
	public String getField() {
		return field;
	}
	
	public void setField(String field) {
		this.field = field;
	}
	
	public String getDead() {
		return dead;
	}
	
	public void setDead(String dead) {
		this.dead = dead;
	}
	
	public String getDoid() {
		return doid;
	}
	
	public void setDoid(String doid) {
		this.doid = doid;
	}
}
