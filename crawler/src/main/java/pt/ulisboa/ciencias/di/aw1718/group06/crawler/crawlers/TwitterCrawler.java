package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterCrawler extends Crawler {
    private static final Logger logger = LoggerFactory.getLogger(TwitterCrawler.class);

	private final String BASE_URL = "";
    private final Twitter twitter;
    private int STORED_TWEET_COUNT = 10;

    public TwitterCrawler(DiseaseCatalog diseaseCatalog, Twitter twitter) {
		super(diseaseCatalog);
        this.twitter = twitter;
	}


	@Override
	public boolean update(Disease disease) {

        Query query = new Query(disease.getName());
        query.setCount(STORED_TWEET_COUNT);
        try {
            QueryResult result = this.twitter.search(query);
            for (Status status : result.getTweets()) {
                logger.info("@" + status.getUser().getScreenName() + ":" + status.getText());
            }
        } catch (TwitterException e) {
            logger.error("Error while looking up results for \"" + disease.getName() + "\"", e);
            return false;
        }
		return true;
	}

}
