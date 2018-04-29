package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

import java.sql.SQLException;

public class FlickrCrawler extends Crawler {

	private static final Logger LOGGER = LoggerFactory.getLogger(FlickrCrawler.class);
	private static final String API_KEY = "80a9c08e71af8c4d5b6e9aa782c68276";
	private static final String SECRET = "ae2f9bf1ea0a11bb";
	private static final int NUMBER_OF_IMAGES = 10;
	private final String BASE_URL = "";
	private DiseaseCatalog diseaseCatalog;
	private Flickr flickr;
	
	public FlickrCrawler(DiseaseCatalog diseaseCatalog) {
		super(diseaseCatalog);
		this.diseaseCatalog = diseaseCatalog;
		flickr = new Flickr(API_KEY, SECRET, new REST());
	}


	@Override
	public boolean update(Disease disease) {
		// TODO Auto-generated method stub
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setAccuracy(Flickr.ACCURACY_WORLD);
		searchParameters.setText(disease.getName());
		try {
			PhotoList<Photo> photos = flickr.getPhotosInterface().search(searchParameters, NUMBER_OF_IMAGES, 0);
			for(Photo photo : photos) {
				try {
					diseaseCatalog.createImage(disease, photo.getUrl());
				} catch (SQLException e) {
					//LOGGER.error("Error while writing to database: ", e);
					return false;
				}
			}
		} catch (FlickrException e) {
			//LOGGER.error("Error while looking up results for \"" + disease.getName() + "\"", e);
			return false;
		}

		return true;
	}

}
