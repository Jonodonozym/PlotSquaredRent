package jdz.MCPlugins.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.plugin.java.JavaPlugin;

import jdz.PSRent.main.Main;

public class FileLogger {
	private static BufferedWriter defaultLogWriter = null;
	private static String directory = null;
	
	public static void init(JavaPlugin plugin){
		directory = plugin.getDataFolder()+File.separator+"Logs";
	}
	
	private static void startNewLog(){
		try{
			if (defaultLogWriter != null)
				defaultLogWriter.close();

			createDefaultDirectory(getLogsDirectory());
			File file = new File(getLogsDirectory() + File.separator + "Log "+getTimestamp()+".txt");
			if (!file.exists())
				file.createNewFile();
			
			defaultLogWriter = new BufferedWriter(new FileWriter(file));
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void log(String message){
		try{
			if (defaultLogWriter == null)
				startNewLog();
			defaultLogWriter.write(getTimestampShort()+": "+message);
			defaultLogWriter.newLine();
		}
		catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public static void logNew(String message){
		startNewLog();
		log(message);
	}
	
	/**
	 * Writes an error log to a file, given an exception and extra data
	 * 
	 * @param e
	 */
	public static void createErrorLog(Exception e, String... extraData) {
		PrintWriter pw = new PrintWriter(new StringWriter());
		e.printStackTrace(pw);
		pw.println();
		pw.println("Extra data:");
		for (String s : extraData)
			pw.println('\t' + s);
		String exceptionAsString = pw.toString();
		createErrorLog(exceptionAsString);
	}

	/**
	 * Writes an error log to a file, given an exception
	 * 
	 * @param e
	 */
	public static void createErrorLog(Exception e) {
		StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = "\n"+sw.toString();
		createErrorLog(exceptionAsString);
	}

	/**
	 * Writes an message to an error log file
	 * 
	 * @param e
	 */
	public static void createErrorLog(String s) {
		Main.plugin.getLogger().info("An error occurred. Check the Error log file for details.");
		String fileDir = getLogsDirectory() + File.separator + "Errors" + File.separator+"Error "
				+ getTimestamp() + ".txt";
		
		createDefaultDirectory(getLogsDirectory());
		createDefaultDirectory(getLogsDirectory() + File.separator + "Errors");
		
		File file = new File(fileDir);
		
		writeFile("An error occurred in the plugin. If you can't work out the issue from this file, send this file to the plugin developer with a description of the failure.\n\n"+s,file);
	}
	
	private static String getLogsDirectory(){
		return directory;
	}
	
	private static void createDefaultDirectory(String directory){
		File file = new File(directory);
		if (!file.exists())
			file.mkdirs();
	}
	
	private static String getTimestamp(){
		return new SimpleDateFormat("yyyy-MM-dd  HH-mm-ss").format(new Date());
	}
	
	private static String getTimestampShort(){
		return "["+new SimpleDateFormat("HH-mm-ss").format(new Date())+"]";
	}
	
	private static void writeFile(String s, File file){
		try {
			if (!file.exists())
				file.createNewFile();
			BufferedWriter bfw = new BufferedWriter(new FileWriter(file));
			bfw.write(s);
			bfw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
