/**
 * JarUtils.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Class for extracting libs that your BukkitJUtils.plugin uses into the plugins directory
 *
 * @author Jaiden Baker
 */
public final class JarUtils {

	public static void extractLibs(String... libNames) {
		try {
			for (final String libName : libNames) {
				File lib = new File(BukkitJUtils.plugin.getDataFolder().getParentFile(), libName);
				if (!lib.exists())
					extractFromJar(lib.getName(), lib.getAbsolutePath());
			}
			for (final String libName : libNames) {
				File lib = new File(BukkitJUtils.plugin.getDataFolder().getParentFile(), libName);
				if (!lib.exists()) {
					String errorMessage = "There was a critical error loading " + BukkitJUtils.plugin.getName() + "! Could not find lib: "
							+ libName + ". If the problem persists, add it manually to your plugins directory";
					BukkitJUtils.plugin.getLogger().warning(errorMessage);
					FileLogger.createErrorLog(errorMessage);
					BukkitJUtils.plugin.getServer().getPluginManager().disablePlugin(BukkitJUtils.plugin);
					return;
				}
				addClassPath(getJarUrl(lib));
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	private static void addClassPath(final URL url) throws IOException {
		final URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		final Class<URLClassLoader> sysclass = URLClassLoader.class;
		try {
			final Method method = sysclass.getDeclaredMethod("addURL", new Class[] { URL.class });
			method.setAccessible(true);
			method.invoke(sysloader, new Object[] { url });
		} catch (final Throwable t) {
			t.printStackTrace();
			throw new IOException("Error adding " + url + " to system classloader");
		}
	}

	private static boolean extractFromJar(final String fileName, final String dest) throws IOException {
		if (FileExporter.getRunningJar() == null) {
			return false;
		}
		final File file = new File(dest);
		if (file.isDirectory()) {
			file.mkdir();
			return false;
		}
		if (!file.exists()) {
			file.getParentFile().mkdirs();
		}

		final JarFile jar = FileExporter.getRunningJar();
		final Enumeration<JarEntry> e = jar.entries();
		while (e.hasMoreElements()) {
			final JarEntry je = e.nextElement();
			if (!je.getName().contains(fileName)) {
				continue;
			}
			final InputStream in = new BufferedInputStream(jar.getInputStream(je));
			final OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
			copyInputStream(in, out);
			jar.close();
			return true;
		}
		jar.close();
		return false;
	}

	private static URL getJarUrl(final File file) throws IOException {
		return new URL("jar:" + file.toURI().toURL().toExternalForm() + "!/");
	}

	private final static void copyInputStream(final InputStream in, final OutputStream out) throws IOException {
		try {
			final byte[] buff = new byte[4096];
			int n;
			while ((n = in.read(buff)) > 0) {
				out.write(buff, 0, n);
			}
		} finally {
			out.flush();
			out.close();
			in.close();
		}
	}

}