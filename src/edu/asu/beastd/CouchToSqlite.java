// Copyright (C) 2012-2013 Stevie Robinson, Eric Alford, Tara Mendoza, Blake Tucker, Anthony Sanchez, Davenn Mannix
//
// This file is part of CouchToSqlite.
//
// CouchToSqlite is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// CouchToSqlite is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with CouchToSqlite.  If not, see <http://www.gnu.org/licenses/>.

package edu.asu.beastd;

import com.fourspaces.couchdb.*;
import com.sun.istack.internal.logging.Logger;

import java.io.*;
import java.util.*;
import java.util.logging.Level;

/**
 * The CouchToSqlite class can take in a SQLite file location and/or CouchDB database location in 
 * its constructor and successfully convert CouchDB into relational SQLite databases.<br>
 * If no CouchDB instance is specified in the constructor, the class defaults to localhost:5984, and if no output file is
 * specified, it defaults to "TestDB.db" as a location.
 * @author Team BEASTD
 */
public class CouchToSqlite {
	
	private static Logger LOG = Logger.getLogger(CouchToSqlite.class);
	
	private SQLite sqlite;
	private Session couchSession;
	
	/**
	 * Constructor for default SQLite location ("TestDB.db") and default CouchDB location (localhost:5984).
	 * @throws CouchException If there is an error accessing the CouchDB instance
	 * @throws SqliteException If there is an error accessing the SQLite .db location
	 */
	public CouchToSqlite() throws CouchException, SqliteException {
		this("TestDB.db", "localhost", 5984);
	}
	
	/**
	 * Constructor for a specific SQLite .db file output location and default CouchDB location (localhost:5984).
	 * @param dbPath Output location of the .db SQLite file generated.
	 * @throws CouchException If there is an error accessing the CouchDB instance
	 * @throws SqliteException If there is an error accessing the SQLite .db location
	 */
	public CouchToSqlite(String dbPath) throws CouchException, SqliteException {
		this(dbPath, "localhost", 5984);
	}
	/**
	 * Constructor for default SQLite location ("TestDB.db") and a specific CouchDB location.
	 * @param couchHost The host location of the CouchDB instance
	 * @param couchPort The port to access CouchDB on (typically the default 5984)
	 * @throws CouchException If there is an error accessing the CouchDB instance
	 * @throws SqliteException If there is an error accessing the SQLite .db location
	 */
	public CouchToSqlite(String couchHost, int couchPort) throws CouchException, SqliteException {
		this("TestDB.db", couchHost, couchPort);
	}
	
	/**
	 * Constructor for a specific SQLite .db file output location and specific CouchDB location.
	 * @param dbPath Output location of the .db SQLite file generated
	 * @param couchHost The host location of the CouchDB instance
	 * @param couchPort The port to access CouchDB on (typically the default 5984)
	 * @throws CouchException If there is an error accessing the CouchDB instance
	 * @throws SqliteException If there is an error accessing the SQLite .db location
	 */
	public CouchToSqlite(String dbPath, String couchHost, int couchPort) throws CouchException, SqliteException {
		try {
			this.sqlite = new SQLite(dbPath);
		} catch (Exception e){
			LOG.logException(e, Level.WARNING);
			throw new SqliteException("Couldn't connect to file location: " + e.getMessage(), e.getCause());
		}
		try {
			this.couchSession = new Session(couchHost, couchPort);
		} catch (Exception e) {
			LOG.logException(e, Level.WARNING);
			throw new CouchException("Couldn't connect to Couch instance: " + e.getMessage(), e.getCause());
		}
	}
	
	/**
	 * Translates the CouchDB instance to SQLite.
	 * @throws CouchException If there is any error involving CouchDB
	 * @throws SqliteException If there is any error involving SQLite
	 */
	public void translateToSqlite() throws CouchException, SqliteException {
		
		List<String> databaseList = null;
		try
		{
			databaseList = getDatabaseNames();
		} catch (Exception e) {
			LOG.logException(e, Level.WARNING);
			throw new CouchException("Problem getting database names from Couch.", e.getCause());
		}
			
		// For each db in databaseList, get the documents, create a table for each db, add in documents to each table.
		// TODO: Add in transaction handling using JDBC. Only commit transaction after we've finished this for loop?
		for(int i=0;i<databaseList.size();i++)
		{
			// Acquire each database and get its documents
			Database db = couchSession.getDatabase(databaseList.get(i));
			ViewResults docList = db.getAllDocuments();
			
			// Initialize table structure using the first document
			if (!(docList.getResults().isEmpty())) {
				try {
					initializeTable(db.getName(), db.getDocument(docList.getResults().get(0).getId()));
				} catch (IOException e) {
					LOG.logException(e, Level.WARNING);
					throw new CouchException("Problem getting CouchDB through couchdb4j.", e.getCause());
				}
			}
			
			// Now, add in the rest of the documents
			if (docList.size() > 1)
			{
				addDocumentsToTable(db.getName(), docList.getResults().subList(1, docList.getResults().size()));
			}
			
		} //end db for loop
	}
	
