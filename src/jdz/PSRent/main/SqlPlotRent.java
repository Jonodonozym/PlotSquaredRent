
package jdz.PSRent.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

import jdz.BukkitJUtils.utils.FileLogger;
import jdz.BukkitJUtils.utils.SqlApi;

public class SqlPlotRent {
	private static String table = "PlotSquaredRentPaid";

	public static void ensureCorrectTables() {
		String update = "CREATE TABLE IF NOT EXISTS " + table
				+ " (player varchar(63), plotNumber int, daysPaid int, world varchar(63), x int, y int);";
		SqlApi.executeUpdate(update);
	}
	
	public static void purgeNonOwned(Player player, Collection<Plot> plots){
		String query = "Select world, x, y FROM " +table+" Where player = '"+player.getName()+"';";
		List<String[]> list = SqlApi.getRows(query);
		
		List<String> worlds = new ArrayList<String>();
		List<PlotId> pids = new ArrayList<PlotId>();
		for(String[] s: list){
			pids.add(new PlotId(Integer.parseInt(s[1]), Integer.parseInt(s[2])));
			worlds.add(s[0]);
		}
		
		for (Plot p: new PlotAPI().getPlayerPlots(player)){
			if (worlds.contains(p.getWorldName()))
				if (pids.contains(p.getId()))
					removeEntry(p);
		}
	}

	public static void addEntry(OfflinePlayer player, Plot plot, boolean forceUpdate) {
		String query = "SELECT player FROM "+table+" WHERE world = '"+plot.getWorldName()+"' AND x = '"+plot.getId().x+"' AND y = '"+plot.getId().y+"';";
		if (!SqlApi.getRows(query).isEmpty())
			if (!forceUpdate)
				return;
			else{
				String update = "UPDATE "+table+" SET daysPaid = "+RentConfig.freeDays+" WHERE world = '"+plot.getWorldName()+"' AND x = '"+plot.getId().x+"' AND y = '"+plot.getId().y+"';";
				SqlApi.executeUpdate(update);
			}
				
		query = "SELECT MAX(plotNumber) FROM " + table + " WHERE player = '" + player.getName() + "';";
		String result = SqlApi.getRows(query).get(0)[0];
		int plotNumber = 0;
		try { plotNumber = Integer.parseInt(result) + 1; }
		catch (Exception e) { }

		String update = "INSERT INTO " + table + " (player, plotNumber, daysPaid, world, x, y)" + " VALUES('"
				+ player.getName() + "'," + plotNumber + "," + RentConfig.freeDays + ",'" + plot.getWorldName()
				+ "','" + plot.getId().x + "','"+plot.getId().y+"');";
		SqlApi.executeUpdate(update);
	}

	public static void removeEntry(Plot plot) {
		removeEntry(plot.getWorldName(), plot.getId().x, plot.getId().y);
	}

	public static void removeEntry(String world, int x, int y) {
		String query = "SELECT plotNumber FROM "+table+" WHERE world = '"+world+"' AND x = '"+x+"' AND y = '"+y+"';";
		List<String[]> result = SqlApi.getRows(query);
		
		int plotNumber = 0;
		try{ plotNumber = Integer.parseInt(result.get(0)[0]); }
		catch (Exception e) {}
		
		String update = "DELETE FROM " + table + " WHERE world = '"+world+"' AND x = '"+x+"' AND y = '"+y+"';";
		SqlApi.executeUpdate(update);
		
		update = "UPDATE " + table +" SET plotNumber = plotNumber-1 WHERE plotNumber > "+plotNumber+";";
		SqlApi.executeUpdate(update);
	}

	public static void setRentDays(OfflinePlayer offlinePlayer, int plotNumber, int daysPaid) {
		if (daysPaid > RentConfig.maxDays)
			daysPaid = RentConfig.maxDays;

		String update = "UPDATE " + table + " SET daysPaid = " + daysPaid + " WHERE player = '" + offlinePlayer.getName()
				+ "' AND plotNumber = " + plotNumber + ";";
		SqlApi.executeUpdate(update);
	}

