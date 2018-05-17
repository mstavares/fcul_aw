package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import java.util.List;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class FullPubMed extends PubMed {

    private int idOriginalDisease;
    private int implicitFeedback;
    private int explicitFeedback;
    
    private List<MentionedDiseasesDAO> mentionedDiseases;

    public FullPubMed(int id, int pubMedId, String title, String description, int idOriginalDisease, int implicitFeedback, int explicitFeedback, List<MentionedDiseasesDAO> mentionedDiseases) {
        super(id, pubMedId, title, description);
        this.idOriginalDisease = idOriginalDisease;
        this.implicitFeedback = implicitFeedback;
        this.explicitFeedback = explicitFeedback;
        this.mentionedDiseases = mentionedDiseases;
    }


    public int getIdOriginalDisease() {
        return idOriginalDisease;
    }

    public void setIdOriginalDisease(int idOriginalDisease) {
        this.idOriginalDisease = idOriginalDisease;
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
    
    public List<MentionedDiseasesDAO> getMentionedDiseases(){
    	return mentionedDiseases;
    }
    
    public void setMentionedDiseases(List<MentionedDiseasesDAO> mentioned) {
    	this.mentionedDiseases = mentionedDiseases;
    }

}
