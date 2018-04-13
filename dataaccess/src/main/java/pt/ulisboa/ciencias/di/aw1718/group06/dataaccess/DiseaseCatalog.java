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
	
	private static final String SQL_SELECT_ALL_PUBMEDS = "SELECT * FROM pubmed";
	
    private static final String SQL_INSERT_DISEASE = "INSERT INTO diseases (name, abstract, was_derived_from) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ALL_DISEASES = "SELECT * FROM diseases";
    private static final String SQL_SELECT_SINGLE_DISEASE = "SELECT * FROM diseases WHERE name = ?";
    private static final String SQL_INSERT_TWEET = "INSERT INTO tweets (url, text, pub_date) VALUES (?, ?, ?)";
    private static final String SQL_INSERT_TWEET_DISEASE_LINKING = "INSERT INTO diseases_tweets (id_diseases, id_tweets, id_original_disease) VALUES (?, ?, ?)";

	private static final String SQL_INSERT_PUBMED = "INSERT INTO pubmed (pubmedID, title, abstract, pub_date) VALUES (?, ?, ?, ?)";
	private static final String SQL_INSERT_PUBMED_DISEASE_LINKING = "INSERT INTO diseases_pubmed (id_diseases, id_pubmed, id_original_disease) VALUES (?, ?, ?)";
	//private static final String SQL_GET_PUBMED_COUNT_BY_PUBMEDID = "SELECT COUNT(*) FROM pubmed WHERE pubmedID = ?";
	private static final String SQL_GET_ID_PUBMED_BY_PUBMEDID = "SELECT id FROM pubmed WHERE pubmedID = ?";

    private static final String SQL_INSERT_IMAGE = "INSERT INTO images (url) VALUES (?)";
    private static final String SQL_INSERT_IMAGE_DISEASE_LINKING = "INSERT INTO diseases_images (id_diseases, id_images) VALUES (?, ?)";

	private static final String SQL_GET_ID_DISEASE_BY_NAME = "SELECT id FROM diseases WHERE name=?";

	private static final String SQL_GET_PAIR_DISEASEID_PUBMEDID = "SELECT * FROM diseases_pubmed WHERE id_diseases = ? AND id_pubmed = ?";
	
	private static final String SQL_UPDATE_PUBMED_RANK = "UPDATE diseases_pubmed SET rank = ? WHERE id_diseases = ? AND id_pubmed = ?";
	
	private static final String SQL_UPDATE_TWEET_RANK = "UPDATE diseases_tweets SET rank = ? WHERE id_diseases = ? AND id_pubmed = ?";
    
	public DiseaseCatalog(Connection connection) {
		this.conn = connection;
	}

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
        	throw new SQLException("Duplicated entry.");
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
	
    public Tweet createTweet(Disease disease, long tweetId, String text, int originalDiseaseID, Date date) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(SQL_INSERT_TWEET, Statement.RETURN_GENERATED_KEYS);
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
                PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_TWEET_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
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

    public PubMed addPubMedInfo(int diseaseID, int pubmedID, String title, String abstrct, Date date) throws SQLException {
    	
    	PreparedStatement statement = conn.prepareStatement(SQL_GET_ID_PUBMED_BY_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
    	statement.setString(1, String.valueOf(pubmedID));
    	try(ResultSet keys = statement.executeQuery()){
    		boolean exists = keys.next();
    		
    		if(exists) {
    			//pubmed article already exists in table pubmed
    			//add linking to this disease
    			int id = keys.getInt("id");
    			addPubMedDiseaseLink(diseaseID, id, diseaseID);
    			return new PubMed(id, pubmedID, title, abstrct);
    		} else {
    			statement = conn.prepareStatement(SQL_INSERT_PUBMED, Statement.RETURN_GENERATED_KEYS);
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
    	    			addPubMedDiseaseLink(diseaseID, id, diseaseID);
    	    			return new PubMed(id, pubmedID, title, abstrct);
    	    		}
    	    	}
    	    	throw new SQLException("Retrieving generated id failed.");
    		}
    	}  	
    } 
    
    public void addPubMedDiseaseLink(int diseaseID, int id, int originalDiseaseID) throws SQLException {
    	
    	PreparedStatement stmt = conn.prepareStatement(SQL_GET_PAIR_DISEASEID_PUBMEDID, Statement.RETURN_GENERATED_KEYS);
    	stmt.setInt(1, diseaseID);
    	stmt.setInt(2, id);
    	boolean exists = false;
    	try(ResultSet keys = stmt.executeQuery()){
    		exists = keys.next();
    	}
    	if(!exists) {
    		stmt = conn.prepareStatement(SQL_INSERT_PUBMED_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
    		stmt.setInt(1, diseaseID);
    		stmt.setInt(2, id);
    		stmt.setInt(3, originalDiseaseID);

    		int affected = stmt.executeUpdate();
    		if (affected == 0) {
    			throw new SQLException("Creating entry failed, no rows affected.");
    		}
    	}
	}

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

	public int getDiseaseID(String diseaseName) throws SQLException {
		PreparedStatement statement = conn.prepareStatement(SQL_GET_ID_DISEASE_BY_NAME, Statement.RETURN_GENERATED_KEYS);
    	statement.setString(1, diseaseName);
    	int id = -1;
    	try(ResultSet keys = statement.executeQuery()){
    		if(keys.next())
    			id = keys.getInt(1);
    	}
		return id;
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
}
