package pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

public class DbPediaCrawler extends Crawler {
	
	private final String BASE_URL = "https://dbpedia.org/sparql?default-graph-uri=http%3A%2F%2Fdbpedia.org&query=PREFIX+dbo%3A+%3Chttp%3A%2F%2Fdbpedia.org%2Fontology%2F%3E%0D%0APREFIX+dct%3A+%3Chttp%3A%2F%2Fpurl.org%2Fdc%2Fterms%2F%3E%0D%0APREFIX+owl%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2002%2F07%2Fowl%23%3E%0D%0APREFIX+prov%3A+%3Chttp%3A%2F%2Fwww.w3.org%2Fns%2Fprov%23%3E%0D%0APREFIX+rdfs%3A+%3Chttp%3A%2F%2Fwww.w3.org%2F2000%2F01%2Frdf-schema%23%3E%0D%0A%0D%0ASELECT+%3Furl+%3Fname+%3Fabstract+%3FwasDerivedFrom+where+%7B%0D%0A+%3Furl+a+dbo%3ADisease%3B%0D%0A+++++++rdfs%3Alabel+%3Fname%3B%0D%0A+++++++dbo%3Aabstract+%3Fabstract%3B%0D%0A+++++++prov%3AwasDerivedFrom+%3FwasDerivedFrom.%0D%0A+++++++FILTER+%28LANG%28%3Fabstract%29%3D%27en%27%29%0D%0A+++++++FILTER+%28LANG%28%3Fname%29%3D%27en%27%29%0D%0A%7D+LIMIT+10&format=application%2Fsparql-results%2Bjson&CXML_redir_for_subjs=121&CXML_redir_for_hrefs=&timeout=30000&debug=on&run=+Run+Query+";
	
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
		    	try {
			    	Disease d = diseaseCatalog.addDisease(name, desc, derivedFrom);
			    	diseases.add(d);
		    	}catch(SQLException e) {}

		    }
		    
		    reader.close();
		    
		    
		    return diseases;
		} catch (IOException  e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}
	

	@Override
	public boolean update(Disease disease) {
		// TODO Auto-generated method stub
		return false;
	}

}
