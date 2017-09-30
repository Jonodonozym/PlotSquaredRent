
package jdz.MCPlugins.utils;

import java.io.File;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class Config {
	public static FileConfiguration getConfig(JavaPlugin plugin){
		File file = new File(plugin.getDataFolder() + File.separator + "config.yml");
		if (!file.exists())
			FileExporter.ExportResource("/config.yml", plugin.getDataFolder() + File.separator + "config.yml");
		
		FileConfiguration config = plugin.getConfig();
		return config;
	}
}
