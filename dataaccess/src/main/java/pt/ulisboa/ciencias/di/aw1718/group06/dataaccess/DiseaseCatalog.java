package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DiseaseCatalog {

	private Connection conn;

	/*  DISEASES  */
	private static final String SQL_SELECT_ALL_DISEASES = "SELECT * FROM diseases";
	private static final String SQL_SELECT_SINGLE_DISEASE = "SELECT * FROM diseases WHERE name = ?";
	private static final String SQL_SELECT_ID_BY_NAME = "SELECT id FROM diseases WHERE name=?";
	private static final String SQL_INSERT_DISEASE = "INSERT INTO diseases (name, abstract, was_derived_from) VALUES (?, ?, ?)";   
	private static final String SQL_COUNT_DISEASES = "SELECT COUNT(*) FROM diseases";
	private static final String SQL_SELECT_DISEASE_IDF = "SELECT idf FROM diseases WHERE id = ?";
	private static final String SQL_UPDATE_DISEASE_IDF = "UPDATE diseases SET idf = ? WHERE id = ?";
	
	/*  TWEETS  */
	private static final String SQL_INSERT_TWEET = "INSERT INTO tweets (url, text, pub_date, id_original_disease) VALUES (?, ?, ?, ?)";
	private static final String SQL_SELECT_ALL_TWEETS = "SELECT * FROM tweets";

	/*  PUBMED  */
	private static final String SQL_SELECT_ALL_PUBMEDS = "SELECT * FROM pubmed";
	private static final String SQL_SELECT_ALL_PUBMED_IDS = "SELECT id FROM pubmed";
	private static final String SQL_SELECT_ID_BY_PUBMEDID = "SELECT id FROM pubmed WHERE pubmedID = ?";
	private static final String SQL_INSERT_PUBMED = "INSERT INTO pubmed (pubmedID, title, abstract, pub_date, id_original_disease) VALUES (?, ?, ?, ?, ?)";
	private static final String SQL_INSERT_PUBMED_DISEASE_LINKING = "INSERT INTO diseases_pubmed (id_diseases, id_pubmed, occurrences) VALUES (?, ?, ?)";
	
	/*  IMAGES  */
	private static final String SQL_INSERT_IMAGE = "INSERT INTO images (url) VALUES (?)";
	private static final String SQL_INSERT_IMAGE_DISEASE_LINKING = "INSERT INTO diseases_images (id_diseases, id_images) VALUES (?, ?)";

	/*  DISEASES_PUBMED  */
	private static final String SQL_SELECT_PAIR_DISEASEID_PUBMEDID = "SELECT * FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_SELECT_PUBMED_RELATED_DISEASES = "SELECT d.name FROM diseases d, diseases_pubmed dp WHERE d.id=dp.id_diseases AND dp.id_pubmed = ?";
	private static final String SQL_SELECT_PUBMED_RELATED_DISEASE_IDS = "SELECT id_diseases FROM diseases_pubmed WHERE id_pubmed = ?";
	private static final String SQL_SELECT_DISEASE_OCCURRENCES_IN_PUBMED = "SELECT occurrences FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_SELECT_ALL_OCCURRENCES_IN_PUBMED = "SELECT SUM(occurrences) FROM diseases_pubmed WHERE id_pubmed = ?";
	private static final String SQL_UPDATE_PUBMED_RANK = "UPDATE diseases_pubmed SET rank = ? WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_UPDATE_PUBMED_OCCURRENCES = "UPDATE diseases_pubmed SET occurrences = ? WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_SELECT_DISEASE_PUBMED_TF = "SELECT tf FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_UPDATE_DISEASE_PUBMED_TF = "UPDATE diseases_pubmed SET tf = ? WHERE id_diseases = ? AND id_pubmed = ?";
	
	/*  DISEASES_TWEETS*/
	private static final String SQL_UPDATE_TWEET_RANK = "UPDATE diseases_tweets SET rank = ? WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_SELECT_PAIR_DISEASEID_TWEETID = "SELECT * FROM diseases_tweets WHERE id_diseases = ? AND id_tweets = ?";
	private static final String SQL_INSERT_TWEET_DISEASE_LINKING = "INSERT INTO diseases_tweets (id_diseases, id_tweets) VALUES (?, ?)";
	
	/* FEEDBACK QUERIES */
	private static final String SQL_GET_PUBMED_DISEASE_MAX_FEEDBACK = "SELECT MAX(implicit_feedback), MAX(explicit_feedback) FROM diseases_pubmed WHERE id_diseases = ?";
	private static final String SQL_GET_TWEET_DISEASE_MAX_FEEDBACK = "SELECT MAX(implicit_feedback), MAX(explicit_feedback) FROM diseases_tweets WHERE id_diseases = ?";	
	private static final String SQL_GET_IMAGE_DISEASE_MAX_FEEDBACK = "SELECT MAX(implicit_feedback), MAX(explicit_feedback) FROM diseases_images WHERE id_diseases = ?";

	public DiseaseCatalog(Connection connection) {
		this.conn = connection;
	}
	
	
	///////////////////////////////////////  DISEASES //////////////////////////////////////////////////
	public Disease addDisease(String name, String description, String derivedFrom) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_INSERT_DISEASE, Statement.RETURN_GENERATED_KEYS);
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
		Statement stmt = conn.createStatement();
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

	public Disease getDisease(String searchTerm) throws SQLException {
		Disease disease = null;

		PreparedStatement statement = conn.prepareStatement(SQL_SELECT_SINGLE_DISEASE);
		statement.setString(1, searchTerm);

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
	
	public int getDiseaseID(String diseaseName) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_SELECT_ID_BY_NAME, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, diseaseName);
		int id = -1;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next())
				id = keys.getInt(1);
		}
		return id;
	}
	
	public int getNumDiseases() throws SQLException {
		int count = 0;
		Statement statement = conn.createStatement();
		try(ResultSet result = statement.executeQuery(SQL_COUNT_DISEASES)){
			if(result.next())
				count = result.getInt(1);
		}
		return count;
	}
	
	///////////////////////////////////////  PUBMED //////////////////////////////////////////////////
	public PubMed addPubMedInfo(int diseaseID, int pubmedID, String title, String abstrct, Date date) throws SQLException {

		PreparedStatement statement = conn.prepareStatement(SQL_SELECT_ID_BY_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, String.valueOf(pubmedID));
		try(ResultSet keys = statement.executeQuery()){
			boolean exists = keys.next();

			if(exists) {
				//pubmed article already exists in table pubmed
				//add linking to this disease
				int id = keys.getInt("id");
				addPubMedDiseaseLink(diseaseID, id, 1);
				return new PubMed(id, pubmedID, title, abstrct);
			} else {
				statement = conn.prepareStatement(SQL_INSERT_PUBMED, Statement.RETURN_GENERATED_KEYS);
				statement.setInt(1, pubmedID);
				statement.setString(2, title);
				statement.setString(3, abstrct);
				statement.setDate(4, date);
				statement.setInt(5,diseaseID);

				int affected = statement.executeUpdate();
				if (affected == 0) {
					throw new SQLException("Creating entry failed, no rows affected.");
				}

				try (ResultSet kys = statement.getGeneratedKeys()) {
					if (kys.next()) {
						int id = kys.getInt(1);
						// Add entry in linking table.
						addPubMedDiseaseLink(diseaseID, id, 1);
						return new PubMed(id, pubmedID, title, abstrct);
					}
				}
				throw new SQLException("Retrieving generated id failed.");
			}
		}  	
	} 
	
	public void addPubMedDiseaseLink(int diseaseID, int id, int occurrences) throws SQLException {

		PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_PAIR_DISEASEID_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
		stmt.setInt(1, diseaseID);
		stmt.setInt(2, id);
		boolean exists = false;
		try(ResultSet keys = stmt.executeQuery()){
			exists = keys.next();

			if(!exists) { // pair (id_diseases,id_pubmed) doesnt exist 
				PreparedStatement insertLinking = conn.prepareStatement(SQL_INSERT_PUBMED_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
				insertLinking.setInt(1, diseaseID);
				insertLinking.setInt(2, id);
				insertLinking.setInt(3, occurrences);
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
					PreparedStatement updateOccurrences = conn.prepareStatement(SQL_UPDATE_PUBMED_OCCURRENCES);
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

	public int getDiseaseOccurrences(int diseaseID, int pubmedID) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_SELECT_DISEASE_OCCURRENCES_IN_PUBMED);
		statement.setInt(1, diseaseID);
		statement.setInt(2, pubmedID);
		int occurrences = -1;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next())
				occurrences = keys.getInt(1);
		}
		return occurrences;
	}
	
	public int getAllOccurrences(int pubmedID) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_SELECT_ALL_OCCURRENCES_IN_PUBMED);
		statement.setInt(1, pubmedID);
		int occurrences = -1;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next())
				occurrences = keys.getInt(1);
		}
		return occurrences;
	}
	
	public List<PubMed> getAllPubMed() throws SQLException {
		ArrayList<PubMed> pubmeds = new ArrayList<>();
		Statement stmt = conn.createStatement();
		try(ResultSet result = stmt.executeQuery(SQL_SELECT_ALL_PUBMEDS)){
			while(result.next()) {
				int id = result.getInt("id");
				int pubmedid = result.getInt("pubmedID");
				String title = result.getString("title");
				String abstrct = result.getString("abstract");
				pubmeds.add(new PubMed(id, pubmedid, title, abstrct));
			}
		}
		return pubmeds;
	}

	public List<Integer> getAllPubMedIds() throws SQLException {
		List<Integer> pubmeds = new ArrayList<>();
		Statement stmt = conn.createStatement();
		try(ResultSet result = stmt.executeQuery(SQL_SELECT_ALL_PUBMED_IDS)){
			while(result.next()) {
				int id = result.getInt("id");
				pubmeds.add(id);
			}
		}
		return pubmeds;
	}
	
	public boolean updatePubMedRank(int diseaseId, int pubmedId, double ranking) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_UPDATE_PUBMED_RANK);
		statement.setDouble(1, ranking);
		statement.setInt(2, diseaseId);
		statement.setInt(3, pubmedId);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Updating entry failed, no rows affected.");
		}
		return true;
	}
	
	public List<String> getRelatedDiseases(int id) throws SQLException {
		ArrayList<String> diseases = new ArrayList<>();
		PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_PUBMED_RELATED_DISEASES);
		stmt.setInt(1, id);
		try(ResultSet result = stmt.executeQuery()){
			while(result.next()) {
				String name = result.getString("name");
				diseases.add(name);
			}
		}
		return diseases;
	}

	public List<Integer> getRelatedDiseaseIds(int pubMedId) throws SQLException {
		List<Integer> diseases = new ArrayList<>();
		PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_PUBMED_RELATED_DISEASE_IDS);
		stmt.setInt(1, pubMedId);
		try(ResultSet result = stmt.executeQuery()){
			while(result.next()) {
				int id = result.getInt("id_diseases");
				diseases.add(id);
			}
		}
		return diseases;
	}
	
	public double getTf(int diseaseID, int pubmedID) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_SELECT_DISEASE_PUBMED_TF, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, diseaseID);
		statement.setInt(2, pubmedID);
		double tf = -1;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next())
				tf = keys.getInt(1);
		}
		return tf;
	}
	
	public boolean updateDiseasePubmedTf(int diseaseID, int pubmedID, double tf) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_UPDATE_DISEASE_PUBMED_TF);
		statement.setDouble(1, tf);
		statement.setInt(2, diseaseID);
		statement.setInt(3, pubmedID);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Updating entry failed, no rows affected.");
		}
		return true;
	}
	
	public double getIdf(int diseaseID) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_SELECT_DISEASE_IDF, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, diseaseID);
		double idf = -1;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next())
				idf = keys.getInt(1);
		}
		return idf;
	}
	
	public boolean updateDiseaseIdf(int diseaseID, double idf) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_UPDATE_DISEASE_IDF);
		statement.setDouble(1, idf);
		statement.setInt(2, diseaseID);	

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Updating entry failed, no rows affected.");
		}
		return true;
	}

	
	///////////////////////////////////////  TWEETS //////////////////////////////////////////////////
	public List<Tweet> getAllTweets() throws SQLException{
		ArrayList<Tweet> tweets = new ArrayList<>();
		Statement stmt = conn.createStatement();
		try(ResultSet result = stmt.executeQuery(SQL_SELECT_ALL_TWEETS)){
			while(result.next()) {
				int id = result.getInt("id");
				String pubmedid = result.getString("url");
				String title = result.getString("text");
				tweets.add(new Tweet(id, pubmedid, title));
			}
		}
		return tweets;
	}

	public Tweet createTweet(Disease disease, long tweetId, String text, int originalDiseaseID, Date date) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_INSERT_TWEET, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, String.valueOf(tweetId));
		statement.setString(2, text);
		statement.setDate(3, date);
		statement.setInt(4, originalDiseaseID);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Creating entry failed, no rows affected.");
		}

		try (ResultSet keys = statement.getGeneratedKeys()) {
			if (keys.next()) {
				int id = keys.getInt(1);
				addDiseaseTweetLink(disease.getId(), id);
				return new Tweet(id, String.valueOf(id), text);
			}
		}
		throw new SQLException("Retrieving generated id failed.");
	}

	public void addDiseaseTweetLink(int diseaseId, int id) throws SQLException {
		PreparedStatement stmt = conn.prepareStatement(SQL_SELECT_PAIR_DISEASEID_TWEETID, Statement.RETURN_GENERATED_KEYS);
		stmt.setInt(1, diseaseId);
		stmt.setInt(2, id);
		boolean exists = false;
		try(ResultSet keys = stmt.executeQuery()){
			exists = keys.next();

			if(!exists) {
				// Add entry in linking table.
				stmt = conn.prepareStatement(SQL_INSERT_TWEET_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
				stmt.setInt(1, diseaseId);
				stmt.setInt(2, id);

				int affected = stmt.executeUpdate();
				if (affected == 0) {
					throw new SQLException("Creating entry failed, no rows affected.");
				}
			} else {
				/*int id_disease = keys.getInt("id_diseases");
				int id_tweet = keys.getInt("id_tweets");
				int oldOccurrences = keys.getInt("occurrences");
				if(occurrences > oldOccurrences) {
					//to prevent setting occurrences to 1 when it was already >1
					//for 'new' diseases
					PreparedStatement updateOccurrences = conn.prepareStatement(SQL_UPDATE_TWEET_OCCURRENCES);
					updateOccurrences.setInt(1, occurrences);
					updateOccurrences.setInt(2, id_disease);
					updateOccurrences.setInt(3, id_pubmed);
					int affected = updateOccurrences.executeUpdate();
					if (affected == 0) {
						throw new SQLException("Creating entry failed, no rows affected.");
					}
				}*/
			}
		}
	}
	
	public boolean updateTweetRank(int diseaseId, int tweetId, double ranking) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_UPDATE_TWEET_RANK);
		statement.setDouble(1, ranking);
		statement.setInt(2, diseaseId);
		statement.setInt(3, tweetId);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Updating entry failed, no rows affected.");
		}
		return true;
	}
	
	///////////////////////////////////////  IMAGES //////////////////////////////////////////////////
	public Image createImage(Disease disease, String url) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_INSERT_IMAGE, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, url);

		int affected = statement.executeUpdate();
		if (affected == 0) {
			throw new SQLException("Creating entry failed, no rows affected.");
		}

		try (ResultSet keys = statement.getGeneratedKeys()) {
			if (keys.next()) {
				int id = keys.getInt(1);
				// Add entry in linking table.
				PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_IMAGE_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
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
	
	
	/////////////////////////////////////////////FEEDBACK//////////////////////////////////////////////
	public Feedback getPubMedMaxFeedback(int diseaseID) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_GET_PUBMED_DISEASE_MAX_FEEDBACK, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, diseaseID);
		int implicit = 0,
		    explicit = 0;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next()) {
				implicit = keys.getInt(1);
				explicit = keys.getInt(2);
			}
				
		}
		return new Feedback(implicit,explicit);
	}
	
	public Feedback getTweetsMaxFeedback(int diseaseID) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_GET_TWEET_DISEASE_MAX_FEEDBACK, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, diseaseID);
		int implicit = 0,
		    explicit = 0;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next()) {
				implicit = keys.getInt(1);
				explicit = keys.getInt(2);
			}
				
		}
		return new Feedback(implicit,explicit);
	}
	
	public Feedback getImageMaxFeedback(int diseaseID) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_GET_IMAGE_DISEASE_MAX_FEEDBACK, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, diseaseID);
		int implicit = 0,
		    explicit = 0;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next()) {
				implicit = keys.getInt(1);
				explicit = keys.getInt(2);
			}
				
		}
		return new Feedback(implicit,explicit);
	}

	

	

	


	


	

	

	


}
