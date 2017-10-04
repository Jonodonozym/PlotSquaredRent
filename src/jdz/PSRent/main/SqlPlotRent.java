
package jdz.PSRent.main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;

import jdz.MCPlugins.utils.SqlApi;

public class SqlPlotRent extends SqlApi {
	private static String table = "PlotSquaredRentPaid";

	public static void ensureCorrectTables() {
		if (!checkPreconditions())
			return;
		String update = "CREATE TABLE IF NOT EXISTS " + table
				+ " (player varchar(63), plotNumber int, daysPaid int, world varchar(63), x int, y int);";
		SqlApi.executeUpdate(update);
	}
	
	public static void purgeNonOwned(Player player, Collection<Plot> plots){
		String query = "Select world, x, y FROM " +table+" Where player = '"+player.getName()+"';";
		List<String[]> list = fetchRows(query);
		
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
		if (!checkPreconditions())
			return;
		
		String query = "SELECT player FROM "+table+" WHERE world = '"+plot.getWorldName()+"' AND x = '"+plot.getId().x+"' AND y = '"+plot.getId().y+"';";
		if (!fetchRows(query).isEmpty())
			if (!forceUpdate)
				return;
			else{
				String update = "UPDATE "+table+" SET daysPaid = "+RentConfig.freeDays+" WHERE world = '"+plot.getWorldName()+"' AND x = '"+plot.getId().x+"' AND y = '"+plot.getId().y+"';";
				executeUpdate(update);
			}
				
		query = "SELECT MAX(plotNumber) FROM " + table + " WHERE player = '" + player.getName() + "';";
		String result = fetchRows(query).get(0)[0];
		int plotNumber = 0;
		try { plotNumber = Integer.parseInt(result) + 1; }
		catch (NumberFormatException e) { }

		String update = "INSERT INTO " + table + " (player, plotNumber, daysPaid, world, x, y)" + " VALUES('"
				+ player.getName() + "'," + plotNumber + "," + RentConfig.freeDays + ",'" + plot.getWorldName()
				+ "','" + plot.getId().x + "','"+plot.getId().y+"');";
		executeUpdate(update);
	}

	public static void removeEntry(Plot plot) {
		removeEntry(plot.getWorldName(), plot.getId().x, plot.getId().y);
	}

	public static void removeEntry(String world, int x, int y) {
		if (!checkPreconditions())
			return;

		String update = "DELETE FROM " + table + " WHERE world = '"+world+"' AND x = '"+x+"' AND y = '"+y+"';";
		executeUpdate(update);
	}

	public static void setRentDays(OfflinePlayer offlinePlayer, int plotNumber, int daysPaid) {
		if (!checkPreconditions())
			return;

		if (daysPaid > RentConfig.maxDays)
			daysPaid = RentConfig.maxDays;

		String update = "UPDATE " + table + " SET daysPaid = " + daysPaid + " WHERE player = '" + offlinePlayer.getName()
				+ "' AND plotNumber = " + plotNumber + ";";
		executeUpdate(update);
	}

	public static int getRentDays(OfflinePlayer offlinePlayer, int plotNumber) {
		if (!checkPreconditions())
			return RentConfig.maxDays;

		String query = "SELECT daysPaid FROM " + table + " WHERE player = '" + offlinePlayer.getName()
				+ "' AND plotNumber = " + plotNumber + ";";
		List<String[]> result = fetchRows(query);
		try {
			return Integer.parseInt(result.get(0)[0]);
		} catch (Exception e) {
			return RentConfig.maxDays;
		}
	}

	public static void decreaseRentDays() {
		decreaseRentDays(1);
	}

	public static void decreaseRentDays(int daysPaid) {
		if (!checkPreconditions())
			return;

		String update = "UPDATE " + table + " SET daysPaid = daysPaid - " + daysPaid+";";
		executeUpdate(update);
	}

	@SuppressWarnings("deprecation")
	public static List<String[]> getOverdueRents() {
		if (!checkPreconditions())
			return new ArrayList<String[]>();
		
		String query = "SELECT player, world, x, y FROM "+table+" WHERE daysPaid <= 0";
		List<String[]> list = fetchRows(query);
		
		return list;
	}
	
	public static void purgeOverdue(){
		String update = "DELETE FROM "+table+" WHERE daysPaid <= 0";
		executeUpdate(update);
	}

	private static boolean checkPreconditions() {
		if (autoReconnect())
			return false;
		return true;
	}
}
