
package jdz.PSRent.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;

import jdz.BukkitJUtils.utils.FileLogger;
import jdz.BukkitJUtils.utils.SqlMessageQueue;
import jdz.BukkitJUtils.utils.TimedTask;

public class RentChecker{
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Date nextCheck = getNextCheck(new Date(), RentConfig.checkHours, RentConfig.checkMinutes);
	static{
			new TimedTask(6000, ()->{
			if (new Date().after(nextCheck)){
				subtractRent();
				setLastCheck(new Date());
			}
		});
	}

	@SuppressWarnings("deprecation")
	private static void subtractRent(){
		SqlPlotRent.decreaseRentDays();
		
		List<String[]> overdue = SqlPlotRent.getOverdueRents();
		
		for (int i=0; i<overdue.size(); i++){
			World world = Bukkit.getWorld(overdue.get(i)[1]);
			int x = Integer.parseInt(overdue.get(i)[2]);
			int y = Integer.parseInt(overdue.get(i)[3]);
			Plot plot = new PlotAPI().getPlot(world, x, y);
			OfflinePlayer player = Bukkit.getOfflinePlayer(overdue.get(i)[0]);
			if (player != null){
				SqlMessageQueue.addQueuedMessage(player,
						Messages.autoUnclaimed.replace("{w}", plot.getWorldName())
						.replace("{x}", ""+plot.getCenter().getX())
						.replace("{z}", ""+plot.getCenter().getZ()));
				FileLogger.log(player.getName()+"'s plot at "+x+","+y+" rent payments went overdue, so it was unclaimed.");
			}
			
			plot.unlink();
			plot.unclaim();
			plot.clear(()->{});
			
			SqlPlotRent.removeEntry(plot);
		}
	}
	
	public static Date getLastCheck(){
		File file = new File(Main.plugin.getDataFolder() + File.separator + "lastCheck.txt");
		if (!file.exists()){
			try{ file.createNewFile(); }
			catch (IOException e) {}
			return new Date();
		}
		try{
			BufferedReader r = new BufferedReader(new FileReader(file));
			Date d = sdf.parse(r.readLine());
			r.close();
			return d;
		}
		catch (IOException | ParseException e){
			return new Date();
		}
	}
	
	public static void setLastCheck(Date lastCheck){
		nextCheck = getNextCheck(lastCheck, RentConfig.checkHours, RentConfig.checkMinutes);
		File file = new File(Main.plugin.getDataFolder() + File.separator + "lastCheck.txt");
		try{
			if (!file.exists())
				file.createNewFile();
			BufferedWriter w = new BufferedWriter(new FileWriter(file));
			w.write(sdf.format(lastCheck));
			w.close();
		}
		catch(IOException e) {}
	}
	
	
	@SuppressWarnings("deprecation")
	private static Date getNextCheck(Date date, int hours, int minutes){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		
		if (date.getHours() > hours || date.getHours() == hours && date.getMinutes() > minutes)
			c.add(Calendar.DATE, 1);
		Date nextDate = c.getTime();
		nextDate.setHours(hours);
		nextDate.setMinutes(minutes);
		return nextDate;
	}
	
	public static void main(String[] args){
		System.out.println(new Location("world", 0, 0, 0, 0, 0));
	}
}
