package pt.ulisboa.ciencias.di.aw1718.group06.crawler.startup;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
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

        try(Connection conn = dataSource.getConnection()) {
            Statement stmt = conn.createStatement();
            ResultSet result = stmt.executeQuery("SELECT * FROM diseases");

            while (result.next()) {
                int id = result.getInt("id");
                String name = result.getString("name");
                String description = result.getString("abstract");
                String derivedFrom = result.getString("was_derived_from");
                logger.info("{}: {} (from: {}) - {}", id, name, derivedFrom, description);
            }
        } catch (SQLException e) {
            logger.error("Error while connecting to database: ", e);
        }
	}
}
