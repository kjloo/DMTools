package com.dmtools.apps;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class MarkovDataAdapter {

	// SQL connection variables
	private Connection conn;
	private String db;
	private String user;
	private String password;
	private String wordTable;
	private String markovTable;
	private boolean locked;
	
	public MarkovDataAdapter(String databaseName) {
		conn = null;
		db = "jdbc:sqlite:src/conf/" + databaseName + ".db";
		user = "SA";
		password = "";
		
		// Database Adapter lock
		locked = false;
		
		// Table Names
		this.wordTable = "WordIndex";
        this.markovTable = "MarkovChain";
	}
	
	private String formatWord(String word) {
		return "\"" + word + "\"";
	}
	
	private boolean getLocked() {
		return this.locked;
	}
	
	private boolean openConnection() {
		try {
		    // Create database connection
			if (this.conn == null || isConnectionOpen() == false) {
		        this.conn = DriverManager.getConnection(db, user, password);
			}
		}
		catch (SQLException e) {
		    System.err.println(e.getMessage());
		    return false;
		}
		return true;
	}
		
	private boolean closeConnection() {
		try {
			// Close connection
			if (this.conn != null) {
				this.conn.close();
				this.conn = null;
			}
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
			return false;
		}
		return true;
	}
	
	public void setAutoCommit(boolean autoCommit) {
		try {
			if (this.conn == null) {
				if (openConnection() == false) {
					return;
				}
			}
			this.conn.setAutoCommit(autoCommit);
			if (autoCommit == false) {
				// Need to keep connection open
				this.locked = true;
			}
			else {
				try {
				    this.conn.commit();
				    if (closeConnection()) {
				        this.locked = false;
				    }
				}
				catch (SQLException e) {
					this.conn.rollback();
				}
			}
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	public void dropTables() {
		// Create Word Indexing Table
		String sqlCreate1 = String.format("DROP TABLE IF EXISTS %s;", this.wordTable);
		// Create Probability Table
		String sqlCreate2 = String.format("DROP TABLE IF EXISTS %s;", this.markovTable);
		executeSql(new String[] {sqlCreate1, sqlCreate2});
	}
	
	public void createTables() {
		// Create Word Indexing Table
		String sqlCreate1 = "CREATE TABLE IF NOT EXISTS " + this.wordTable
				         + "(WordID INTEGER PRIMARY KEY AUTOINCREMENT,"
				         + "Word TEXT UNIQUE"
				         + ");";
		// Create Probability Table
		String sqlCreate2 = "CREATE TABLE IF NOT EXISTS " + this.markovTable
		         + "(CurrentWordID INTEGER NOT NULL, "
		         + "NextWordID INTEGER NOT NULL, "
		         + "Count FLOAT NOT NULL, "
		         + "FOREIGN KEY (CurrentWordID) REFERENCES WordIndex(WordID), "
		         + "FOREIGN KEY (NextWordID) REFERENCES WordIndex(WordID)"
		         + ");";
		executeSql(new String[] {sqlCreate1, sqlCreate2});
	}
	
	private boolean isConnectionOpen() {
		if (this.conn == null) {
			return false;
		}
		try {
			return this.conn.isClosed() == false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private void executeSql(String[] sqlCmds) {
		try {
			if (getLocked() == false) {
			    openConnection();
			}
		    Statement stmt = this.conn.createStatement();
		    for (int i = 0; i < sqlCmds.length; i++) {
			    //System.out.println(sqlCmds[i]);
		        stmt.execute(sqlCmds[i]);
		    }
		    stmt.close();
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		finally {
			if (getLocked() == false) {
			    closeConnection();
			}

		}
	}
	
	private void insertWordIndex(String word) {
		word = formatWord(word);
		// Insert Word into Word Indexing Table
		String sqlInsert = "INSERT INTO " + this.wordTable + "(Word) "
				+ "SELECT(" + word + ") "
				+ "WHERE NOT EXISTS (SELECT 1 FROM "
				+ this.wordTable + " WHERE Word = " + word + ");";
		executeSql(new String[] {sqlInsert});
	}
	
	private void insertMarkovChain(String context, Character nextChar, float count) {
		context = formatWord(context);
		String next = formatWord(String.valueOf(nextChar));
		// Insert Mapping into MarkovMap
		String sqlInsert = "INSERT INTO " + this.markovTable + " "
				+ "SELECT w1.WordID, w2.WordID, " + String.valueOf(count) + " FROM " + this.wordTable + " w1, " + this.wordTable + " w2 "
				+ "WHERE w1.Word = " + context + " AND w2.Word = " + next + ";";
		executeSql(new String[] {sqlInsert});
	}
	
	public void updateChain(String context, Character nextChar, float count) {
		// Try to insert word into word index table in case it hasn't been encountered yet
		insertWordIndex(context);
		insertWordIndex(String.valueOf(nextChar));
		
		// Add pair to MarkovChain
		insertMarkovChain(context, nextChar, count);
	}
	
	private ArrayList<MarkovProbability> selectSql(String sqlCmd) {
	    ArrayList<MarkovProbability> weights = new ArrayList<MarkovProbability>();
		try {
			openConnection();
			//System.out.println(sqlCmd);
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlCmd);
			while (rs.next()) {
				weights.add(new MarkovProbability(rs.getString("Prediction"), rs.getFloat("Value")));
			}
			stmt.close();
			rs.close();
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		finally {
			closeConnection();
		}
		return weights;
	}
	
	public String getNextChar(String context) {
		context = formatWord(context);
		// Select random entry from MarkovMap
		String sqlSelect = String.format("SELECT w2.Word as Prediction, Count as Value FROM %s "
				         + "INNER JOIN %s w1 on %s.CurrentWordID == w1.WordID "
				         + "INNER JOIN %s w2 on %s.NextWordID == w2.WordID "
				         + "WHERE w1.Word = %s", this.markovTable, this.wordTable, this.markovTable, this.wordTable, this.markovTable, context);
		ArrayList<MarkovProbability> weights = selectSql(sqlSelect);
		Float[] totals = new Float[weights.size()];
		Float accumulator = 0.0f;
		for (int i = 0; i < weights.size(); i++) {
			accumulator += weights.get(i).getValue();
			totals[i] = accumulator;
		}
		
		// Randomly generate a number and then find the corresponding index and character
		Double rand = (Math.random() * accumulator);
		for (int i = 0; i < totals.length; i++) {
			if (rand < totals[i]) {
				return weights.get(i).getPrediction();
			}
		}
		
		// Will get here when no results are found
		System.out.println("HERE");
		return null;
	}
		
	public String getRandomContext() {
		String result = null;
		try {
			openConnection();

			String sqlCmd = String.format("SELECT Word FROM %s "
					      + "WHERE LENGTH(Word) > 1 "
					      + "ORDER BY RANDOM() "
					      + "LIMIT 1", this.wordTable);
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery(sqlCmd);
			while (rs.next()) {
				result = rs.getString("Word");
			}
			stmt.close();
			rs.close();
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		finally {
			closeConnection();
		}
		return result;
	}
	
	public String getDatabase() {
		return this.db;
	}
}
