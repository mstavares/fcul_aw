package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Statistic {

    private int numberOfDiseases;
    private int numberOfPubMeds;
    private int numberOfTweets;
    private int numberOfImages;

    public Statistic() {}

    public Statistic(int numberOfDiseases, int numberOfPubMeds, int numberOfTweets, int numberOfImages) {
        this.numberOfDiseases = numberOfDiseases;
        this.numberOfPubMeds = numberOfPubMeds;
        this.numberOfTweets = numberOfTweets;
        this.numberOfImages = numberOfImages;
    }

    public int getNumberOfDiseases() {
        return numberOfDiseases;
    }

    public void setNumberOfDiseases(int numberOfDiseases) {
        this.numberOfDiseases = numberOfDiseases;
    }

    public int getNumberOfPubMeds() {
        return numberOfPubMeds;
    }

    public void setNumberOfPubMeds(int numberOfPubMeds) {
        this.numberOfPubMeds = numberOfPubMeds;
    }

    public int getNumberOfImages() {
        return numberOfImages;
    }

    public void setNumberOfImages(int numberOfImages) {
        this.numberOfImages = numberOfImages;
    }

    public int getNumberOfTweets() {
        return numberOfTweets;
    }

    public void setNumberOfTweets(int numberOfTweets) {
        this.numberOfTweets = numberOfTweets;
    }
}
