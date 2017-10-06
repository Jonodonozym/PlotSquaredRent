/**
 * Config.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Class for getting the config without worrying about exporting it first
 *
 * @author Jonodonozym
 */
public final class Config {	
	/**
	 * Gets the config as a FileConfiguration
	 * @return
	 */
	public static FileConfiguration getConfig(){
		return YamlConfiguration.loadConfiguration(getConfigFile());
	}
	
	/**
	 * Goes back in time to assassinate Hitler
	 * what do you think this method does?
	 * @return
	 */
	public static File getConfigFile(){
		File file = new File(BukkitJUtils.plugin.getDataFolder() + File.separator + "config.yml");
		if (!file.exists())
			FileExporter.ExportResource("/config.yml", BukkitJUtils.plugin.getDataFolder() + File.separator + "config.yml");
		return file;
	}
}
