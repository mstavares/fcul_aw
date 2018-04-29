package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

public abstract class Crawler {
	
	protected DiseaseCatalog diseaseCatalog;
	
	public Crawler(DiseaseCatalog diseaseCatalog) {
		this.diseaseCatalog = diseaseCatalog;
	}
	
	public abstract boolean update(Disease disease);
}
