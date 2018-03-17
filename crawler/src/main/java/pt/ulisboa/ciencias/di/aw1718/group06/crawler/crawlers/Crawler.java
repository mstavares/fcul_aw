package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

public abstract class Crawler {
	
	private DiseaseCatalog diseaseCatalog;
	
	public Crawler(DiseaseCatalog diseaseCatalog) {
		this.diseaseCatalog = diseaseCatalog;
	}
	
	public abstract boolean update(String disease);
}
