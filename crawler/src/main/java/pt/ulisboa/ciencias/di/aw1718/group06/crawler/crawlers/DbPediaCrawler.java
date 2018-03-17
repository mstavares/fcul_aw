package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

public class DbPediaCrawler extends Crawler {
	
	private final String BASE_URL = "";
	
	public DbPediaCrawler(DiseaseCatalog diseaseCatalog) {
		super(diseaseCatalog);
	}


	@Override
	public boolean update(String disease) {
		// TODO Auto-generated method stub
		return false;
	}

}
