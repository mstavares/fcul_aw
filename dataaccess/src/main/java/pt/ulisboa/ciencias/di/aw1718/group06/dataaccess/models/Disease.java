package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Disease {

	private int id;
	//private String doid;
	private String name;
	private String diseaseAbstract;
	private String derivedFrom;


	public Disease() {}

	public Disease(int id, String name, String diseaseAbstract, String derivedFrom) {
		this.id = id;
		this.name = name;
		this.diseaseAbstract = diseaseAbstract;
		this.derivedFrom = derivedFrom;
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

	public String getDiseaseAbstract() {
		return diseaseAbstract;
	}

	public void setDiseaseAbstract(String diseaseAbstract) {
		this.diseaseAbstract = diseaseAbstract;
	}

	public String getDerivedFrom() {
		return derivedFrom;
	}

	public void setDerivedFrom(String derivedFrom) {
		this.derivedFrom = derivedFrom;
	}
}
