package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;

public class FullPubMed extends PubMed {

    private int idOriginalDisease;
    private int relevance;

    public FullPubMed(int id, int pubMedId, String title, String description, int idOriginalDisease, int relevance, int implicitFeedback, int explicitFeedback) {
        super(id, pubMedId, title, description);
        this.idOriginalDisease = idOriginalDisease;
        this.relevance = relevance;
    }


    public int getIdOriginalDisease() {
        return idOriginalDisease;
    }

    public void setIdOriginalDisease(int idOriginalDisease) {
        this.idOriginalDisease = idOriginalDisease;
    }

    public int getRelevance() {
        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

}
