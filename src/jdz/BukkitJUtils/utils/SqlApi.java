/**
 * SqlApi.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;


/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public final class SqlApi {
	private static String dbURL = "";
	private static String dbName = "";
	private static String dbUsername = "";
	private static String dbPassword = "";
	private static int dbReconnectTime = 1200;
	
	private static final String driver = "com.mysql.jdbc.Driver";
	private static TimedTask autoReconnectTask = null;
	private static Connection dbConnection = null;
	private static List<Runnable> connectHooks = new ArrayList<Runnable>();
	
	public static void runOnConnect(Runnable r){
		connectHooks.add(r);
	}
	
	public static boolean reloadConfig(FileConfiguration config){
		dbURL = config.getString("database.URL");
		dbName = config.getString("database.name");
		dbUsername = config.getString("database.username");
		dbPassword = config.getString("database.password");
		dbReconnectTime = config.getInt("database.autoReconnectSeconds")*20;
		dbReconnectTime = dbReconnectTime<=0?1200:dbReconnectTime;
		
		if (dbURL.equals("") || dbName.equals("") || dbUsername.equals("") || dbPassword.equals("")) {
			BukkitJUtils.plugin.getLogger().info(
					"Some of the database lines in config.yml are empty, please fill in the config.yml and reload the plugin.");

			if (!config.contains("database.URL"))
				config.addDefault("database.URL", "");
			
			if (!config.contains("database.name"))
				config.addDefault("database.name", "");
			
			if (!config.contains("database.username"))
				config.addDefault("database.username", "");
			
			if (!config.contains("database.password"))
				config.addDefault("database.password", "");
			
			if (!config.contains("database.autoReconnectSeconds"))
				config.addDefault("database.autoReconnectSeconds", 60);
			
			try {
				config.save(Config.getConfigFile());
			} catch (IOException e) {
				FileLogger.createErrorLog(e, "This error occurred in the BukkitJUtils");
			}
			
			return false;
		} 
		
		return (open(BukkitJUtils.plugin.getLogger()) != null);
	}

	/**
	 * Opens a new connection to a specified SQL database If it fails 3 times,
	 * writes the error to a log file in the plugin's directory
	 * 
	 * @param logger
	 *            the logger to record success / fail messages to
	 * @return the opened connection, or null if one couldn't be created
	 */
	public static Connection open(Logger logger) {
		if (dbConnection != null)
			close(dbConnection);
		try {
			try {
				Class.forName(driver).newInstance();
			} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				if (logger != null) FileLogger.createErrorLog(e);
			}

			String url = "jdbc:mysql://" + dbURL + ":3306/" + dbName + "?user=" + dbUsername + "&password="
					+ dbPassword + "&loginTimeout=1000&useSSL=false";

			Connection dbConnection = DriverManager.getConnection(url, dbUsername, dbPassword);
			dbConnection.setNetworkTimeout(Executors.newFixedThreadPool(2), 15000);
			if (logger != null)
				logger.info("Successfully connected to the " + dbName + " database at the host " + dbURL);

			SqlApi.dbConnection = dbConnection;
			
			for (Runnable r: connectHooks)
				r.run();
			
			return dbConnection;
		}

		catch (SQLException e) {
			if (logger != null){
				logger.info("Failed to connect to the database. Refer to the error log file in the plugin's directory"
						+ " and contact the database host / plugin developer to help resolve the issue.");
				FileLogger.createErrorLog(e);
			}
			autoReconnect();
		}
		return null;
	}

	/**
	 * Closes a given connection, catching any errors
	 * 
	 * @param connection
	 */
	public static boolean close(Connection connection) {
		if (connection != null) {
			try {
				connection.close();
				return true;
			} catch (SQLException e) {
			}
		}
		return false;
	}
	
	public static boolean isConnected(){
		try {
			if (dbConnection != null && !dbConnection.isClosed())
				return true;
		} catch (SQLException e) { }
		return false;
	}
	
	protected static boolean autoReconnect(){
		if (isConnected())
			return false;
		
		if (autoReconnectTask == null){
			autoReconnectTask = new TimedTask(dbReconnectTime, ()->{
				Connection con = SqlApi.open(null);
				if (con !=null)
				{
					BukkitJUtils.plugin.getLogger().info("Successfully re-connected to the database");
					dbConnection = con;
					if(autoReconnectTask != null)
						autoReconnectTask.stop();
					autoReconnectTask = null;
				}
			});
			autoReconnectTask.start();
		}
		return true;
	}

	/**
	 * Executes a query, returning the rows if the database responds with them
	 * 
	 * @param connection
	 * @param query
	 * @return
	 */
	public static List<String[]> getRows(String query) {
		List<String[]> rows = new ArrayList<String[]>();

		if (!isConnected()){
			autoReconnect();
			return rows;
		}
		
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			int columns = rs.getMetaData().getColumnCount();
			while (rs.next()) {
				String[] row = new String[columns];
				for (int i = 1; i <= columns; i++)
					row[i-1] = rs.getString(i);
				if (row.length > 0)
					rows.add(row);
			}
		} catch (SQLException e) {
			FileLogger.createErrorLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					FileLogger.createErrorLog(e);
				}
			}
		}
		return rows;
	}

	public static List<String> getColumns(String table) {
		List<String> columns = new ArrayList<String>();
		if (!isConnected()){
			autoReconnect();
			return columns;
		}
		
		String query = "SHOW columns FROM " + table + ";";
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next())
				columns.add(rs.getString("Field"));
		} catch (SQLException e) {
			FileLogger.createErrorLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					FileLogger.createErrorLog(e);
				}
			}
		}
		return columns;
	}

	/**
	 * Executes a database update
	 * 
	 * @param connection
	 * @param update
	 */
	public static void executeUpdate(String update) {
		if (!isConnected()){
			autoReconnect();
			return;
		}
		
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			stmt.executeUpdate(update);
		} catch (SQLException e) {
			FileLogger.createErrorLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					FileLogger.createErrorLog(e);
				}
			}
		}
	}

	/**
	 * Checks to see if the database has a table
	 * 
	 * @param connection
	 * @param tableName
	 * @return
	 */
	public static boolean hasTable(String tableName) {
		if (!isConnected())
			return false;
		
		String query = "SHOW TABLES LIKE '" + tableName + "';";
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next())
				return true;
		} catch (SQLException e) {
			FileLogger.createErrorLog(e);
		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					FileLogger.createErrorLog(e);
				}
			}
		}
		return false;
	}
	
	/**
	 * Creates a new table, if it doesn't already exist
	 * @param tableName
	 * @param columns
	 */
	public static void addTable(String tableName, SqlColumn... columns){
	    String update = "CREATE TABLE IF NOT EXISTS"+tableName+" (";
	    for(SqlColumn c: columns)
	    	update += c.name()+" "+c.type().getSqlSyntax()+",";
	    if (update.contains(","))
	    	update.substring(0, update.length()-1);
	    update += ");";
	    
	    executeUpdate(update);
	}
	
	public static void removeTable(String tableName){
		String update = "DROP TABLE IF EXISTS '"+tableName+"';";
		executeUpdate(update);
	}

	/**
	 * Adds a single column to the table, if it doesn't exist
	 * @param tableName
	 * @param column
	 */
	public static void addColumn(String tableName, SqlColumn column){
		addColumns(tableName, column);
	}
	
	/**
	 * Adds multiple columns to the table, if they don't exist
	 * @param tableName
	 * @param columns
	 */
	public static void addColumns(String tableName, SqlColumn...columns){
		String update = "ALTER TABLE "+tableName+" ";
		List<String> existingColumns = getColumns(tableName);
		for (SqlColumn c: columns)
			if (!existingColumns.contains(c.name()))
				update += "ADD COLUMN '"+c.name()+"' "+c.type().getSqlSyntax()+" NOT NULL, ";
			
		if (update.contains(",")){
			update = update.substring(0, update.length()-2);
			update += ";";
			executeUpdate(update);
		}
	}
	
	/**
	 * drops a column from a table
	 * @param tableName
	 * @param columnName
	 */
	public static void removeColumn(String tableName, String columnName){
		removeColumns(tableName, columnName);
	}
	
	/**
	 * drops multiple columns from a table
	 * @param tableName
	 * @param columnNames
	 */
	public static void removeColumns(String tableName, String...columnNames){
		String update = "ALTER TABLE "+tableName+" ";
		List<String> existingColumns = getColumns(tableName);
		for (String c: columnNames)
			if (!existingColumns.contains(c))
				update += "DROP COLUMN "+c+", ";

		if (update.contains(",")){
			update = update.substring(0, update.length()-2);
			update += ";";
			executeUpdate(update);
		}
	}
}
