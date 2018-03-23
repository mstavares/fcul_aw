package pt.ulisboa.ciencias.di.aw1718.group06.crawler.startup;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.DbPediaCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.PubMedCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.FlickrCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.TwitterCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String CONFIG_FILE_NAME = "config.properties";

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
            //logger.error("Failed to get data source from config: {}", CONFIG_FILE_NAME);
            return;
        }
        
        System.setProperty("twitter4j.loggerFactory", "twitter4j.NullLoggerFactory");
        Twitter twitter = TwitterFactory.getSingleton();

        try(Connection conn = dataSource.getConnection()) {
            DiseaseCatalog catalog = new DiseaseCatalog(conn);
            
            List<Disease> diseases = catalog.getDiseases(limit);
            
            if (singleDisease != null) {
            	Disease d = catalog.getDisease(singleDisease);
            	if (d != null) {
            		diseases = new ArrayList<Disease>();
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

        } catch (SQLException e) {
            //logger.error("Error while connecting to database: ", e);
        }

    }

    private static MysqlDataSource getDataSourceFromConfig(String configFileName) {
        MysqlDataSource dataSource;
        try (InputStream input = ClassLoader.getSystemResourceAsStream(configFileName)) {
            //logger.info("Reading properties from file: {}.", configFileName);
            Properties props = new Properties();
            props.load(input);
            dataSource = new MysqlDataSource();
            dataSource.setUser(props.getProperty("db.user"));
            dataSource.setPassword(props.getProperty("db.password"));
            dataSource.setServerName(props.getProperty("db.hostname"));
            dataSource.setDatabaseName(props.getProperty("db.name"));
            dataSource.setPort(Integer.valueOf(props.getProperty("db.port")));

        } catch (FileNotFoundException e) {
            //logger.error("Properties file not found.", e);
            return null;
        } catch (IOException e) {
            //logger.error("Error while reading properties file.", e);
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
