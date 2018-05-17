package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

public class DbPediaCrawler extends Crawler {
	
	private static String BASE_URL = "https://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+%3Furl+%3Fname+%3Ffield+%3Fabstract+%3FwasDerivedFrom+where+%7B%0D%0A+%3Furl+a+dbo%3ADisease%3B%0D%0A+++++++foaf%3Aname+%3Fname%3B%0D%0A+++++++dbo%3Aabstract+%3Fabstract%3B%0D%0A+++++++prov%3AwasDerivedFrom+%3FwasDerivedFrom.%0D%0AOPTIONAL%7B%0D%0A+++++++%3Furl+dbp%3Afield+%3FfieldURL.%0D%0A+++++++%3FfieldURL+rdfs%3Alabel+%3Ffield+FILTER+%28LANG%28%3Ffield%29+%3D+%27en%27%29%0D%0A%7D%0D%0A+++++++FILTER+%28LANG%28%3Fabstract%29%3D%27en%27%29%0D%0A+++++++FILTER+%28LANG%28%3Fname%29%3D%27en%27%29%0D%0A%7D&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+";

	private static String GET_SINGLE_DISEASE_FST = "https://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+%3Furl+%3Fname+%3Ffield+%3Fabstract+%3FwasDerivedFrom+where+%7B%0D%0A+%3Furl+a+dbo%3ADisease%3B%0D%0A+++++++foaf%3Aname+%3Fname%3B%0D%0A+++++++dbo%3Aabstract+%3Fabstract%3B%0D%0A+++++++prov%3AwasDerivedFrom+%3FwasDerivedFrom.%0D%0AOPTIONAL+%7B%0D%0A+++++++%3Furl+dbp%3Afield+%3FfieldURL.%0D%0A+++++++%3FfieldURL+rdfs%3Alabel+%3Ffield+FILTER+%28LANG%28%3Ffield%29+%3D+%27en%27%29%0D%0A%7D%0D%0A+++++++FILTER+%28LANG%28%3Fabstract%29%3D%27en%27%29%0D%0A+++++++FILTER+%28LANG%28%3Fname%29%3D%27en%27%29%0D%0A+++++++FILTER+%28lcase%28str%28%3Fname%29%29+%3D+%22";
	private static String GET_SINGLE_DISEASE_SND = "%22%29%0D%0A%7D+LIMIT+1&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+";
	
	private static String GET_DEAD_FST = "https://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=PREFIX+dbo%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0D%0A%0D%0ASELECT+%3Fperson+where+%7B%0D%0A+%3Fdisease+a+dbo%3ADisease+.%0D%0A+%3FpersonURL+dbo%3AdeathCause+%3Fdisease+.%0D%0A+%3FpersonURL+foaf%3Aname+%3Fperson+FILTER+%28lang%28%3Fperson%29+%3D+%22en%22%29.%0D%0A+%3Fdisease+foaf%3Aname+%3Fdiseasename+FILTER+%28lang%28%3Fdiseasename%29+%3D+%22en%22%29.%0D%0AFILTER+%28lcase%28str%28%3Fdiseasename%29%29+%3D+%22";
	private static String GET_DEAD_SND = "%22%29%0D%0A%7D+LIMIT+5&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+";
	
	public DbPediaCrawler(DiseaseCatalog diseaseCatalog, int limit) {
		super(diseaseCatalog);
		
		if (limit > 0)
			BASE_URL = "https://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=SELECT+%3Furl+%3Fname+%3Ffield+%3Fabstract+%3FwasDerivedFrom+where+%7B%0D%0A+%3Furl+a+dbo%3ADisease%3B%0D%0A+++++++foaf%3Aname+%3Fname%3B%0D%0A+++++++dbo%3Aabstract+%3Fabstract%3B%0D%0A+++++++prov%3AwasDerivedFrom+%3FwasDerivedFrom.%0D%0AOPTIONAL%7B%0D%0A+++++++%3Furl+dbp%3Afield+%3FfieldURL.%0D%0A+++++++%3FfieldURL+rdfs%3Alabel+%3Ffield+FILTER+%28LANG%28%3Ffield%29+%3D+%27en%27%29%0D%0A%7D%0D%0A+++++++FILTER+%28LANG%28%3Fabstract%29%3D%27en%27%29%0D%0A+++++++FILTER+%28LANG%28%3Fname%29%3D%27en%27%29%0D%0A%7D+LIMIT+"+ 
					limit + "&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+";
	}
	
