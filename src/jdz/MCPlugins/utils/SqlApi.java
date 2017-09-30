package jdz.MCPlugins.utils;

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
import org.bukkit.plugin.java.JavaPlugin;


/**
 * Utility class with static methods to interact with the sql database
 * 
 * @author Jonodonozym
 */
public class SqlApi {
	private static String dbURL = "";
	private static String dbName = "";
	private static String dbUsername = "";
	private static String dbPassword = "";
	private static int dbReconnectTime = 1200;
	
	private static final String driver = "com.mysql.jdbc.Driver";
	
	private static TimedTask autoReconnectTask = null;
	
	private static Connection dbConnection = null;
	
	private static ConnectHook connectHook = null;
	
	private static JavaPlugin main = null;
	
	public interface ConnectHook{
		public void run();
	}
	
	public static void addConnectHook(ConnectHook hook){
		connectHook = hook;
	}
	
	public static boolean reloadConfig(FileConfiguration config, JavaPlugin plugin){
		main = plugin;
		
		dbURL = config.getString("database.URL");
		dbName = config.getString("database.name");
		dbUsername = config.getString("database.username");
		dbPassword = config.getString("database.password");
		dbReconnectTime = config.getInt("database.autoReconnectSeconds")*20;
		dbReconnectTime = dbReconnectTime<=0?1200:dbReconnectTime;
		
		if (dbURL.equals("") || dbName.equals("") || dbUsername.equals("") || dbPassword.equals("")) {
			main.getLogger().info(
					"Some of the database lines in config.yml are empty, please fill in the config.yml and reload the plugin.");
			return false;
		} 
		
		return (open(plugin.getLogger()) != null);
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
			
			if (connectHook != null)
				connectHook.run();
			connectHook = null;
			
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
			autoReconnectTask = new TimedTask(dbReconnectTime, main, ()->{
				Connection con = SqlApi.open(null);
				if (con !=null)
				{
					main.getLogger().info("Successfully re-connected to the database");
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
	protected static List<String[]> fetchRows(String query) {
		List<String[]> rows = new ArrayList<String[]>();

		if (!isConnected())
			return rows;
		
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

	protected static List<String> fetchColumns(String table) {
		List<String> columns = new ArrayList<String>();
		if (!isConnected())
			return columns;
		
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
	protected static void executeUpdate(String update) {
		if (!isConnected())
			return;
		
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
	 * @param Table
	 * @return
	 */
	protected static boolean hasTable(String Table) {
		if (!isConnected())
			return false;
		
		boolean returnValue = false;
		String query = "SHOW TABLES LIKE '" + Table + "';";
		Statement stmt = null;
		try {
			stmt = dbConnection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()) {
				returnValue = true;
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
		return returnValue;
	}
}
