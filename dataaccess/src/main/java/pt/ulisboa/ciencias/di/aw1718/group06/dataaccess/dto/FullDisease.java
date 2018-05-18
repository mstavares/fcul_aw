package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class FullDisease extends Disease {

    private List<FullPubMed> articles = new ArrayList<>();
    private List<FullImage> images = new ArrayList<>();
    private List<FullTweet> tweets = new ArrayList<>();
    private List<Disease> diseases = new ArrayList<>();


    public FullDisease() {}

    public FullDisease(int id, String doid, String name, String diseaseAbstract, String derivedFrom, String field, String dead, List<FullPubMed> articles, List<FullImage> images, List<FullTweet> tweets, List<Disease> diseases) {
        super(id, doid, name, diseaseAbstract, derivedFrom, field, dead);
        this.articles = articles;
        this.images = images;
        this.tweets = tweets;
        this.diseases = diseases;
    }


    public List<FullPubMed> getArticles() {
        return articles;
    }

    public void setArticles(List<FullPubMed> articles) {
        this.articles = articles;
    }

    public List<FullImage> getImages() {
        return images;
    }

    public void setImages(List<FullImage> images) {
        this.images = images;
    }

    public List<FullTweet> getTweets() {
        return tweets;
    }

    public void setTweets(List<FullTweet> tweets) {
        this.tweets = tweets;
    }
    
    public List<Disease> getDiseases(){
    	return diseases;
    }
    
    public void setDisease(List<Disease> diseases) {
    	this.diseases = diseases;
    }
}
