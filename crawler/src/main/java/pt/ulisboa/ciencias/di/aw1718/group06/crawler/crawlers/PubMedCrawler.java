package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import pt.ulisboa.ciencias.di.aw1718.group06.crawler.startup.Main;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

public class PubMedCrawler extends Crawler {
	
	private final String BASE_URL_SEARCH_IDS = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&retmax=%d&retmode=xml&term=%s";
	private final String BASE_URL_ARTICLE = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=text&rettype=xml&id=%d";
	private static final Logger logger = LoggerFactory.getLogger(PubMedCrawler.class);
	//TODO we will need this eventually private final String BASE_URL_PUBMED = "https://www.ncbi.nlm.nih.gov/pubmed/%d";

	private final String ID_TAG_NAME = "Id";
	private final String TITLE_TAG_NAME = "ArticleTitle";
	private final String ABSTRACT_TAG_NAME = "AbstractText";
	
	private final int RETURN_LIMIT = 20;
	
	public PubMedCrawler(DiseaseCatalog diseaseCatalog) {
		super(diseaseCatalog);
	}


	@Override
	public boolean update(Disease disease){
		
		try {
			String diseaseEncoded = URLEncoder.encode(disease.getName(), "UTF-8");
			
			String requestURL = String.format(BASE_URL_SEARCH_IDS, RETURN_LIMIT, diseaseEncoded);
			Document document = getDocument(requestURL);
			
			NodeList ids = document.getElementsByTagName(ID_TAG_NAME);
			
			for(int i=0; i<ids.getLength(); i++){
				Node node = ids.item(i);
				int id = Integer.parseInt(node.getTextContent());
				String req = String.format(BASE_URL_ARTICLE, id);
				Document doc = getDocument(req);
				String title = getTagValue(doc, TITLE_TAG_NAME);
				String abstrct = getTagValue(doc, ABSTRACT_TAG_NAME);
				if(abstrct != null) {
					diseaseCatalog.addPubMedInfo(disease.getId(), id, title, abstrct);
				}
					
			}
		} catch (ParserConfigurationException e) {
			//System.err.println("Cannot create DocumentBuilder");
			return false;
		} catch (IOException e) {
			return false;
		} catch (SAXException e) {
			//System.err.println("Error parsing xml document");
			return false;
		} catch (SQLException e) {
			//System.err.println("Error while writing to database");
			return false;
		}	
		return true;
	}
	
	private String getTagValue(Document document, String tagname) {	
		String value = null;
		NodeList titleNode = document.getElementsByTagName(tagname);
		if(titleNode.getLength() != 0) {
			StringBuilder sb = new StringBuilder();
			for(int i = 0; i < titleNode.getLength(); i++) {
				sb.append(titleNode.item(i).getTextContent());
			}
			value = sb.toString();
		}
		return value;
	}

	private Document getDocument(String requestURL)
			throws IOException, SAXException, ParserConfigurationException {
		
		URL url = new URL(requestURL);
		InputStream is = url.openConnection().getInputStream();       
		DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = builderFactory.newDocumentBuilder();
		Document document = builder.parse(is);
		return document;
	}
}