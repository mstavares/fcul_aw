package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Locale;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

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
	private final String PUBLISHED_DATE_TAG_NAME = "PubDate";
	
	private final int RETURN_LIMIT = 10;
	
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
				String date = getDateAsString(doc);
				Date dt = getDate(date);
				
				if(abstrct != null) {
					diseaseCatalog.addPubMedInfo(disease.getId(), id, title, abstrct, new java.sql.Date(dt.getTime()));
				}
					
			}
		} catch (ParserConfigurationException e) {
			logger.error( "Cannot create DocumentBuilder", e);
			return false;
		} catch (IOException e) {
			logger.error( "IOException", e);
			return false;
		} catch (SAXException e) {
			logger.error( "Error parsing xml document", e);
			return false;
		} catch (SQLException e) {
			logger.error( "Error writing to database", e);
			return false;
		}	
		return true;
	}


	private Date getDate(String date) {
		SimpleDateFormat format;
		if (date.length() > 8)
			format = new SimpleDateFormat("yyyyMMMdd", new Locale("EN"));
		else if (date.length() > 7)
			format = new SimpleDateFormat("yyyyMMdd", new Locale("EN"));
		else if (date.length() > 6)
			format = new SimpleDateFormat("yyyyMMM", new Locale("EN"));
		else if (date.length() > 4)
			format = new SimpleDateFormat("yyyyMM", new Locale("EN"));
		else
			format = new SimpleDateFormat("yyyy", new Locale("EN"));
		
		Date dt;
		try {
			dt = format.parse(date);
		} catch (ParseException e) {
			dt = new Date(0);
			logger.error("Pubmed date failed: " + date);
		}
		return dt;
	}
	
	private String getDateAsString(Document doc) {
		NodeList dateNode = doc.getElementsByTagName(PUBLISHED_DATE_TAG_NAME);
		String date = dateNode.item(0).getTextContent();
		
		if(doc.getElementsByTagName("MedlineDate").getLength() != 0) {
			String [] stringArray = date.split("[ ,-]");
			date = stringArray[0] + stringArray[stringArray.length-1];
		}
		return date;
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