
package jdz.MCPlugins.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.jar.JarFile;

public class FileExporter {
	  /**
     * Export a resource embedded into a Jar file to the local file path.
     *
     * @param resourceName ie.: "/SmartLibrary.dll"
     * @return The path to the exported resource
	 * @throws IOException 
     * @throws Exception
     */
    static public void ExportResource(String resourceName, String destinationPath) {
        InputStream stream = null;
        OutputStream resStreamOut = null;
        try {
            stream = FileExporter.class.getResourceAsStream(resourceName);//note that each / is a directory down in the "jar tree" been the jar the root of the tree
            if(stream == null) {
                throw new Exception("Cannot get resource \"" + resourceName + "\" from Jar file.");
            }

            int readBytes;
            byte[] buffer = new byte[4096];
            resStreamOut = new FileOutputStream(destinationPath);
            while ((readBytes = stream.read(buffer)) > 0) {
                resStreamOut.write(buffer, 0, readBytes);
            }
        } catch (Exception ex) {
            FileLogger.createErrorLog(ex);
        } finally {
        	try{
	            stream.close();
	            resStreamOut.close();
        	}
        	catch(Exception ex2) {}
        }
    }
    
    public static JarFile getRunningJar() throws IOException {
        if (!RUNNING_FROM_JAR) {
            return null; // null if not running from jar
        }
        String path = new File(FileExporter.class.getProtectionDomain()
                .getCodeSource().getLocation().getPath()).getAbsolutePath();
        path = URLDecoder.decode(path, "UTF-8");
        return new JarFile(path);
    }
    
    private static boolean RUNNING_FROM_JAR = false;
    
    static {
        final URL resource = FileExporter.class.getClassLoader()
                .getResource("plugin.yml");
        if (resource != null) {
            RUNNING_FROM_JAR = true;
        }
    }
}
