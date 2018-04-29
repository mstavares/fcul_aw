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


    public FullDisease() {}

    public FullDisease(int id, String name, String diseaseAbstract, String derivedFrom, List<FullPubMed> articles, List<FullImage> images, List<FullTweet> tweets) {
        super(id, name, diseaseAbstract, derivedFrom);
        this.articles = articles;
        this.images = images;
        this.tweets = tweets;
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
}
