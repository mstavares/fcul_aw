package pt.ulisboa.ciencias.di.aw1718.group06.ws;

import com.google.common.collect.ImmutableMap;
import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.CompoundRanker;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.Index;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.RankType;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
public class ServiceConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ServiceConfig.class);

    private static final String CONFIG_FILE_NAME = "config.properties";

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public Index getPubmedIndex() throws SQLException {
        return new Index(getCompoundRanker(), getDiseaseCatalog());
    }

    @Bean
    public CompoundRanker getCompoundRanker() {
        return new CompoundRanker(ImmutableMap.of(
            RankType.TF_IDF_RANK, 0.3,
            RankType.DATE_RANK, 0.1,
            RankType.EXPLICIT_FEEDBACK_RANK, 0.4,
            RankType.IMPLICIT_FEEDBACK_RANK, 0.2
        ));
    }

    @Bean
    public MysqlDataSource getJdbcDataSource() {
        return getDataSourceFromConfig(CONFIG_FILE_NAME);
    }

    @Bean
    public DiseaseCatalog getDiseaseCatalog() throws SQLException {
        return new DiseaseCatalog(getJdbcDataSource().getConnection());
    }

    private MysqlDataSource getDataSourceFromConfig(String configFileName) {
        MysqlDataSource dataSource;
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream(configFileName)) {
            LOG.info("Reading properties from file: {}.", configFileName);
            Properties props = new Properties();
            props.load(input);
            dataSource = new MysqlDataSource();
            dataSource.setUser(props.getProperty("db.user"));
            dataSource.setPassword(props.getProperty("db.password"));
            dataSource.setServerName(props.getProperty("db.hostname"));
            dataSource.setDatabaseName(props.getProperty("db.name"));
            dataSource.setPort(Integer.valueOf(props.getProperty("db.port")));
        } catch (FileNotFoundException e) {
            LOG.error("Properties file not found.", e);
            return null;
        } catch (IOException e) {
            LOG.error("Error while reading properties file.", e);
            return null;
        }
        return dataSource;
    }
}
