
package jdz.PSRent.main;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;

import jdz.BukkitJUtils.utils.TimedTask;

public class WarningMessages {
	private static TimedTask warning7Days, warning4Days, warning1Days;
	
	public static void start(){
		if (warning7Days != null)
			warning7Days.stop();
		if (warning4Days != null)
			warning4Days.stop();
		if (warning1Days != null)
			warning1Days.stop();
		warning7Days = new TimedTask(72000/RentConfig.warningPerHour7Days, ()->{ doWarning(7, 5); } );
		warning4Days = new TimedTask(72000/RentConfig.warningPerHour4Days, ()->{ doWarning(4, 2); } );
		warning1Days = new TimedTask(72000/RentConfig.warningPerHour1Days, ()->{ doWarning(1, 1); } );
		warning7Days.start();
		warning4Days.start();
		warning1Days.start();
	}
	
	@SuppressWarnings("deprecation")  
	private static void doWarning(int maxdDaysLeft, int minDaysLeft){
		List<String[]> data = SqlPlotRent.getDaysBetweenInclusive(minDaysLeft, maxdDaysLeft);
		for(String[] row: data){
			OfflinePlayer player = Bukkit.getOfflinePlayer(row[0]);
			if (player != null && player.isOnline()){
				World world = Bukkit.getWorld(row[1]);
				int x = Integer.parseInt(row[2]);
				int y = Integer.parseInt(row[3]);
				
				Plot plot = new PlotAPI().getPlot(world, x, y);
				
				player.getPlayer().sendMessage(Messages.lowRentWarning
						.replaceAll("\\{d\\}", row[4])
						.replaceAll("\\{w\\}", row[1])
						.replaceAll("\\{x\\}", ""+plot.getCenter().getX())
						.replaceAll("\\{z\\}", ""+plot.getCenter().getZ())
						.replaceAll("days", row[4].equals("1")?"day":"days"));
			}
		}
	}
	
}
