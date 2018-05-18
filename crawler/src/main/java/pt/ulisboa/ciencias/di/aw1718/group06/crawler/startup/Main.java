package pt.ulisboa.ciencias.di.aw1718.group06.crawler.startup;

import com.google.common.collect.ImmutableMap;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.DbPediaCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.PubMedCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.FlickrCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.TwitterCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.CompoundRanker;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.Index;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.RankType;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Tweet;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javafx.util.Pair;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String CONFIG_FILE_NAME = "config.properties";
    
    private static final int MAX_DISEASES = 50;
    
    private final static String BASE_URL_MER = "http://labs.fc.ul.pt/mer/api.php?lexicon=disease&text=";

	public static void main(String[] args) {
		
		boolean forceUpdate = forceUpdate(args);
		String singleDisease = getSingleDisease(args);
		int limit = getLimit(args);
		
		if (limit > 0)
			logger.info("Only updating " + limit + " diseases.");
		else
			logger.info("WARNING: No diseases limit.");
		
        MysqlDataSource dataSource = getDataSourceFromConfig(CONFIG_FILE_NAME);
        if (dataSource == null) {
            logger.error("Failed to get data source from config: {}", CONFIG_FILE_NAME);
            return;
        }
        
        System.setProperty("twitter4j.loggerFactory", "twitter4j.NullLoggerFactory");
        Twitter twitter = TwitterFactory.getSingleton();

        try {
			DiseaseCatalog catalog = new DiseaseCatalog(dataSource.getConnection());
            
            List<Disease> diseases = catalog.getDiseases(limit);
            
            if (singleDisease != null) {
            	Disease d = catalog.getDisease(singleDisease);
            	if (d != null) {
            		diseases = new ArrayList<>();
            		diseases.add(d);
            	}else {
            		logger.info("Couldn't find the disease " + singleDisease);
            		System.exit(-1);
            	}
            }
            
            if (forceUpdate || diseases.size() == 0) {
                DbPediaCrawler dbpediaCrawler = new DbPediaCrawler(catalog, limit);
                List<Disease> temp = dbpediaCrawler.update();
                logger.info("Added " + temp.size() + " diseases to the database.");
                
                if (singleDisease == null)
                	diseases = temp;
            }

            //get base diseases and related info
            TwitterCrawler twitterCrawler = new TwitterCrawler(catalog, twitter);
            PubMedCrawler pubmedCrawler = new PubMedCrawler(catalog);
            FlickrCrawler flickrCrawler = new FlickrCrawler(catalog);
            
            for (Disease d : diseases) {
            	boolean sucT = twitterCrawler.update(d);
            	boolean sucP = pubmedCrawler.update(d);
            	boolean sucF = flickrCrawler.update(d);
                String s = (sucT? "twitter, " : "")
                	+ (sucP? "pubmed, " : "")
                	+ (sucF? "flickr" : "");
                logger.info("Updated " + s + " info for " + d.getName());
            }
            
            List<PubMed> pubmeds = catalog.getAllPubMed();
            List<Integer> checkedPubmeds = new ArrayList<Integer>();
            
            int numDiseases = limit;
            
            //get pubmed-mentioned diseases and related info 
            while(numDiseases < MAX_DISEASES && pubmeds.size() > 0) {
            	for(PubMed p : pubmeds) {
            		//<DiseaseName,occurences in abstract>
                	Map<String, Pair<Integer, String>> annotations = getAnnotations(p.getDescription());
                	if(annotations != null) {
                		for(String diseaseName : annotations.keySet()) {
                			int occurrences = annotations.get(diseaseName).getKey();
                			String places = annotations.get(diseaseName).getValue();
                			Disease disease = catalog.getDisease(diseaseName);
                			if(disease != null) {
                				//disease already in our db
                				catalog.addPubMedDiseaseLink(disease.getId(), p.getId(), occurrences, places);
                				logger.info("Added link between " + disease.getId() + " and " + p.getId());
                			} else {
                				//disease not in our db
                				DbPediaCrawler dbpediaCrawler = new DbPediaCrawler(catalog);
                				Disease newDisease = dbpediaCrawler.getSingleDisease(diseaseName);
                				if(newDisease != null) {
                					logger.info("Added "+newDisease.getName()+" to DB");
                					boolean sucT = twitterCrawler.update(newDisease);
                	            	boolean sucP = pubmedCrawler.update(newDisease);
                	            	boolean sucF = flickrCrawler.update(newDisease);
                	                String s = (sucT? "twitter, " : "")
                	                	+ (sucP? "pubmed, " : "")
                	                	+ (sucF? "flickr" : "");
                	                logger.info("Updated " + s + " info for " + newDisease.getName());
                	                catalog.addPubMedDiseaseLink(newDisease.getId(), p.getId(), occurrences, places);
                				}
                			}
                		}
                	}
                	checkedPubmeds.add(p.getPubMedId());
                }
            	numDiseases = catalog.getNumDiseases();
            	pubmeds = catalog.getAllPubMed().stream()
            			.filter(pubmed -> !checkedPubmeds.contains(pubmed.getPubMedId()))
            			.collect(Collectors.toList());
            }
            
            logger.info("Not adding more diseases!");
            logger.info("Adding remaining disease-pubmed linkings");
            
            //link pubmed-mentioned diseases to each pubmed
            for(PubMed p : pubmeds) {
            	Map<String, Pair<Integer, String>> annotations = getAnnotations(p.getDescription());
            	if(annotations != null) {
            		for(String diseaseName : annotations.keySet()) {
            			int occurrences = annotations.get(diseaseName).getKey();
            			String places = annotations.get(diseaseName).getValue();
            			Disease disease = catalog.getDisease(diseaseName);
            			if(disease != null) {
            				//we dont want to add any more diseases now, but we still have to add the links if 
            				//its a disease we already have
            				catalog.addPubMedDiseaseLink(disease.getId(), p.getId(), occurrences, places);
            				logger.info("Added link between " + disease.getId() + " and " + p.getId());
            			} 
            		}
            	}
            }
            
            logger.info("adding mentioned diseases - tweets linkings");
            
            //link twitter mentioned diseases to each tweet
            List<Tweet> tweets = catalog.getAllTweets();
            for(Tweet t : tweets) {
            	Map<String, Pair<Integer, String>> annotations = getAnnotations(t.getDescription());
            	if(annotations != null) {
            		for(String diseaseName : annotations.keySet()) {
            			Disease disease = catalog.getDisease(diseaseName);
            			if(disease != null) {
            				catalog.addDiseaseTweetLink(disease.getId(), t.getId());
            			}
            		}
            	}
            }

            logger.info("Adding tf and idf to tables");

            // Build inverted index.
            CompoundRanker ranker = new CompoundRanker(ImmutableMap.of(
				RankType.TF_IDF_RANK, 0.3,
				RankType.DATE_RANK, 0.1,
				RankType.EXPLICIT_FEEDBACK_RANK, 0.4,
				RankType.IMPLICIT_FEEDBACK_RANK, 0.2
			));

			Index index = new Index(ranker, catalog);
			index.build();




        } catch (SQLException e) {
            logger.error("Error while connecting to database: " + e.getErrorCode(), e);
        } catch (MalformedURLException e) {
			logger.error("MalformedURLException " + e.getMessage());
		} catch (UnsupportedEncodingException e) {
			logger.error("UnsupportedEncodingException " + e.getMessage());
		}

    }
    
    private static Map<String, Pair<Integer, String>> getAnnotations(String abstrct) throws MalformedURLException, UnsupportedEncodingException {
		HashMap<String, Pair<Integer, String>> annotations = new HashMap<>();
		URL url = new URL(BASE_URL_MER + URLEncoder.encode(abstrct, "UTF-8"));
		BufferedReader reader;
		try{
			InputStream is = url.openConnection().getInputStream();

			reader = new BufferedReader(new InputStreamReader(is));
			String s = "";
			while((s = reader.readLine()) != null) {
				String [] split = s.split("\t");
				String diseaseName = split[split.length-1];
				String place = split[0];
				if(annotations.containsKey(diseaseName)) {
					int occurrences = annotations.get(diseaseName).getKey();
					String places = annotations.get(diseaseName).getValue();
					annotations.replace(diseaseName, new Pair(occurrences+1, places + ";" + place));
				} else {
					annotations.put(diseaseName, new Pair(1, place));
				}
			}
			reader.close();
		} catch(IOException e) {
			return null;
		}
		
		return annotations;
	}

	private static MysqlDataSource getDataSourceFromConfig(String configFileName) {
        MysqlDataSource dataSource;
        try (InputStream input = ClassLoader.getSystemResourceAsStream(configFileName)) {
            logger.info("Reading properties from file: {}.", configFileName);
            Properties props = new Properties();
            props.load(input);
            dataSource = new MysqlDataSource();
            dataSource.setUser(props.getProperty("db.user"));
            dataSource.setPassword(props.getProperty("db.password"));
            dataSource.setServerName(props.getProperty("db.hostname"));
            dataSource.setDatabaseName(props.getProperty("db.name"));
            dataSource.setPort(Integer.valueOf(props.getProperty("db.port")));

        } catch (FileNotFoundException e) {
            logger.error("Properties file not found.", e);
            return null;
        } catch (IOException e) {
            logger.error("Error while reading properties file.", e);
            return null;
        }

        return dataSource;
    }
    
    
    private static boolean forceUpdate(String[] args) {
    	for (String s: args)
    		if (s.equals("-f")) {
    			logger.info("Force updating diseases.");
    			return true;
    		}
    	return false;
    }
    
    
    private static String getSingleDisease(String[] args) {
    	boolean flag = false;	
    	for (String s: args) {
    		if (flag) {
    			logger.info("Updating single diseas: " + s);
    			return s;
    		}		
    		if (s.equals("-d"))
    			flag = true;
    		else
    			flag = false;
    	}	
    	return null;
    }
      
    private static int getLimit(String[] args) {
    	int result = -1;
    	boolean flag = false;
    	for (String s: args) {
    		if (flag) {
    			try {
        			result = Integer.parseInt(s);
        			logger.info("Found limit: " + result);
        			return result;
    			}catch(NumberFormatException e) {
    				return -1;
    			}
    		}	
    		if (s.equals("-s"))
    			flag = true;
    		else
    			flag = false;
    	}	
    	return result;
    }
}
