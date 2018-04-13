package pt.ulisboa.ciencias.di.aw1718.group06.crawler.startup;

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
import java.util.*;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableMap;
import javafx.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mysql.cj.jdbc.MysqlDataSource;

import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.CompoundRanker;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.Index;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.PubMedRanked;
import pt.ulisboa.ciencias.di.aw1718.group06.crawler.index.TfIdfRanker;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.DiseaseCatalog;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.PubMed;

public class MainRanking {
	private static final Logger logger = LoggerFactory.getLogger(MainRanking.class);
	private static final String CONFIG_FILE_NAME = "config.properties";

	private final static String BASE_URL_MER = "http://labs.fc.ul.pt/mer/api.php?lexicon=disease&text=";

	public static void main(String[] args) throws IOException {

		MysqlDataSource dataSource = getDataSourceFromConfig(CONFIG_FILE_NAME);
		if (dataSource == null) {
			return;
		}

		try(Connection conn = dataSource.getConnection()) {
			DiseaseCatalog catalog = new DiseaseCatalog(conn);

			List<PubMed> pubmeds = catalog.getAllPubMed();
            List<Pair<PubMed, List<Integer>>> pubMedsAnnotated = new ArrayList<>();

            for(PubMed p : pubmeds) {
				logger.info(""+p.getId());
				List<String> relatedDiseases = getAnnotationsFromAbstract(p.getDescription());
				if(relatedDiseases != null) {
                    List<Integer> disIds = new ArrayList<>();

                    for(String diseaseName : relatedDiseases) {
						logger.info(diseaseName);
						int diseaseId = catalog.getDiseaseID(diseaseName);
						if(diseaseId != -1) {
                            catalog.addPubMedDiseaseLink(diseaseId, p.getId(), diseaseId);
                            disIds.add(diseaseId);
                        }
					}
					pubMedsAnnotated.add(new Pair<>(p, disIds));
				}
			}

            List<Disease> diseases = catalog.getDiseases(0);
            Map<Integer, Disease> diseaseMap = diseases.stream()
                .collect(Collectors.toMap(Disease::getId, d -> d));

            TfIdfRanker tfIdfRanker = new TfIdfRanker(diseaseMap);
//          DiShInRanker diShInRanker = new DiShInRanker(diseaseMap);

            CompoundRanker ranker = new CompoundRanker(ImmutableMap.of(
//              diShInRanker, 0.3,
//              tfIdfRanker, 0.5,
//              feedbackRanker, 0.2,
                tfIdfRanker, 1.0
            ));

            Index index = new Index(diseaseMap, pubMedsAnnotated, ranker);
            index.build(pubMedsAnnotated);

            for (Disease disease : diseases) {
                List<PubMedRanked> articles = index.getArticlesFor(disease.getId());

                for (PubMedRanked article : articles) {
                    catalog.updatePubMedRank(disease.getId(), article.getPubMed().getId(), article.getRank().get());
                }
            }

        } catch (SQLException e) {
			//e.printStackTrace();
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


	private static List<String> getAnnotationsFromAbstract(String abstrct) throws MalformedURLException, UnsupportedEncodingException {
		ArrayList<String> annotations = new ArrayList<>();
		URL url = new URL(BASE_URL_MER + URLEncoder.encode(abstrct, "UTF-8"));
		BufferedReader reader;
		try{
			InputStream is = url.openConnection().getInputStream();

			reader = new BufferedReader(new InputStreamReader(is));
			String s = "";
			while((s = reader.readLine()) != null) {
				String [] split = s.split("\t");
				String diseaseName = split[split.length-1];
				if(!annotations.contains(diseaseName))
					annotations.add(diseaseName);
			}
			reader.close();
		} catch(IOException e) {
			return null;
		}
		
		return annotations;
	}


}
