package pt.ulisboa.ciencias.di.aw1718.group06.crawler.startup;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.DbPediaCrawler;
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
import java.util.List;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final String CONFIG_FILE_NAME = "config.properties";

	public static void main(String[] args) {

        MysqlDataSource dataSource = getDataSourceFromConfig(CONFIG_FILE_NAME);
        if (dataSource == null) {
            logger.error("Failed to get data source from config: {}", CONFIG_FILE_NAME);
            return;
        }

        Twitter twitter = TwitterFactory.getSingleton();

        try(Connection conn = dataSource.getConnection()) {
            DiseaseCatalog catalog = new DiseaseCatalog(conn);
            
            DbPediaCrawler dbpediaCrawler = new DbPediaCrawler(catalog);
            List<Disease> diseases = dbpediaCrawler.update();
            
            TwitterCrawler twitterCrawler = new TwitterCrawler(catalog, twitter);
            
            for (Disease d : diseases) {
            	twitterCrawler.update(d);
                //logger.info("{}: {} (from: {}) - {}", d.getId(), d.getName(), d.getDescription(), d.getDerivedFrom());
            }

        } catch (SQLException e) {
            logger.error("Error while connecting to database: ", e);
        }

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
}
