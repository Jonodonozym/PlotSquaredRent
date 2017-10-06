
package jdz.PSRent.main;

import java.io.File;
import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import jdz.BukkitJUtils.utils.FileExporter;
import jdz.BukkitJUtils.utils.Vault;

public class Messages {
	public static String autoUnclaimed;
	public static String notPlayer;
	public static String notEnoughArguments;
	public static String notEnoughArgumentsPayRent;
	public static String notANumber;
	public static String notASubCommand;
	public static String notAPlotNumber;
	public static String notEnoughMoney;
	public static String rentPaid;
	public static String rentPaidAll;
	public static String tooLong;
	public static String plotListHeadder;
	public static String plotListFooter;

	public static List<String> help;

	public static void reloadMessages(){
		File file = new File(Main.plugin.getDataFolder() + File.separator + "messages.yml");
		if (!file.exists())
			FileExporter.ExportResource("/messages.yml", Main.plugin.getDataFolder() + File.separator + "messages.yml");

		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		autoUnclaimed = colorize(config.getString("messages.autoUnclaimed"));
		notPlayer = colorize(config.getString("messages.notPlayer"));
		notEnoughArguments = colorize(config.getString("messages.notEnoughArguments"));
		notEnoughArgumentsPayRent = colorize(config.getString("messages.notEnoughArgumentsPayRent"));
		notANumber = colorize(config.getString("messages.notANumber"));
		notASubCommand = colorize(config.getString("messages.notASubCommand"));
		notAPlotNumber = colorize(config.getString("messages.notAPlotNumber"));
		notEnoughMoney = colorize(config.getString("messages.notEnoughMoney"));
		rentPaid = colorize(config.getString("messages.rentPaid"));
		rentPaidAll = colorize(config.getString("messages.rentPaidAll"));
		tooLong = colorize(config.getString("messages.tooLong").replaceAll("\\{days\\}", ""+RentConfig.maxDays));
		plotListHeadder = colorize(config.getString("messages.plotListHeadder"));
		plotListFooter = colorize(config.getString("messages.plotListFooter"));
		help = config.getStringList("messages.help");
		for (int i=0; i<help.size(); i++)
			help.set(i, colorize(help.get(i).replace("{rent}", Vault.getEconomy().format(RentConfig.rentCost))));
		
		
	}
	
	private static String colorize(String s){
        if(s == null) return null;
        return s.replaceAll("&([0-9a-f])", "\u00A7$1");
    }
}