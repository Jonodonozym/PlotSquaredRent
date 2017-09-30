package jdz.PSRent.main;

import org.bukkit.configuration.file.FileConfiguration;

import jdz.MCPlugins.utils.FileLogger;

public class RentConfig{
	
	public static int defaultRentDays = 1;

	public static boolean isEnabled = true;
	public static double rentCost = 100000;
	public static int freeDays = 5;
	public static int checkHours = 5;
	public static int checkMinutes = 0;
	public static int warningPerHour7Days = 1;
	public static int warningPerHour4Days = 2;
	public static int warningPerHour1Days = 4;
	public static int maxDays = 50;
	
	public static void reloadConfig(FileConfiguration config) {		
		isEnabled = config.getBoolean("rent.enabled");
		rentCost = config.getDouble("rent.rentCost");
		freeDays = config.getInt("rent.firstFreeDays");
		
		try{
			String[] s = config.getString("rent.checkRentAt").split("(?<=\\G..)");
			checkHours = Integer.parseInt(s[0]);
			checkMinutes = Integer.parseInt(s[1]);
		}
		catch (NumberFormatException e){
			FileLogger.createErrorLog("rent.checkRentAt format is incorrect in config.yml. Should be HHmm e.g. '0500'");
			checkHours = 5;
			checkMinutes = 0;
		}
		
		warningPerHour7Days = config.getInt("rent.warningPerHour7DaysLeft");
		warningPerHour4Days = config.getInt("rent.warningPerHour3DaysLeft");
		warningPerHour1Days = config.getInt("rent.warningPerHour1DayLeft");
		maxDays = config.getInt("rent.maxRentDays");
	}
}
