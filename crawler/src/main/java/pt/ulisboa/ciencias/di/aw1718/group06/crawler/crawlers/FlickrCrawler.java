package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

public class FlickrCrawler extends Crawler {
	
	private final String BASE_URL = "";
	
	public FlickrCrawler(DiseaseCatalog diseaseCatalog) {
		super(diseaseCatalog);
	}


	@Override
	public boolean update(Disease disease) {
		// TODO Auto-generated method stub
		return false;
	}

}