	public static int getRentDays(OfflinePlayer offlinePlayer, int plotNumber) {
		String query = "SELECT daysPaid FROM " + table + " WHERE player = '" + offlinePlayer.getName()
				+ "' AND plotNumber = " + plotNumber + ";";
		List<String[]> result = SqlApi.getRows(query);
		
		try {
			return Integer.parseInt(result.get(0)[0]);
		} catch (Exception e) {
			FileLogger.createErrorLog(e);
			return RentConfig.maxDays;
		}
	}
	
	public static int[] getAllRentDays(OfflinePlayer offlinePlayer){
		String query = "SELECT daysPaid FROM " + table + " WHERE player = '" + offlinePlayer.getName()+"' ORDER BY plotNumber asc;";
		List<String[]> result = SqlApi.getRows(query);
		int[] returnArray = new int[result.size()];
		for (int i=0; i<result.size(); i++){
			try {
				returnArray[i] = Integer.parseInt(result.get(i)[0]);
			} catch (Exception e) {
				e.printStackTrace();
				FileLogger.createErrorLog(e);
				returnArray[i] = RentConfig.maxDays;
			}
		}
		return returnArray;
	}

	public static void decreaseRentDays() {
		decreaseRentDays(1);
	}

	public static void decreaseRentDays(int daysPaid) {
		String update = "UPDATE " + table + " SET daysPaid = daysPaid - " + daysPaid+";";
		SqlApi.executeUpdate(update);
	}
	
	@SuppressWarnings("deprecation")
	public static List<Plot> getPlots(OfflinePlayer player){
		String query = "SELECT world, x, y FROM "+table+" WHERE player ='"+player.getName()+"';";
		List<String[]> result = SqlApi.getRows(query);
		List<Plot> plots = new ArrayList<Plot>();
		for (String[] s: result){
			World world = Bukkit.getWorld(s[0]);
			int x = Integer.parseInt(s[1]);
			int y = Integer.parseInt(s[2]);
			plots.add(new PlotAPI().getPlot(world, x, y));
		}
		return plots;
	}

	public static List<String[]> getOverdueRents() {
		return getDaysLessOrEqualThan(0);
	}

	public static List<String[]> getDaysLessOrEqualThan(int days) {
		String query = "SELECT player, world, x, y, daysPaid FROM "+table+" WHERE daysPaid <= "+days+";";
		List<String[]> list = SqlApi.getRows(query);
		
		return list;
	}

	public static List<String[]> getDaysBetweenInclusive(int min, int max) {
		String query = "SELECT player, world, x, y, daysPaid FROM "+table+" WHERE daysPaid <= "+max+" AND daysPaid >= "+min+";";
		List<String[]> list = SqlApi.getRows(query);
		
		return list;
	}
	
	public static void purgeOverdue(){
		String update = "DELETE FROM "+table+" WHERE daysPaid <= 0";
		SqlApi.executeUpdate(update);
	}
	
	public static void fixData(){
		String query = "SELECT player, plotNumber, world, x, y FROM "+table+" ORDER BY plotNumber asc;";
		List<String[]> rows = SqlApi.getRows(query);
		
		HashMap<String, List<String[]>> playerToRow = new HashMap<String, List<String[]>>();
		
		for (String[] row: rows){
			String player = row[0];
			if (!playerToRow.containsKey(player))
				playerToRow.put(player, new ArrayList<String[]>());
			playerToRow.get(player).add(row);
		}
		
		for (List<String[]> list: playerToRow.values()){
			int i=0;
			for (String[] args: list){
				int plotNumber = Integer.parseInt(args[1]);
				if (plotNumber != i){
					String update = "UPDATE " + table +" SET plotNumber = "+i+" WHERE world = '"+args[2]+"' AND x = '"+args[3]+"' AND y = '"+args[4]+"';";
					SqlApi.executeUpdate(update);
				}
				i++;
			}
		}
	}
}