	/**
	 * Retrieves the list of database names from the couch session.
	 * @return A list of strings containing the names of all databases in the instance
	 * @throws CouchException If there is any error acquiring the database names from Couch
	 */
	public List<String> getDatabaseNames() throws CouchException{
		List<String> toReturn;
		try {
			toReturn = couchSession.getDatabaseNames();
		} catch (Exception e) {
			LOG.logException(e, Level.WARNING);
			throw new CouchException("Problem getting database names from Couch.", e.getCause());
		}
		return toReturn;
	}
	
	/**
	 * Method to initialize a SQLite table for a particular database
	 * @param databaseName The CouchDB database name (used as table name)
	 * @param doc The initial document used to structure the SQLite table
	 * @throws SqliteException If there are any SQLite-related errors
	 */
	private void initializeTable(String databaseName, Document doc) throws SqliteException {
		
		@SuppressWarnings("unchecked") // We can add this because couchdb4j stores key-value pairs as strings
		Set<String> docFields = doc.keySet();
		
		Iterator<String> fieldIt = docFields.iterator();
		
		// Drops the table if it exists. Ignores otherwise.
		// NOTE: We may want to consider just dropping all tables at the beginning, once we have transactions in place. This would help to eliminate leftover
		// and deleted CouchDB databases, which will with the library as is at the moment.
		try{
			sqlite.executeSql("DROP TABLE " + databaseName);
		}
		catch (Exception e) {
			LOG.info("Skipped dropping table " + databaseName, e);
		}
		
		// Creates the table for the particular database. Initializes all columns to the structure of the
		// first document.
		// TODO: Somebody add in type checking, so we don't only add in strings.
		sqlite.executeSql("CREATE TABLE " + databaseName + "(" + fieldIt.next() + " VARCHAR(100000000))");
		while (fieldIt.hasNext())
		{
			sqlite.executeSql("ALTER TABLE " + databaseName + " ADD COLUMN " + fieldIt.next() + " VARCHAR(100000000)");
		}
		
		fieldIt = docFields.iterator(); // reset fieldIt for adding values into doc.
		
		// Build keySetString and valueSetString to inject into SQLite command.
		// We do this by moving through the fields and adding values to the string
		String keySetString = new String();
		while(fieldIt.hasNext())
		{
			keySetString += fieldIt.next() + ",";
		}
		keySetString = keySetString.substring(0, keySetString.length()-1); //remove extra comma
		fieldIt = docFields.iterator();
		String valueSetString = new String();
		while(fieldIt.hasNext())
		{
			// replaceAll is to add escape character for single quote
			valueSetString += "'" + doc.getString(fieldIt.next()).replaceAll("'", "''") + "',";
		}
		valueSetString = valueSetString.substring(0, valueSetString.length()-1);
		sqlite.executeSql("INSERT INTO " + databaseName + "( " + keySetString + ") VALUES (" + valueSetString + ")");
	}
	
	/**
	 * Auxiliary method used to add additional documents to a particular table after it has already been initialized.
	 * @param tableName The table to add rows to
	 * @param docList The list of documents to add to said table (the table should already have been initialized using initializeTable)
	 * @throws SqliteException If there are any SQLite-related errors
	 */
	private void addDocumentsToTable(String tableName, List<Document> docList) throws SqliteException {
		for(int i=0;i<docList.size();i++){
			Document doc = docList.get(i);
			
			@SuppressWarnings("unchecked") // We can add this because couchdb4j stores key-value pairs as strings
			Set<String> docFields = doc.keySet();
			
			Iterator<String> fieldIt = docFields.iterator();
			String keySetString = "";
			while(fieldIt.hasNext())
			{
				keySetString += fieldIt.next() + ",";
			}
			keySetString = keySetString.substring(0, keySetString.length()-1);
			fieldIt = docFields.iterator();
			String valueSetString = "";
			while(fieldIt.hasNext())
			{
				// Checks if a particular column already exists. If it doesn't, it is added to
				// the appropriate table.
				String columnName = fieldIt.next();
				try {
					if (!(sqlite.hasColumn(columnName, tableName)))
					{
						sqlite.executeSql("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " VARCHAR(100000000)");
					}
				} catch (SqliteException e) {
					LOG.logException(e, Level.WARNING);
					throw new SqliteException("Error adding new column to a table: " + e.getMessage(), e.getCause());
				}
				valueSetString += "'" + doc.getString(columnName).replaceAll("'", "''") + "',";
			}
			valueSetString = valueSetString.substring(0, valueSetString.length()-1);
			sqlite.executeSql("INSERT INTO " + tableName + "( " + keySetString + ") VALUES (" + valueSetString + ")");
		}
	}
	
	

}