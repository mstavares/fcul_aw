package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

public class FullFeedback {

    public enum Operations { INCREMENT_IMPLICIT, INCREMENT_EXPLICIT, DECREMENT_IMPLICIT, DECREMENT_EXPLICIT }
    private int diseaseId;
    private int objectId;
    private int implicitFeedback;
    private int explicitFeedback;
    
    public FullFeedback(int diseaseId, int objectId, int implicitFeedback, int explicitFeedback) {
        this.diseaseId = diseaseId;
        this.objectId = objectId;
        this.implicitFeedback = implicitFeedback;
        this.explicitFeedback = explicitFeedback;
    }

    public int getDiseaseId() {
        return diseaseId;
    }

    public int getObjectId() {
        return objectId;
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
