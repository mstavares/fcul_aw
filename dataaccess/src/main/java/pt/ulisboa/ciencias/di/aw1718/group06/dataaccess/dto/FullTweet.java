package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Tweet;

import java.util.Date;

public class FullTweet extends Tweet {

    private Date pubDate;
    private int idOriginalDisease;
    private int relevance;


    public FullTweet(int id, String url, String description, Date pubDate, int idOriginalDisease, int implicitFeedBack, int explicitFeedBack, int relevance) {
        super(id, url, description, implicitFeedBack, explicitFeedBack);
        this.pubDate = pubDate;
        this.idOriginalDisease = idOriginalDisease;
        this.relevance = relevance;

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

    public int getRelevance() {
        return relevance;
    }

    public void setRelevance(int relevance) {
        this.relevance = relevance;
    }

}
