package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Tweet;

import java.util.Date;

public class FullTweet extends Tweet {

    private Date pubDate;
    private int idOriginalDisease;
    private int implicitFeedback;
    private int explicitFeedback;


    public FullTweet(int id, String url, String description, Date pubDate, int idOriginalDisease, int implicitFeedBack, int explicitFeedBack) {
        super(id, url, description);
        this.pubDate = pubDate;
        this.idOriginalDisease = idOriginalDisease;
        this.implicitFeedback = implicitFeedBack;
        this.explicitFeedback = explicitFeedBack;

    }

    public Date getPubDate() {
        return pubDate;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
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
