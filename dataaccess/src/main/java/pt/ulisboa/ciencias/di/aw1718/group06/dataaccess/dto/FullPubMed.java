package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;

public class FullPubMed extends PubMed {

    private int idOriginalDisease;
    private int implicitFeedback;
    private int explicitFeedback;

    public FullPubMed(int id, int pubMedId, String title, String description, int idOriginalDisease, int implicitFeedback, int explicitFeedback) {
        super(id, pubMedId, title, description);
        this.idOriginalDisease = idOriginalDisease;
        this.implicitFeedback = implicitFeedback;
        this.explicitFeedback = explicitFeedback;
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

}
