
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

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.object.Location;
import com.intellectualcrafters.plot.object.Plot;

import jdz.MCPlugins.utils.SqlMessageQueue;
import jdz.MCPlugins.utils.TimedTask;

public class RentChecker extends TimedTask{
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private static Date nextCheck = getNextCheck(new Date(), RentConfig.checkHours, RentConfig.checkMinutes);
	private static RentChecker instance = null;
	
	public static RentChecker getInstance(){
		return instance;
	}
	
	public static RentChecker create(){
		// TODO change to 6000
		instance = new RentChecker(300, Main.plugin);
		return instance;
	}
	
	private RentChecker(int time, JavaPlugin plugin) {
		super(time, plugin, ()->{
			if (new Date().after(nextCheck)){
				subtractRent();
				setLastCheck(new Date());
			}
		});
	}

	private static void subtractRent(){
		SqlPlotRent.decreaseRentDays();
		List<Plot> overdue = SqlPlotRent.getOverdueRents();
		for (Plot plot: overdue){
			SqlMessageQueue.addQueuedMessage(Bukkit.getOfflinePlayer(plot.getOwners().iterator().next()),
					Messages.autoUnclaimed.replace("\\{w\\}", plot.getWorldName())
					.replace("\\{x\\}", ""+plot.getCenter().getX())
					.replace("\\{z\\}", ""+plot.getCenter().getZ()));
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
	
	
	//@SuppressWarnings("deprecation")
	private static Date getNextCheck(Date date, int hours, int minutes){
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		
		//TODO remove
		c.add(Calendar.SECOND, 15);
		return c.getTime();
		
		/*
		if (date.getHours() > hours || date.getHours() == hours && date.getMinutes() > minutes)
			c.add(Calendar.DATE, 1);
		Date nextDate = c.getTime();
		nextDate.setHours(hours);
		nextDate.setMinutes(minutes);
		return date;
		*/
	}
	
	public static void main(String[] args){
		System.out.println(new Location("world", 0, 0, 0, 0, 0));
	}
}
