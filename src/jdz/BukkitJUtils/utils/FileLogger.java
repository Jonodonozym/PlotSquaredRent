/**
 * FileLogger.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Lets you log plugin messages in a file
 * Also lets you log errors in the file instead of displaying a big ugly message on the console
 *
 * @author Jonodonozym
 */
public final class FileLogger {
	private static BufferedWriter defaultLogWriter = null;
	
	/**
	 * Starts a new log file
	 * 
	 * you probably never need to do this, I just use it for a few methods myself and thought I should share.
	 * Aren't I a wonderful developer?
	 */
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
		catch(IOException exception){
			exception.printStackTrace();
		}
	}
	
	/**
	 * Logs a message to the current log file
	 * creates a new log file if one isn't already in use
	 * @param message
	 */
	public static void log(String message){
		try{
			if (defaultLogWriter == null)
				startNewLog();
			defaultLogWriter.write(getTimestampShort()+": "+message);
			defaultLogWriter.newLine();
		}
		catch(IOException exception){
			exception.printStackTrace();
		}
	}
	
	/**
	 * Writes an exception's stack trace to an error log file, given an exception and extra information you might want to tack on to help debugging
	 * 
	 * @param exception
	 */
	public static void createErrorLog(Exception exception, String... extraData) {
		PrintWriter pw = new PrintWriter(new StringWriter());
		exception.printStackTrace(pw);
		pw.println();
		pw.println("Extra data:");
		for (String s : extraData)
			pw.println('\t' + s);
		String exceptionAsString = pw.toString();
		createErrorLog(exceptionAsString);
	}

	/**
	 * Writes an exception's stack trace to an error log file, given an exception
	 * 
	 * @param exception
	 */
	public static void createErrorLog(Exception exception) {
		StringWriter sw = new StringWriter();
		exception.printStackTrace(new PrintWriter(sw));
		String exceptionAsString = "\n"+sw.toString();
		createErrorLog(exceptionAsString);
	}

	/**
	 * Writes an error message to an error log file
	 * 
	 * @param exception
	 */
	public static void createErrorLog(String error) {
		BukkitJUtils.plugin.getLogger().info("An error occurred. Check the Error log file for details.");
		String fileDir = getLogsDirectory() + File.separator + "Errors" + File.separator+"Error "
				+ getTimestamp() + ".txt";
		
		createDefaultDirectory(getLogsDirectory());
		createDefaultDirectory(getLogsDirectory() + File.separator + "Errors");
		
		File file = new File(fileDir);
		
		writeFile("An error occurred in the plugin. If you can't work out the issue from this file, send this file to the plugin developer with a description of the failure",error,file);
	}
	
	private static String getLogsDirectory(){
		return BukkitJUtils.plugin.getDataFolder()+File.separator+"Logs";
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
	
	private static void writeFile(String header, String contents, File file){
		try {
			if (!file.exists())
				file.createNewFile();
			BufferedWriter bfw = new BufferedWriter(new FileWriter(file));
			if (header != ""){
				bfw.write(header);
				bfw.newLine();
				bfw.newLine();
			}
			bfw.write(contents);
			bfw.close();
		} catch (IOException exception) {
			exception.printStackTrace();
		}
	}

}
