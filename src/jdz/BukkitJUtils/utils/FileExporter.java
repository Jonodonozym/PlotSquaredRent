/**
 * FileExporter.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.JarFile;

/**
 * Lets you export files that you include in your plugin's .jar
 *
 * @author Jonodonozym
 */
public final class FileExporter {
	static boolean RUNNING_FROM_JAR = false;

	/**
	 * Export a resource embedded into a Jar file to the local file path.
	 *
	 * @param resourceName
	 *            ie.: "/SmartLibrary.dll"
	 * @return The path to the exported resource
	 * @throws IOException
	 * @throws Exception
	 */
	static private void ExportResource(String resourceName, String destinationPath, boolean isSilent) {
		InputStream stream = null;
		OutputStream resStreamOut = null;
		try {
			stream = FileExporter.class.getResourceAsStream("/" + resourceName);
			if (stream == null) {
				throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
			}

			int readBytes;
			byte[] buffer = new byte[4096];
			resStreamOut = new FileOutputStream(destinationPath);
			while ((readBytes = stream.read(buffer)) > 0) {
				resStreamOut.write(buffer, 0, readBytes);
			}
		} catch (Exception ex) {
			if (!isSilent)
				FileLogger.createErrorLog(ex);
		} finally {
			try {
				stream.close();
				resStreamOut.close();
			} catch (Exception ex2) {
			}
		}
	}

	/**
	 * Export a resource embedded into a Jar file to the local file path without
	 * creating error logs.
	 *
	 * @param resourceName
	 *            ie.: "SmartLibrary.dll"
	 * @return The path to the exported resource
	 * @throws IOException
	 * @throws Exception
	 */
	public static void ExportResourceSilent(String resourceName, String destinationPath) {
		ExportResource("/" + resourceName, destinationPath, true);
	}

	/**
	 * Export a resource embedded into a Jar file to the local file path.
	 *
	 * @param resourceName
	 *            ie.: "SmartLibrary.dll"
	 */
	public static void ExportResource(String resourceName, String destinationPath) {
		ExportResource("/" + resourceName, destinationPath, false);
	}

	/**
	 * Export a resource embedded into a Jar file to the local file path.
	 *
	 * @param resourceName
	 *            ie.: "SmartLibrary.dll"
	 */
	public static void ExportResource(String resourceName) {
		String location = BukkitJUtils.plugin.getDataFolder().getParentFile().getPath() + File.separator + resourceName;
		ExportResource("/" + resourceName, location, false);
	}

	public static JarFile getRunningJar() throws IOException {
		if (!RUNNING_FROM_JAR) {
			return null; // null if not running from jar
		}
		String path = new File(FileExporter.class.getProtectionDomain().getCodeSource().getLocation().getPath())
				.getAbsolutePath();
		path = URLDecoder.decode(path, "UTF-8");
		return new JarFile(path);
	}

	static {
		final URL resource = FileExporter.class.getClassLoader().getResource("plugin.yml");
		if (resource != null) {
			RUNNING_FROM_JAR = true;
		}
	}
}
