package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.Feedback;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.FullImage;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.FullPubMed;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.FullTweet;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Disease;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Image;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.PubMed;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.Tweet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DiseaseCatalog {

	private static final Logger LOG = LoggerFactory.getLogger(DiseaseCatalog.class);
	private Connection connection;

	/*  DISEASES  */
	private static final String SQL_SELECT_ALL_DISEASES = "SELECT * FROM diseases";
	private static final String SQL_SELECT_SINGLE_DISEASE_BY_NAME = "SELECT * FROM diseases WHERE id = ? OR name = ?";
	private static final String SQL_SELECT_FRAGMENT_DISEASES = "SELECT * FROM diseases WHERE name LIKE ?";
	private static final String SQL_SELECT_ID_BY_NAME = "SELECT id FROM diseases WHERE name=?";
	private static final String SQL_INSERT_DISEASE = "INSERT INTO diseases (name, abstract, was_derived_from) VALUES (?, ?, ?)";   
	private static final String SQL_COUNT_DISEASES = "SELECT COUNT(*) FROM diseases";
	
	/*  TWEETS  */
	private static final String SQL_SELECT_TWEETS_BY_DISEASE_ID = "SELECT * FROM tweets, diseases_tweets WHERE diseases_tweets.id_diseases = ? AND tweets.id = diseases_tweets.id_tweets;";
	private static final String SQL_INSERT_TWEET = "INSERT INTO tweets (url, text, pub_date) VALUES (?, ?, ?)";
	private static final String SQL_INSERT_TWEET_DISEASE_LINKING = "INSERT INTO diseases_tweets (id_diseases, id_tweets, id_original_disease) VALUES (?, ?, ?)";

	/*  PUBMED  */
	private static final String SQL_SELECT_PUBMEDS_BY_DISEASE_ID = "SELECT * FROM pubmed, diseases_pubmed WHERE diseases_pubmed.id_diseases = ? AND pubmed.id = diseases_pubmed.id_pubmed;";
	private static final String SQL_SELECT_ALL_PUBMEDS = "SELECT * FROM pubmed";
	private static final String SQL_SELECT_ID_BY_PUBMEDID = "SELECT id FROM pubmed WHERE pubmedID = ?";
	private static final String SQL_INSERT_PUBMED = "INSERT INTO pubmed (pubmedID, title, abstract, pub_date) VALUES (?, ?, ?, ?)";
	private static final String SQL_INSERT_PUBMED_DISEASE_LINKING = "INSERT INTO diseases_pubmed (id_diseases, id_pubmed, id_original_disease, occurrences) VALUES (?, ?, ?, ?)";

	/*  IMAGES  */
	private static final String SQL_SELECT_IMAGES_BY_DISEASE_ID = "SELECT * FROM images, diseases_images WHERE diseases_images.id_diseases = ? AND images.id = diseases_images.id_images;";
	private static final String SQL_INSERT_IMAGE = "INSERT INTO images (url) VALUES (?)";
	private static final String SQL_INSERT_IMAGE_DISEASE_LINKING = "INSERT INTO diseases_images (id_diseases, id_images) VALUES (?, ?)";

	/*  DISEASES_PUBMED  */
	private static final String SQL_SELECT_PUBMED_FEEDBACK = "SELECT * FROM diseases_pubmed WHERE id_diseases = ? AND pubmed.id = ?;";
	private static final String SQL_UPDATE_PUBMED_FEEDBACK = "UPDATE diseases_pubmed SET implicit_feedback = ?, explicit_feedback = ? WHERE id_diseases = ? AND pubmed.id = ?;";
	private static final String SQL_SELECT_PAIR_DISEASEID_PUBMEDID = "SELECT * FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_SELECT_PUBMED_RELATED_DISEASES = "SELECT d.name FROM diseases d, diseases_pubmed dp WHERE d.id=dp.id_diseases AND dp.id_pubmed = ?";
	private static final String SQL_UPDATE_PUBMED_RANK = "UPDATE diseases_pubmed SET rank = ? WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_UPDATE_PUBMED_OCCURRENCES = "UPDATE diseases_pubmed SET occurrences = ? WHERE id_diseases = ? AND id_pubmed = ?";
	
	/*  DISEASES_TWEETS */
	private static final String SQL_SELECT_TWEET_FEEDBACK = "SELECT * FROM diseases_tweets WHERE id_diseases = ? AND tweets.id = ?;";
	private static final String SQL_UPDATE_TWEET_FEEDBACK = "UPDATE diseases_tweets SET implicit_feedback = ?, explicit_feedback = ? WHERE id_diseases = ? AND tweets.id = ?;";
	private static final String SQL_UPDATE_TWEET_RANK = "UPDATE diseases_tweets SET rank = ? WHERE id_diseases = ? AND id_pubmed = ?";

	/*  DISEASES_IMAGES */
	private static final String SQL_SELECT_IMAGE_FEEDBACK = "SELECT * FROM diseases_images WHERE id_diseases = ? AND image.id = ?;";
	private static final String SQL_UPDATE_IMAGE_FEEDBACK = "UPDATE diseases_images SET implicit_feedback = ?, explicit_feedback = ? WHERE id_diseases = ? AND image.id = ?;";

	public DiseaseCatalog(String configFileName) throws SQLException {
		MysqlDataSource dataSource = getDataSourceFromConfig(configFileName);
		connection = dataSource.getConnection();
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

	public Disease addDisease(String name, String description, String derivedFrom) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_INSERT_DISEASE, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, name);
		statement.setString(2, description);
		statement.setString(3, derivedFrom);

		try {
			int affected = statement.executeUpdate();
			if (affected == 0) {
				throw new SQLException("Creating entry failed, no rows affected.");
			}
		}catch(SQLIntegrityConstraintViolationException e) {
			throw new SQLException("SQLIntegrityConstraint: " + e.getMessage());
		}

		try (ResultSet keys = statement.getGeneratedKeys()) {
			if (keys.next()) {
				int id = keys.getInt(1);
				return new Disease(id, name, description, derivedFrom);
			}
		}
		throw new SQLException("Retrieving generated id failed.");

	}


	public List<Disease> getDiseases(int limit) throws SQLException {
		ArrayList<Disease> results = new ArrayList<>();
		Statement stmt = connection.createStatement();
		String query = SQL_SELECT_ALL_DISEASES;

		if (limit > 0)
			query += " LIMIT " + limit;

		try (ResultSet result = stmt.executeQuery(query)) {
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				String description = result.getString("abstract");
				String derived = result.getString("was_derived_from");
				results.add(new Disease(id, name, description, derived));
			}
		}
		return results;
	}

	public List<Disease> getFragmentDiseases(String fragment) throws SQLException {
		ArrayList<Disease> results = new ArrayList<>();
		String query = SQL_SELECT_FRAGMENT_DISEASES;
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setString(1, "%" + fragment + "%");
		try (ResultSet result = preparedStatement.executeQuery()) {
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				String description = result.getString("abstract");
				String derived = result.getString("was_derived_from");
				results.add(new Disease(id, name, description, derived));
			}
		}
		return results;
	}

	public Disease getDisease(String searchTerm) throws SQLException {
		Disease disease = null;
		PreparedStatement statement = connection.prepareStatement(SQL_SELECT_SINGLE_DISEASE_BY_NAME);
		statement.setString(1, searchTerm);
		statement.setString(2, searchTerm);

		try (ResultSet result = statement.executeQuery()) {
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				String description = result.getString("abstract");
				String derived = result.getString("was_derived_from");
				disease = new Disease(id, name, description, derived);
			}
		}
		return disease;
	}

	public Tweet createTweet(Disease disease, long tweetId, String text, int originalDiseaseID, Date date) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_INSERT_TWEET, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, String.valueOf(tweetId));
		statement.setString(2, text);
		statement.setDate(3, date);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Creating entry failed, no rows affected.");
		}

		try (ResultSet keys = statement.getGeneratedKeys()) {
			if (keys.next()) {
				int id = keys.getInt(1);
				// Add entry in linking table.
				PreparedStatement stmt = connection.prepareStatement(SQL_INSERT_TWEET_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, disease.getId());
				stmt.setInt(2, id);
				stmt.setInt(3, originalDiseaseID);

				affected = stmt.executeUpdate();
				if (affected == 0) {
					throw new SQLException("Creating entry failed, no rows affected.");
				}
				return new Tweet(id, String.valueOf(id), text);
			}
		}
		throw new SQLException("Retrieving generated id failed.");
	}

	public List<FullTweet> getFullTweetsByDiseaseId(String diseaseId) throws SQLException {
		ArrayList<FullTweet> results = new ArrayList<>();
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_TWEETS_BY_DISEASE_ID);
		preparedStatement.setString(1, diseaseId);
		try (ResultSet result = preparedStatement.executeQuery()) {
			while (result.next()) {
				int id = result.getInt("id");
				String url = result.getString("url");
				String text = result.getString("text");
				Date pubDate = result.getDate("pub_date");
				int idOriginalDisease = result.getInt("id_original_disease");
				int relevance = result.getInt("relevance");
				int implicitFeedback = result.getInt("implicit_feedback");
				int explicitFeedback = result.getInt("explicit_feedback");
				results.add(new FullTweet(id, url, text, pubDate, idOriginalDisease, implicitFeedback, explicitFeedback, relevance));
			}
		}
		return results;
	}

	public PubMed addPubMedInfo(int diseaseID, int pubmedID, String title, String abstrct, Date date) throws SQLException {

		PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ID_BY_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, String.valueOf(pubmedID));
		try(ResultSet keys = statement.executeQuery()){
			boolean exists = keys.next();

			if(exists) {
				//pubmed article already exists in table pubmed
				//add linking to this disease
				int id = keys.getInt("id");
				addPubMedDiseaseLink(diseaseID, id, diseaseID, 1);
				return new PubMed(id, pubmedID, title, abstrct);
			} else {
				statement = connection.prepareStatement(SQL_INSERT_PUBMED, Statement.RETURN_GENERATED_KEYS);
				statement.setString(1, String.valueOf(pubmedID));
				statement.setString(2, title);
				statement.setString(3, abstrct);
				statement.setDate(4, date);

				int affected = statement.executeUpdate();
				if (affected == 0) {
					throw new SQLException("Creating entry failed, no rows affected.");
				}

				try (ResultSet kys = statement.getGeneratedKeys()) {
					if (kys.next()) {
						int id = kys.getInt(1);
						// Add entry in linking table.
						addPubMedDiseaseLink(diseaseID, id, diseaseID, 1);
						return new PubMed(id, pubmedID, title, abstrct);
					}
				}
				throw new SQLException("Retrieving generated id failed.");
			}
		}  	
	} 

	public void addPubMedDiseaseLink(int diseaseID, int id, int originalDiseaseID, int occurrences) throws SQLException {

		PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_PAIR_DISEASEID_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
		stmt.setInt(1, diseaseID);
		stmt.setInt(2, id);
		boolean exists = false;
		try(ResultSet keys = stmt.executeQuery()){
			exists = keys.next();
		
			if(!exists) { // pair (id_diseases,id_pubmed) doesnt exist 
				PreparedStatement insertLinking = connection.prepareStatement(SQL_INSERT_PUBMED_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
				insertLinking.setInt(1, diseaseID);
				insertLinking.setInt(2, id);
				insertLinking.setInt(3, originalDiseaseID);
				insertLinking.setInt(4, occurrences);
				int affected = insertLinking.executeUpdate();
				if (affected == 0) {
					throw new SQLException("Creating entry failed, no rows affected.");
				}
			} else {
				int id_disease = keys.getInt("id_diseases");
				int id_pubmed = keys.getInt("id_pubmed");
				int oldOccurrences = keys.getInt("occurrences");
				if(occurrences > oldOccurrences) {
					//to prevent setting occurrences to 1 when it was already >1
					//for 'new' diseases
					PreparedStatement updateOccurrences = connection.prepareStatement(SQL_UPDATE_PUBMED_OCCURRENCES);
					updateOccurrences.setInt(1, occurrences);
					updateOccurrences.setInt(2, id_disease);
					updateOccurrences.setInt(3, id_pubmed);
					int affected = updateOccurrences.executeUpdate();
					if (affected == 0) {
						throw new SQLException("Creating entry failed, no rows affected.");
					}
				}
			}	
		}
	}

	public Image createImage(Disease disease, String url) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_INSERT_IMAGE, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, url);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Creating entry failed, no rows affected.");
		}

		try (ResultSet keys = statement.getGeneratedKeys()) {
			if (keys.next()) {
				int id = keys.getInt(1);
				// Add entry in linking table.
				PreparedStatement stmt = connection.prepareStatement(SQL_INSERT_IMAGE_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, disease.getId());
				stmt.setInt(2, id);

				affected = stmt.executeUpdate();
				if (affected == 0) {
					throw new SQLException("Creating entry failed, no rows affected.");
				}
				return new Image(id, url);
			}
		}
		throw new SQLException("Retrieving generated id failed.");
	}

	public List<FullImage> getFullImagesByDiseaseId(String diseaseId) throws SQLException {
		ArrayList<FullImage> results = new ArrayList<>();
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_IMAGES_BY_DISEASE_ID);
		preparedStatement.setString(1, diseaseId);
		try (ResultSet result = preparedStatement.executeQuery()) {
			while (result.next()) {
				int id = result.getInt("id");
				String url = result.getString("url");
				boolean blackListed = result.getBoolean("black_listed");
				int implicitFeedback = result.getInt("implicit_feedback");
				int explicitFeedback = result.getInt("explicit_feedback");
				results.add(new FullImage(id, url, implicitFeedback, explicitFeedback, blackListed));
			}
		}
		return results;
	}

	public List<PubMed> getAllPubMed() throws SQLException {
		ArrayList<PubMed> results = new ArrayList<>();
		Statement stmt = connection.createStatement();
		try(ResultSet result = stmt.executeQuery(SQL_SELECT_ALL_PUBMEDS)){
			while(result.next()) {
				int id = result.getInt("id");
				int pubmedid = result.getInt("pubmedID");
				String title = result.getString("title");
				String abstrct = result.getString("abstract");
				results.add(new PubMed(id, pubmedid, title, abstrct));
			}
		}
		return results;
	}

	public List<FullPubMed> getFullPubmedsByDiseaseId(String diseaseId) throws SQLException {
		ArrayList<FullPubMed> results = new ArrayList<>();
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_PUBMEDS_BY_DISEASE_ID);
		preparedStatement.setString(1, diseaseId);
		try (ResultSet result = preparedStatement.executeQuery()) {
			while (result.next()) {
				int id = result.getInt("id");
				int pubMedId = result.getInt("pubmedID");
				String title = result.getString("title");
				String description = result.getString("abstract");
				int idOriginalDisease = result.getInt("id_original_disease");
				int relevance = result.getInt("relevance");
				int implicitFeedback = result.getInt("implicit_feedback");
				int explicitFeedback = result.getInt("explicit_feedback");
				results.add(new FullPubMed(id, pubMedId, title, description, idOriginalDisease, implicitFeedback, explicitFeedback, relevance));
			}
		}
		return results;
	}

	public int getDiseaseID(String diseaseName) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ID_BY_NAME, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, diseaseName);
		int id = -1;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next())
				id = keys.getInt(1);
		}
		return id;
	}

	private Feedback getFeedback(int diseaseId, int objectId, String query) throws SQLException {
		PreparedStatement preparedStatement = connection.prepareStatement(query);
		preparedStatement.setInt(1, diseaseId);
		preparedStatement.setInt(2, objectId);
		try (ResultSet result = preparedStatement.executeQuery()) {
			if (result.next()) {
				int implicitFeedback = result.getInt("implicit_feedback");
				int explicitFeedback = result.getInt("explicit_feedback");
				return new Feedback(diseaseId, objectId, implicitFeedback, explicitFeedback);
			}
		}
		throw new SQLException("Instance not found");
	}

	private boolean updateFeedback(Feedback feedback, String query) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(query);
		statement.setInt(1, feedback.getImplicitFeedback());
		statement.setInt(2, feedback.getExplicitFeedback());
		statement.setInt(3, feedback.getDiseaseId());
		statement.setInt(4, feedback.getObjectId());
		return statement.executeUpdate() != 0;
	}

	private void performFeedbackUpdate(Feedback feedback, Feedback.Operations operation) {
		if(Feedback.Operations.INCREMENT_IMPLICIT == operation) {
			feedback.incrementImplicitFeedback();
		} else if(Feedback.Operations.INCREMENT_EXPLICIT == operation) {
			feedback.incrementExplicitFeedback();
		} else if(Feedback.Operations.DECREMENT_IMPLICIT == operation) {
			feedback.decrementImplicitFeedback();
		} else {
			feedback.decrementExplicitFeedback();
		}
	}

	public boolean updatePubMeFeedback(int diseaseId, int pubMedId, Feedback.Operations operation) throws SQLException {
		Feedback feedback = getFeedback(diseaseId, pubMedId, SQL_SELECT_PUBMED_FEEDBACK);
		performFeedbackUpdate(feedback, operation);
		return updateFeedback(feedback, SQL_UPDATE_PUBMED_FEEDBACK);
	}

	public boolean updateImageFeedback(int diseaseId, int imageId, boolean increment) throws SQLException {
		Feedback feedback = getFeedback(diseaseId, imageId, SQL_SELECT_IMAGE_FEEDBACK);
		if(increment) {
			feedback.incrementImplicitFeedback();
		} else {
			feedback.decrementImplicitFeedback();
		}
		return updateFeedback(feedback, SQL_UPDATE_IMAGE_FEEDBACK);
	}

	public boolean updateTweetFeedback(int diseaseId, int tweetId, boolean increment) throws SQLException {
		Feedback feedback = getFeedback(diseaseId, tweetId, SQL_SELECT_TWEET_FEEDBACK);
		if(increment) {
			feedback.incrementImplicitFeedback();
		} else {
			feedback.decrementImplicitFeedback();
		}
		return updateFeedback(feedback, SQL_UPDATE_TWEET_FEEDBACK);
	}

	// TODO delete?
	public boolean updatePubMedRank(int diseaseId, int pubmedId, double ranking) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_PUBMED_RANK);
		statement.setDouble(1, ranking);
		statement.setInt(2, diseaseId);
		statement.setInt(3, pubmedId);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Updating entry failed, no rows affected.");
		}
		return true;
	}

	// TODO delete?
	public boolean updateTweetRank(int diseaseId, int tweetId, double ranking) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_TWEET_RANK);
		statement.setDouble(1, ranking);
		statement.setInt(2, diseaseId);
		statement.setInt(3, tweetId);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Updating entry failed, no rows affected.");
		}
		return true;
	}

	public int getNumDiseases() throws SQLException {
		int count = 0;
		Statement statement = connection.createStatement();
		try(ResultSet result = statement.executeQuery(SQL_COUNT_DISEASES)){
			if(result.next())
				count = result.getInt(1);
		}
		return count;
	}

	public List<String> getRelatedDiseases(int id) throws SQLException {
		ArrayList<String> diseases = new ArrayList<>();
		PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_PUBMED_RELATED_DISEASES);
		stmt.setInt(1, id);
		try(ResultSet result = stmt.executeQuery()){
			while(result.next()) {
				String name = result.getString("name");
				diseases.add(name);
			}
		}
		return diseases;
	}

	
}
