package com.dmtools.apps;

import java.util.Random;
import java.sql.*;

public class MarkovDataAdapter {

	// SQL connection variables
	private Connection conn;
	private String db;
	private String user;
	private String password;
	private String wordTable;
	private String markovTable;
	
	public MarkovDataAdapter() {
		conn = null;
		db = "jdbc:hsqldb:file:src/conf/markovdb";
		user = "SA";
		password = "";
		
		// Table Names
		this.wordTable = "WordIndex";
		this.markovTable = "MarkovMap";
		
		createProbabilityTable();
	}
	
	private void openConnection() {
		try {
		    // Create database connection
		    this.conn = DriverManager.getConnection(db, user, password);
		}
		catch (SQLException e) {
		    System.err.println(e.getMessage());
		}
	}
		
	private void closeConnection() {
		try {
			// Close connection
			if (this.conn != null) {
				this.conn.close();
			}
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
	}
	
	private void createProbabilityTable() {
		try {
			openConnection();
			// Create Word Indexing Table
			String sqlCreate = "CREATE TABLE IF NOT EXISTS " + this.wordTable
					         + "(WordID INTEGER IDENTITY PRIMARY KEY,"
					         + "Word VARCHAR(3) UNIQUE"
					         + ");";
			Statement stmt = this.conn.createStatement();
			stmt.execute(sqlCreate);
			
			// Create Probability Table
//			sqlCreate = "CREATE TABLE IF NOT EXISTS " + this.markovTable
//			         + "(currentWordID INTEGER NOT NULL FOREIGN KEY, "
//			         + "nextWordID INTEGER NOT NULL FOREIGN KEY"
//			         + ");";
//			stmt.execute(sqlCreate);
			stmt.close();
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		finally {
			closeConnection();
		}
	}
	
	public void insertWord(String word) {
		try {
			openConnection();
			// Insert Word into Word Indexing Table
			String sqlCreate = "INSERT INTO " + this.wordTable + "(Word) "
					+ "VALUES('" + word + "');";
			System.out.println(sqlCreate);
			Statement stmt = this.conn.createStatement();
			stmt.execute(sqlCreate);
			
			stmt.close();
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		finally {
			closeConnection();
		}
	}
	
	public String[] getProbabilityTable(String snip) {		
		try {
			
		    openConnection();
			// Create and execute statement
			Statement stmt = this.conn.createStatement();
			ResultSet rs = stmt.executeQuery("SELECT wordID from WordIndex WHERE word == snip");

			// Loop through the data and print all artist names
			while(rs.next() ) {
				System.out.println("Customer Name: " + rs.getString("FIRSTNAME"));
			}
			
			// Clean up
			rs.close();
			stmt.close();
		}
		catch (SQLException e) {
			System.err.println(e.getMessage());
		}
		finally {
			closeConnection();
		}
		return null;
	}
	
	public static String getNextChar(String snip) {
		Random rand = new Random();
		int r = rand.nextInt(100);
		
		return null;
	}
}
