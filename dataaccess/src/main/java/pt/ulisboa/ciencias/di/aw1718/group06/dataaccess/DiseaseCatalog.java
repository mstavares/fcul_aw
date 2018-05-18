package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess;

import com.mysql.cj.jdbc.MysqlDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.dto.*;
import pt.ulisboa.ciencias.di.aw1718.group06.dataaccess.models.*;
import javafx.util.Pair;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DiseaseCatalog {

	private static final Logger LOG = LoggerFactory.getLogger(DiseaseCatalog.class);
	private Connection connection;

	/*  DISEASES  */
	private static final String SQL_SELECT_ALL_DISEASES = "SELECT * FROM diseases";
	private static final String SQL_SELECT_SINGLE_DISEASE_BY_NAME = "SELECT * FROM diseases WHERE id = ? OR name = ?";
	private static final String SQL_SELECT_FRAGMENT_DISEASES = "SELECT * FROM diseases WHERE name LIKE ?";
	private static final String SQL_SELECT_ID_BY_NAME = "SELECT id FROM diseases WHERE name=?";
	private static final String SQL_INSERT_DISEASE = "INSERT INTO diseases (doid, name, abstract, was_derived_from, field, death_cause_of) VALUES (?, ?, ?, ?, ?)";
	private static final String SQL_COUNT_DISEASES = "SELECT COUNT(*) FROM diseases";
	private static final String SQL_SELECT_DISEASE_IDF = "SELECT idf FROM diseases WHERE id = ?";
	private static final String SQL_SELECT_ALL_IDFS = "SELECT id, idf FROM diseases";
	private static final String SQL_UPDATE_DISEASE_IDF = "UPDATE diseases SET idf = ? WHERE id = ?";
	private static final String SQL_SELECT_RELATED_DISEASES = "SELECT id, name FROM diseases WHERE field = (SELECT field FROM diseases WHERE name = ?)";

	/*  TWEETS  */
    private static final String SQL_COUNT_TWEETS = "SELECT COUNT(*) FROM tweets";
    private static final String SQL_INSERT_TWEET = "INSERT INTO tweets (url, text, pub_date, id_original_disease) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL_TWEETS = "SELECT * FROM tweets";
    private static final String SQL_SELECT_TWEETS_BY_DISEASE_ID = "SELECT * FROM tweets, diseases_tweets WHERE diseases_tweets.id_diseases = ? AND tweets.id = diseases_tweets.id_tweets;";
    private static final String SQL_SELECT_TWEETS_BY_DISEASE_ID_SORTED = "SELECT * FROM tweets, diseases_tweets WHERE diseases_tweets.id_diseases = ? AND tweets.id = diseases_tweets.id_tweets ORDER BY diseases_tweets.explicit_feedback DESC";
    private static final String SQL_INSERT_TWEET_DISEASE_LINKING = "INSERT INTO diseases_tweets (id_diseases, id_tweets) VALUES (?, ?)";

	/*  PUBMED  */
    private static final String SQL_COUNT_PUBMEDS = "SELECT COUNT(*) FROM pubmed";
	private static final String SQL_SELECT_PUBMEDS_BY_DISEASE_ID = "SELECT * FROM pubmed, diseases_pubmed WHERE diseases_pubmed.id_diseases = ? AND pubmed.id = diseases_pubmed.id_pubmed;";
	private static final String SQL_GET_FULLPUBMED_BY_ID = "SELECT * FROM pubmed, diseases_pubmed WHERE pubmed.id = ? AND diseases_pubmed.id_diseases = ?";
	private static final String SQL_SELECT_ALL_PUBMEDS = "SELECT * FROM pubmed";
	private static final String SQL_SELECT_ALL_PUBMED_IDS = "SELECT id FROM pubmed";
	private static final String SQL_SELECT_ID_BY_PUBMEDID = "SELECT id FROM pubmed WHERE pubmedID = ?";
    private static final String SQL_SELECT_DATE_BY_PUBMEDID = "SELECT pub_date FROM pubmed WHERE pubmedID = ?";
	private static final String SQL_INSERT_PUBMED = "INSERT INTO pubmed (pubmedID, title, abstract, pub_date, id_original_disease) VALUES (?, ?, ?, ?, ?)";
	private static final String SQL_INSERT_PUBMED_DISEASE_LINKING = "INSERT INTO diseases_pubmed (id_diseases, id_pubmed, occurrences, places) VALUES (?, ?, ?, ?)";

	/*  IMAGES  */
    private static final String SQL_COUNT_IMAGES = "SELECT COUNT(*) FROM images";
	private static final String SQL_SELECT_IMAGES_BY_DISEASE_ID = "SELECT * FROM images, diseases_images WHERE diseases_images.id_diseases = ? AND images.id = diseases_images.id_images;";
	private static final String SQL_SELECT_IMAGES_BY_DISEASE_ID_SORTED = "SELECT * FROM images, diseases_images WHERE diseases_images.id_diseases = ? AND images.id = diseases_images.id_images ORDER BY diseases_images.explicit_feedback DESC;";
	private static final String SQL_INSERT_IMAGE = "INSERT INTO images (url) VALUES (?)";
	private static final String SQL_INSERT_IMAGE_DISEASE_LINKING = "INSERT INTO diseases_images (id_diseases, id_images) VALUES (?, ?)";

	/*  DISEASES_PUBMED  */
	private static final String SQL_SELECT_PUBMED_FEEDBACK = "SELECT * FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?;";
	private static final String SQL_UPDATE_PUBMED_FEEDBACK = "UPDATE diseases_pubmed SET implicit_feedback = ?, explicit_feedback = ? WHERE id_diseases = ? AND id_pubmed = ?;";
	private static final String SQL_SELECT_PAIR_DISEASEID_PUBMEDID = "SELECT * FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_SELECT_PUBMED_RELATED_DISEASES = "SELECT d.id, d.name, dp.places FROM diseases d, diseases_pubmed dp WHERE d.id=dp.id_diseases AND dp.id_pubmed = ?";
	private static final String SQL_SELECT_PUBMED_RELATED_DISEASE_IDS = "SELECT id_diseases FROM diseases_pubmed WHERE id_pubmed = ?";
	private static final String SQL_SELECT_DISEASE_OCCURRENCES_IN_PUBMED = "SELECT occurrences FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_SELECT_ALL_OCCURRENCES_IN_PUBMED = "SELECT SUM(occurrences) FROM diseases_pubmed WHERE id_pubmed = ?";
	private static final String SQL_UPDATE_PUBMED_OCCURRENCES = "UPDATE diseases_pubmed SET occurrences = ?, places = ? WHERE id_diseases = ? AND id_pubmed = ?";
    private static final String SQL_SELECT_DISEASE_PUBMED_TF = "SELECT tf FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
    private static final String SQL_UPDATE_DISEASE_PUBMED_TF = "UPDATE diseases_pubmed SET tf = ? WHERE id_diseases = ? AND id_pubmed = ?";
    private static final String SQL_SELECT_DISEASE_PLACES_ON_PUBMED = "SELECT places FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
    
    /*  DISEASES_TWEETS*/
    private static final String SQL_SELECT_PAIR_DISEASEID_TWEETID = "SELECT * FROM diseases_tweets WHERE id_diseases = ? AND id_tweets = ?";
    private static final String SQL_SELECT_TWEET_FEEDBACK = "SELECT * FROM diseases_tweets WHERE id_diseases = ? AND id_tweets = ?;";
    private static final String SQL_UPDATE_TWEET_FEEDBACK = "UPDATE diseases_tweets SET implicit_feedback = ?, explicit_feedback = ? WHERE id_diseases = ? AND id_tweets = ?;";

    /*  DISEASES_IMAGES */
    private static final String SQL_SELECT_IMAGE_FEEDBACK = "SELECT * FROM diseases_images WHERE id_diseases = ? AND id_images = ?;";
    private static final String SQL_UPDATE_IMAGE_FEEDBACK = "UPDATE diseases_images SET implicit_feedback = ?, explicit_feedback = ? WHERE id_diseases = ? AND id_images = ?;";

    /* FEEDBACK QUERIES */
    private static final String SQL_GET_PUBMED_DISEASE_FEEDBACK = "SELECT implicit_feedback, explicit_feedback FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	private static final String SQL_GET_PUBMED_DISEASE_MAX_FEEDBACK = "SELECT MAX(implicit_feedback), MAX(explicit_feedback) FROM diseases_pubmed WHERE id_diseases = ?";
	private static final String SQL_GET_TWEET_DISEASE_MAX_FEEDBACK = "SELECT MAX(implicit_feedback), MAX(explicit_feedback) FROM diseases_tweets WHERE id_diseases = ?";
	private static final String SQL_GET_IMAGE_DISEASE_MAX_FEEDBACK = "SELECT MAX(implicit_feedback), MAX(explicit_feedback) FROM diseases_images WHERE id_diseases = ?";
	
	private static final String SQL_GET_AVG_PUBMEDS_BY_DISEASE = "SELECT avg(a.num) as average FROM (SELECT diseases.id, COUNT(diseases_pubmed.id_pubmed) as num FROM diseases_pubmed, diseases WHERE diseases_pubmed.id_diseases = diseases.id GROUP BY diseases.id) as a";
	private static final String SQL_GET_AVG_IMAGES_BY_DISEASE = "SELECT avg(a.num) as average FROM (SELECT diseases.id, COUNT(diseases_tweets.id_tweets) as num FROM diseases_tweets, diseases WHERE diseases_tweets.id_diseases = diseases.id GROUP BY diseases.id) as a";
	private static final String SQL_GET_AVG_TWEETS_BY_DISEASE = "SELECT avg(a.num) as average FROM (SELECT diseases.id, COUNT(diseases_images.id_images) as num FROM diseases_images, diseases WHERE diseases_images.id_diseases = diseases.id GROUP BY diseases.id) as a";

    public DiseaseCatalog(Connection connection) {
        this.connection = connection;
    }

	///////////////////////////////////////  DISEASES //////////////////////////////////////////////////
	public Disease addDisease(String doid, String name, String description, String derivedFrom, String field, String dead) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_INSERT_DISEASE, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, doid);
		statement.setString(2, name);
		statement.setString(3, description);
		statement.setString(4, derivedFrom);
		statement.setString(5, field);
		statement.setString(6, dead);

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
				return new Disease(id, name, description, derivedFrom, field, dead);
			}
		}
		throw new SQLException("Retrieving generated id failed.");

	}

	public List<Disease> getDiseases(int limit) throws SQLException {
		ArrayList<Disease> results = new ArrayList<>();
		Statement stmt = connection.createStatement();
		String query = SQL_SELECT_ALL_DISEASES;

        if (limit > 0) {
            query += " LIMIT " + limit;
        }

		try (ResultSet result = stmt.executeQuery(query)) {
			while (result.next()) {
				int id = result.getInt("id");
				String name = result.getString("name");
				String description = result.getString("abstract");
				String derived = result.getString("was_derived_from");
				String field = result.getString("field");
				String dead = result.getString("death_cause_of");
				results.add(new Disease(id, name, description, derived, field, dead));
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
				String field = result.getString("field");
				String dead = result.getString("death_cause_of");
				results.add(new Disease(id, name, description, derived, field, dead));
			}
		}
		return results;
	}

	/*
	public List<Pair<Integer, IndexRank>> getRankedPubMeds(int diseaseId) throws SQLException {
        CompoundRanker ranker = new CompoundRanker(ImmutableMap.of(
                RankType.TF_IDF_RANK, 0.3,
                RankType.DATE_RANK, 0.1,
                RankType.EXPLICIT_FEEDBACK_RANK, 0.4,
                RankType.IMPLICIT_FEEDBACK_RANK, 0.2
        ));

        Index index = new Index(ranker, this);
        index.build();

        return index.getArticlesFor(diseaseId);
    }
    */

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
				String field = result.getString("field");
				String dead = result.getString("death_cause_of");
				disease = new Disease(id, name, description, derived, field, dead);
			}
		}
		return disease;
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

    public int getNumDiseases() throws SQLException {
        int count = 0;
        Statement statement = connection.createStatement();
        try(ResultSet result = statement.executeQuery(SQL_COUNT_DISEASES)){
            if(result.next())
                count = result.getInt(1);
        }
        return count;
    }
    
    public List<Pair<Integer, String>> getRelatedDiseases(String diseaseName) throws SQLException{
		ArrayList<Pair<Integer, String>> diseases = new ArrayList<>();
		PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_RELATED_DISEASES);
		stmt.setString(1, diseaseName);
		try(ResultSet result = stmt.executeQuery()){
			while(result.next()) {
				String name = result.getString("name");
				int id = result.getInt("id");
				diseases.add(new Pair(id, name));
			}
		}
		return diseases;
	}

	///////////////////////////////////////  PUBMED //////////////////////////////////////////////////
	public PubMed addPubMedInfo(int diseaseID, int pubmedID, String title, String abstrct, Date date) throws SQLException {

		PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ID_BY_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
		statement.setString(1, String.valueOf(pubmedID));
		try(ResultSet keys = statement.executeQuery()){
			boolean exists = keys.next();

			if(exists) {
				//pubmed article already exists in table pubmed
				//add linking to this disease
				int id = keys.getInt("id");
				addPubMedDiseaseLink(diseaseID, id, 1, "");
				return new PubMed(id, pubmedID, title, abstrct);
			} else {
				statement = connection.prepareStatement(SQL_INSERT_PUBMED, Statement.RETURN_GENERATED_KEYS);
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
						addPubMedDiseaseLink(diseaseID, id, 1, "");
						return new PubMed(id, pubmedID, title, abstrct);
					}
				}
				throw new SQLException("Retrieving generated id failed.");
			}
		}  	
	} 

	public void addPubMedDiseaseLink(int diseaseID, int id, int occurrences, String places) throws SQLException {

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
				insertLinking.setInt(3, occurrences);
				insertLinking.setString(4, places);
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
					updateOccurrences.setString(2, places);
					updateOccurrences.setInt(3, id_disease);
					updateOccurrences.setInt(4, id_pubmed);
					
					int affected = updateOccurrences.executeUpdate();
					if (affected == 0) {
						throw new SQLException("Creating entry failed, no rows affected.");
					}
				}
			}	
		}
	}

    public int getDiseaseOccurrences(int diseaseID, int pubmedID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_DISEASE_OCCURRENCES_IN_PUBMED);
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
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL_OCCURRENCES_IN_PUBMED);
        statement.setInt(1, pubmedID);
        int occurrences = -1;
        try(ResultSet keys = statement.executeQuery()){
            if(keys.next()) {
                occurrences = keys.getInt(1);
            }
        }
        return occurrences;
    }

	public List<PubMed> getAllPubMed() throws SQLException {
		ArrayList<PubMed> pubmeds = new ArrayList<>();
		Statement stmt = connection.createStatement();
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

	public List<FullPubMed> getFullPubmedsByDiseaseId(int diseaseId, int start, int limit) throws SQLException {
		ArrayList<FullPubMed> results = new ArrayList<>();
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_PUBMEDS_BY_DISEASE_ID);
		preparedStatement.setInt(1, diseaseId);
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
				
				results.add(new FullPubMed(id, pubMedId, title, description, idOriginalDisease, implicitFeedback, explicitFeedback, getMentionedDiseases(id)));
			}
		}
		return results;
	}

    public List<Integer> getAllPubMedIds() throws SQLException {
        List<Integer> pubmeds = new ArrayList<>();
        Statement stmt = connection.createStatement();
        try(ResultSet result = stmt.executeQuery(SQL_SELECT_ALL_PUBMED_IDS)){
            while(result.next()) {
                int id = result.getInt("id");
                pubmeds.add(id);
            }
        }
        return pubmeds;
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

    public List<Integer> getRelatedDiseaseIds(int pubMedId) throws SQLException {
        List<Integer> diseases = new ArrayList<>();
        PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_PUBMED_RELATED_DISEASE_IDS);
        stmt.setInt(1, pubMedId);
        try (ResultSet result = stmt.executeQuery()) {
            while (result.next()) {
                int id = result.getInt("id_diseases");
                diseases.add(id);
            }
        }
        return diseases;
    }

    public Date getPubMedDate(int pubmedId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_DATE_BY_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, pubmedId);
        Date date = new Date(0);
        try (ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                date = result.getDate(1);
            }
        }
        return date;
    }

    public double getTf(int diseaseID, int pubmedID) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_DISEASE_PUBMED_TF, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, diseaseID);
        statement.setInt(2, pubmedID);
        double tf = -1;
        try (ResultSet keys = statement.executeQuery()) {
            if (keys.next()) {
                tf = keys.getInt(1);
            }
        }
        return tf;
    }

    public boolean updateDiseasePubmedTf(int diseaseID, int pubmedID, double tf) throws SQLException {
        return updateTf(diseaseID, pubmedID, tf, SQL_UPDATE_DISEASE_PUBMED_TF);
    }

    private boolean updateTf(int diseaseID, int pubmedID, double tf, String sqlUpdateTf) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlUpdateTf);
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
		PreparedStatement statement = connection.prepareStatement(SQL_SELECT_DISEASE_IDF, Statement.RETURN_GENERATED_KEYS);
		statement.setInt(1, diseaseID);
		double idf = -1;
		try(ResultSet keys = statement.executeQuery()){
			if(keys.next()) {
			    idf = keys.getInt(1);
            }
		}
		return idf;
	}

	public Map<Integer, Double> getAllIdfs() throws SQLException {
        Map<Integer, Double> dToIdf = new HashMap<>();
        PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL_IDFS, Statement.RETURN_GENERATED_KEYS);
		try(ResultSet result = statement.executeQuery()){
			while (result.next()) {
                dToIdf.put(result.getInt("id"), result.getDouble("idf"));
            }
		}
		return dToIdf;
	}

	public boolean updateDiseaseIdf(int diseaseID, double idf) throws SQLException {
		PreparedStatement statement = connection.prepareStatement(SQL_UPDATE_DISEASE_IDF);
		statement.setDouble(1, idf);
		statement.setInt(2, diseaseID);

        int affected = statement.executeUpdate();
        if (affected == 0) {
            throw new SQLException("Updating entry failed, no rows affected.");
        }
        return true;
    }


    private FullFeedback getFeedback(int diseaseId, int objectId, String query) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, diseaseId);
        preparedStatement.setInt(2, objectId);
        try (ResultSet result = preparedStatement.executeQuery()) {
            if (result.next()) {
                int implicitFeedback = result.getInt("implicit_feedback");
                int explicitFeedback = result.getInt("explicit_feedback");
                return new FullFeedback(diseaseId, objectId, implicitFeedback, explicitFeedback);
            }
        }
        throw new SQLException("Instance not found");
    }

    private boolean updateFeedback(FullFeedback fullFeedback, String query) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, fullFeedback.getImplicitFeedback());
        statement.setInt(2, fullFeedback.getExplicitFeedback());
        statement.setInt(3, fullFeedback.getDiseaseId());
        statement.setInt(4, fullFeedback.getObjectId());
        return statement.executeUpdate() != 0;
    }

    private void performFeedbackUpdate(FullFeedback fullFeedback, FullFeedback.Operations operation) {
        if(FullFeedback.Operations.INCREMENT_IMPLICIT == operation) {
            fullFeedback.incrementImplicitFeedback();
        } else if(FullFeedback.Operations.INCREMENT_EXPLICIT == operation) {
            fullFeedback.incrementExplicitFeedback();
        } else if(FullFeedback.Operations.DECREMENT_IMPLICIT == operation) {
            fullFeedback.decrementImplicitFeedback();
        } else {
            fullFeedback.decrementExplicitFeedback();
        }
    }

    public Statistic getStatistic() throws SQLException {
        int numberOfDiseases = getNumberOf(SQL_COUNT_DISEASES);
        int numberOfPubMeds = getNumberOf(SQL_COUNT_PUBMEDS);
        int numberOfTweets = getNumberOf(SQL_COUNT_TWEETS);
        int numberOfImages = getNumberOf(SQL_COUNT_IMAGES);
        double avgPubmeds = getAvgPubmedsByDisease();
        double avgTweets = getAvgTweetsByDisease();
        double avgImages = getAvgImagesByDisease();
        System.out.println("avgPubmeds= " + avgPubmeds + "===========================================================================");
        return new Statistic(numberOfDiseases, numberOfPubMeds, numberOfTweets, numberOfImages, avgPubmeds, avgTweets, avgImages);
    }

    private double getAvgImagesByDisease() throws SQLException {
    	double avg = -1;
        Statement statement = connection.createStatement();
        try(ResultSet result = statement.executeQuery(SQL_GET_AVG_IMAGES_BY_DISEASE)){
            if(result.next())
                avg = result.getDouble(1);
        }
        return avg;
	}

	private double getAvgTweetsByDisease() throws SQLException {
		double avg = -1;
        Statement statement = connection.createStatement();
        try(ResultSet result = statement.executeQuery(SQL_GET_AVG_TWEETS_BY_DISEASE)){
            if(result.next())
                avg = result.getDouble(1);
        }
        return avg;
	}

	private double getAvgPubmedsByDisease() throws SQLException {
		double avg = -1;
        Statement statement = connection.createStatement();
        try(ResultSet result = statement.executeQuery(SQL_GET_AVG_PUBMEDS_BY_DISEASE)){
            if(result.next())
                avg = result.getDouble(1);
        }
        return avg;
	}

	public int getNumberOf(String countQuery) throws SQLException {
        int count = 0;
        Statement statement = connection.createStatement();
        try(ResultSet result = statement.executeQuery(countQuery)){
            if(result.next())
                count = result.getInt(1);
        }
        return count;
    }

    public boolean updatePubMedFeedback(int diseaseId, int pubMedId, FullFeedback.Operations operation) throws SQLException {
        FullFeedback feedback = getFeedback(diseaseId, pubMedId, SQL_SELECT_PUBMED_FEEDBACK);
        performFeedbackUpdate(feedback, operation);
        return updateFeedback(feedback, SQL_UPDATE_PUBMED_FEEDBACK);
    }

    public boolean updateTweetFeedback(int diseaseId, int tweetId, FullFeedback.Operations operation) throws SQLException {
        FullFeedback feedback = getFeedback(diseaseId, tweetId, SQL_SELECT_TWEET_FEEDBACK);
        performFeedbackUpdate(feedback, operation);
        return updateFeedback(feedback, SQL_UPDATE_TWEET_FEEDBACK);
    }

    public boolean updateImageFeedback(int diseaseId, int imageId, FullFeedback.Operations operation) throws SQLException {
        FullFeedback feedback = getFeedback(diseaseId, imageId, SQL_SELECT_IMAGE_FEEDBACK);
        performFeedbackUpdate(feedback, operation);
        return updateFeedback(feedback, SQL_UPDATE_IMAGE_FEEDBACK);
    }
    
    public String getDiseaseOccurrencesPlaces(int diseaseId, int idPubmed) throws SQLException{
  		String places = null;
  		PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_DISEASE_PLACES_ON_PUBMED);
  		stmt.setInt(1, diseaseId);
  		stmt.setInt(2, idPubmed);
  		try(ResultSet result = stmt.executeQuery()){
  			while(result.next()) {
  				places = result.getString("places");
  			}
  		}
  		return places;
  	}
    
    public List<MentionedDiseasesDAO> getMentionedDiseases(int pubmedId) throws SQLException{
    	ArrayList<MentionedDiseasesDAO> diseases = new ArrayList<>();
        PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_PUBMED_RELATED_DISEASES);
        stmt.setInt(1, pubmedId);
        try(ResultSet result = stmt.executeQuery()){
            while(result.next()) {
            	int id = result.getInt("id");
                String name = result.getString("name");
                String places = result.getString("places");
                diseases.add(new MentionedDiseasesDAO(id, name, places));
            }
        }
        return diseases;
    }


    ///////////////////////////////////////  TWEETS //////////////////////////////////////////////////
    public List<Tweet> getAllTweets() throws SQLException{
        ArrayList<Tweet> tweets = new ArrayList<>();
        Statement stmt = connection.createStatement();
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
        PreparedStatement statement = connection.prepareStatement(SQL_INSERT_TWEET, Statement.RETURN_GENERATED_KEYS);
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
        PreparedStatement stmt = connection.prepareStatement(SQL_SELECT_PAIR_DISEASEID_TWEETID, Statement.RETURN_GENERATED_KEYS);
        stmt.setInt(1, diseaseId);
        stmt.setInt(2, id);
        boolean exists = false;
        try(ResultSet keys = stmt.executeQuery()){
            exists = keys.next();
            if(!exists) {
                // Add entry in linking table.
                stmt = connection.prepareStatement(SQL_INSERT_TWEET_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, diseaseId);
                stmt.setInt(2, id);
                
                int affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Creating entry failed, no rows affected.");
                }
            } 
        }
    }

    public List<FullTweet> getFullTweetsByDiseaseId(int diseaseId, int start, int limit) throws SQLException {
        ArrayList<FullTweet> results = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_TWEETS_BY_DISEASE_ID);
        preparedStatement.setInt(1, diseaseId);
        try (ResultSet result = preparedStatement.executeQuery()) {
            while (result.next()) {
                int id = result.getInt("id");
                String url = result.getString("url");
                String text = result.getString("text");
                Date pubDate = result.getDate("pub_date");
                int idOriginalDisease = result.getInt("id_original_disease");
                int implicitFeedback = result.getInt("implicit_feedback");
                int explicitFeedback = result.getInt("explicit_feedback");
                results.add(new FullTweet(id, url, text, pubDate, idOriginalDisease, implicitFeedback, explicitFeedback));
            }
        }
        return results;
    }

    ///////////////////////////////////////  IMAGES //////////////////////////////////////////////////
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

    public List<FullImage> getFullImagesByDiseaseId(int diseaseId, int start, int limit) throws SQLException {
        ArrayList<FullImage> results = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_IMAGES_BY_DISEASE_ID);
        preparedStatement.setInt(1, diseaseId);
        try (ResultSet result = preparedStatement.executeQuery()) {
            while (result.next()) {
                int id = result.getInt("id");
                String url = result.getString("url");
                int implicitFeedback = result.getInt("implicit_feedback");
                int explicitFeedback = result.getInt("explicit_feedback");
                results.add(new FullImage(id, url, implicitFeedback, explicitFeedback));
            }
        }
        return results;
    }

    /////////////////////////////////////////////FEEDBACK//////////////////////////////////////////////
    public Feedback getPubMedMaxFeedback(int diseaseID) throws SQLException {
        return getMaxFeedbackFor(diseaseID, SQL_GET_PUBMED_DISEASE_MAX_FEEDBACK);
    }

    public Feedback getTweetsMaxFeedback(int diseaseID) throws SQLException {
        return getMaxFeedbackFor(diseaseID, SQL_GET_TWEET_DISEASE_MAX_FEEDBACK);
    }

    public Feedback getImageMaxFeedback(int diseaseID) throws SQLException {
        return getMaxFeedbackFor(diseaseID, SQL_GET_IMAGE_DISEASE_MAX_FEEDBACK);
    }

    private Feedback getMaxFeedbackFor(int diseaseID, String sqlGetMaxFeedback) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sqlGetMaxFeedback, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, diseaseID);
        int implicit = 0;
        int explicit = 0;
        try (ResultSet keys = statement.executeQuery()) {
            if (keys.next()) {
                implicit = keys.getInt(1);
                explicit = keys.getInt(2);
            }

        }
        return new Feedback(implicit, explicit);
    }

    public Feedback getDiseasePubMedFeedback(int diseaseId, int pubmedId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(SQL_GET_PUBMED_DISEASE_FEEDBACK, Statement.RETURN_GENERATED_KEYS);
        statement.setInt(1, diseaseId);
        statement.setInt(2, pubmedId);
        int implicit = 0;
        int explicit = 0;
        try (ResultSet keys = statement.executeQuery()) {
            if (keys.next()) {
                implicit = keys.getInt(1);
                explicit = keys.getInt(2);
            }

        }
        return new Feedback(implicit, explicit);
    }

	public FullPubMed getFullPubmedByPubID(int id, int id_disease) throws SQLException {
		FullPubMed pub = null;
		PreparedStatement preparedStatement = connection.prepareStatement(SQL_GET_FULLPUBMED_BY_ID);
		preparedStatement.setInt(1, id);
		preparedStatement.setInt(2, id_disease);
		try (ResultSet result = preparedStatement.executeQuery()) {
			while (result.next()) {
				
				int pubMedId = result.getInt("pubmedID");
				String title = result.getString("title");
				String description = result.getString("abstract");
				int idOriginalDisease = result.getInt("id_original_disease");
				int implicitFeedback = result.getInt("implicit_feedback");
				int explicitFeedback = result.getInt("explicit_feedback");
				
				pub = new FullPubMed(id, pubMedId, title, description, idOriginalDisease, implicitFeedback, explicitFeedback, getMentionedDiseases(id));
			}
		}
		return pub;
	}

	public List<FullTweet> getOrderedTweets(int diseaseId) throws SQLException {
		ArrayList<FullTweet> results = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_TWEETS_BY_DISEASE_ID_SORTED);
        preparedStatement.setInt(1, diseaseId);
        try (ResultSet result = preparedStatement.executeQuery()) {
            while (result.next()) {
                int id = result.getInt("id");
                String url = result.getString("url");
                String text = result.getString("text");
                Date pubDate = result.getDate("pub_date");
                int idOriginalDisease = result.getInt("id_original_disease");
                int implicitFeedback = result.getInt("implicit_feedback");
                int explicitFeedback = result.getInt("explicit_feedback");
                results.add(new FullTweet(id, url, text, pubDate, idOriginalDisease, implicitFeedback, explicitFeedback));
            }
        }
        return results;
	}

	public List<FullImage> getOrderedImages(int diseaseId) throws SQLException {
		ArrayList<FullImage> results = new ArrayList<>();
        PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_IMAGES_BY_DISEASE_ID_SORTED);
        preparedStatement.setInt(1, diseaseId);
        try (ResultSet result = preparedStatement.executeQuery()) {
            while (result.next()) {
                int id = result.getInt("id");
                String url = result.getString("url");
                int implicitFeedback = result.getInt("implicit_feedback");
                int explicitFeedback = result.getInt("explicit_feedback");
                results.add(new FullImage(id, url, implicitFeedback, explicitFeedback));
            }
        }
        return results;
	}
	
	
}
