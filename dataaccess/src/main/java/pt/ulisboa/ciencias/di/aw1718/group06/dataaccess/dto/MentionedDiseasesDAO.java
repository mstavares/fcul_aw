package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

public class MentionedDiseasesDAO {
	
	private int id;
	
	private String name;
	
	private String places;
	
	public MentionedDiseasesDAO() {}
	
	public MentionedDiseasesDAO(int id, String name, String places) {
		this.setId(id);
		this.setName(name);
		this.setPlaces(places);
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setPlaces(String places) {
		this.places = places;
	}
	
	public String getPlaces() {
		return places;
	}
}
