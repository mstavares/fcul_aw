package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models;

public class Feedback {

	private int implicit;
	private int explicit;
	
	public Feedback(int implicit, int explicit) {
		this.implicit = implicit;
		this.explicit = explicit;
	}
	
	public int getImplicitFeedback() {
		return implicit;
	}
	
	public int getExplicitFeedback() {
		return explicit;
	}
}