	public DbPediaCrawler(DiseaseCatalog diseaseCatalog) {
		super(diseaseCatalog);
	}
	
	
	/**
	 * Pulls the list of diseases from dbpedia and updates it in the database.
	 * @return Returns a list of the added Diseases
	 */
	public List<Disease> update() {
		try {
			List<Disease> diseases = new ArrayList<Disease>();
			URL url = new URL(BASE_URL);
			
		    InputStream is = url.openConnection().getInputStream();

		    BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
		    
		    JsonParser parser = new JsonParser();
		    JsonObject rootObject = parser.parse(reader).getAsJsonObject();
		    JsonArray mainArray = rootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
		    
		    for (JsonElement elem: mainArray) {
		    	JsonObject elemObj = elem.getAsJsonObject();
		    	//String link = elemObj.get("url").getAsJsonObject().get("value").getAsString();
		    	String name = elemObj.get("name").getAsJsonObject().get("value").getAsString();;
		    	String desc =  elemObj.get("abstract").getAsJsonObject().get("value").getAsString();;
		    	String derivedFrom = elemObj.get("wasDerivedFrom").getAsJsonObject().get("value").getAsString();;
		    	String field = elemObj.has("field") ? elemObj.get("field").getAsJsonObject().get("value").getAsString() : null;;
		    	String dead = getDeadFromDisease(name);
		    	try {
			    	Disease d = diseaseCatalog.addDisease(name, desc, derivedFrom, field, dead);
			    	diseases.add(d);
		    	}catch(SQLException e) {}

		    }
		    
		    reader.close();
		    
		    
		    return diseases;
		} catch (IOException  e) {
			e.printStackTrace();
		}

		return null;
	}
	
	// Will return a String as follows:   dead_name1;dead_name2;dead_name3
	private String getDeadFromDisease(String diseaseName) {
		String dead = null;
		try {
			String nameForURL = URLEncoder.encode(diseaseName.toLowerCase(), "UTF-8");
			String formattedURL = GET_DEAD_FST + nameForURL + GET_DEAD_SND;
			
			URL url = new URL(formattedURL);
			
		    InputStream is = url.openConnection().getInputStream();

		    BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
		    
		    JsonParser parser = new JsonParser();
		    JsonObject rootObject = parser.parse(reader).getAsJsonObject();
		    JsonArray mainArray = rootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();
		    if(mainArray.size() == 0)
		    	return dead;
		    dead = "";
		    for (JsonElement elem: mainArray) {
		    	JsonObject elemObj = elem.getAsJsonObject();	 
		    	String name = elemObj.get("person").getAsJsonObject().get("value").getAsString();;
		    	dead += (name+";");		    	
		    }
		    
		    reader.close();
		    
		} catch (UnsupportedEncodingException e) {
			System.err.println("Error encoding url: " + e.getMessage());
		} catch (MalformedURLException e) {
			System.err.println("Malformed URL: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		}
		return dead;
	}

	@Override
	public boolean update(Disease disease) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public Disease getSingleDisease(String diseaseName) {
		
		Disease disease = null;
		try {
			String nameForURL = URLEncoder.encode(diseaseName.toLowerCase(), "UTF-8");
			String formattedURL = GET_SINGLE_DISEASE_FST + nameForURL + GET_SINGLE_DISEASE_SND;
			
			URL url = new URL(formattedURL);
			
		    InputStream is = url.openConnection().getInputStream();

		    BufferedReader reader = new BufferedReader( new InputStreamReader( is )  );
		    
		    JsonParser parser = new JsonParser();
		    JsonObject rootObject = parser.parse(reader).getAsJsonObject();
		    JsonArray mainArray = rootObject.get("results").getAsJsonObject().get("bindings").getAsJsonArray();

		    for (JsonElement elem: mainArray) {
		    	JsonObject elemObj = elem.getAsJsonObject();
		    	String name = elemObj.get("name").getAsJsonObject().get("value").getAsString();;
		    	String desc =  elemObj.get("abstract").getAsJsonObject().get("value").getAsString();;
		    	String derivedFrom = elemObj.get("wasDerivedFrom").getAsJsonObject().get("value").getAsString();;
		    	String field = elemObj.has("field") ? elemObj.get("field").getAsJsonObject().get("value").getAsString() : null;;
		    	String dead = getDeadFromDisease(name);
			    disease = diseaseCatalog.addDisease(name, desc, derivedFrom, field, dead);
		    }
		    
		    reader.close();
		    
		} catch (UnsupportedEncodingException e) {
			System.err.println("Error encoding url: " + e.getMessage());
		} catch (MalformedURLException e) {
			System.err.println("Malformed URL: " + e.getMessage());
		} catch (IOException e) {
			System.err.println("IOException: " + e.getMessage());
		} catch (SQLException e) {
			System.err.println("SQLException: " + e.getMessage());
		}
		
		return disease;
	}

}
