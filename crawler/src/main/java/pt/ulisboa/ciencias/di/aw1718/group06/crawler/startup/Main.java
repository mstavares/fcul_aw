package pt.ulisboa.ciencias.di.aw1718.group06.crawler.startup;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.crawlers.TwitterCrawler;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) {

        String configFileName = "config.properties";
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
            return;
        } catch (IOException e) {
            logger.error("Error while reading properties file.", e);
            return;
        }

        DiseaseCatalog catalog = DiseaseCatalog.getInstance();

        try(Connection conn = dataSource.getConnection()) {

            String SQL_INSERT = "INSERT INTO diseases (name, abstract, was_derived_from) VALUES (?, ?, ?)";
            PreparedStatement statement = conn.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS);
            String diseaseName = "asthma";
            String diseaseDescription = "asthma description";
            String derivedFrom = "derived from";
            statement.setString(1, diseaseName);
            statement.setString(2, diseaseDescription);
            statement.setString(3, derivedFrom);

            int affected = statement.executeUpdate();
            if (affected == 0) {
                throw new SQLException("Creating entry failed, no rows affected.");
            }

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int id = keys.getInt(1);
                    Disease disease = new Disease(id, diseaseName, diseaseDescription, derivedFrom);

                    Twitter twitter = TwitterFactory.getSingleton();
                    TwitterCrawler twitterCrawler = new TwitterCrawler(catalog, twitter);
                    twitterCrawler.update(disease);
                }
            }

            Statement stmt = conn.createStatement();
            try (ResultSet result = stmt.executeQuery("SELECT * FROM diseases")) {
                while (result.next()) {
                    int id = result.getInt("id");
                    String name = result.getString("name");
                    String description = result.getString("abstract");
                    String derived = result.getString("was_derived_from");
                    logger.info("{}: {} (from: {}) - {}", id, name, derived, description);
                }
            }

        } catch (SQLException e) {
            logger.error("Error while connecting to database: ", e);
        }

    }
}
