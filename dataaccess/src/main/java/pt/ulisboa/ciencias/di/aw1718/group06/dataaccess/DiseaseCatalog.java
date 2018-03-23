package pt.ulisboa.ciencias.di.aw1718.group06.dataaccess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DiseaseCatalog {

	private Connection conn;
	
    private static final String SQL_INSERT_DISEASE = "INSERT INTO diseases (name, abstract, was_derived_from) VALUES (?, ?, ?)";
    private static final String SQL_SELECT_ALL_DISEASES = "SELECT * FROM diseases";
    private static final String SQL_SELECT_SINGLE_DISEASE = "SELECT * FROM diseases WHERE name = ?";
    private static final String SQL_INSERT_TWEET = "INSERT INTO tweets (url, text) VALUES (?, ?)";
    private static final String SQL_INSERT_TWEET_DISEASE_LINKING = "INSERT INTO diseases_tweets (id_diseases, id_tweets) VALUES (?, ?)";

	private static final String SQL_INSERT_PUBMED = "INSERT INTO pubmed (pubmedID, title, abstract) VALUES (?, ?, ?)";
	private static final String SQL_INSERT_PUBMED_DISEASE_LINKING = "INSERT INTO diseases_pubmed (id_diseases, id_pubmed) VALUES (?, ?)";

    private static final String SQL_INSERT_IMAGE = "INSERT INTO images (url) VALUES (?)";
    private static final String SQL_INSERT_IMAGE_DISEASE_LINKING = "INSERT INTO diseases_images (id_diseases, id_images) VALUES (?, ?)";

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
	
	
    public Tweet createTweet(Disease disease, long tweetId, String text) throws SQLException {
        PreparedStatement statement = conn.prepareStatement(SQL_INSERT_TWEET, Statement.RETURN_GENERATED_KEYS);
        // TODO: change from URL to twitterID.
        statement.setString(1, String.valueOf(tweetId));
        statement.setString(2, text);

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

                affected = stmt.executeUpdate();
                if (affected == 0) {
                    throw new SQLException("Creating entry failed, no rows affected.");
                }
                return new Tweet(id, String.valueOf(id), text);
            }
        }
        throw new SQLException("Retrieving generated id failed.");
    }

    
    public PubMed addPubMedInfo(int diseaseID, int pubmedID, String title, String abstrct) throws SQLException {
    	PreparedStatement statement = conn.prepareStatement(SQL_INSERT_PUBMED, Statement.RETURN_GENERATED_KEYS);
    	statement.setString(1, String.valueOf(pubmedID));
    	statement.setString(2, title);
    	statement.setString(3,  abstrct);

    	int affected = statement.executeUpdate();
    	if (affected == 0) {
    		throw new SQLException("Creating entry failed, no rows affected.");
    	}

    	try (ResultSet keys = statement.getGeneratedKeys()) {
    		if (keys.next()) {
    			int id = keys.getInt(1);
    			// Add entry in linking table.
    			PreparedStatement stmt = conn.prepareStatement(SQL_INSERT_PUBMED_DISEASE_LINKING, Statement.RETURN_GENERATED_KEYS);
    			stmt.setInt(1, diseaseID);
    			stmt.setInt(2, id);

    			affected = stmt.executeUpdate();
    			if (affected == 0) {
    				throw new SQLException("Creating entry failed, no rows affected.");
    			}
    			return new PubMed(id, pubmedID, title, abstrct);
    		}
    	}
    	throw new SQLException("Retrieving generated id failed.");
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
}
