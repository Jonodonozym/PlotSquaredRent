
package jdz.PSRent.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;

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

	public static void addEntry(OfflinePlayer player, Plot plot) {
		if (!checkPreconditions())
			return;
		
		String query = "SELECT player FROM "+table+" WHERE world = '"+plot.getWorldName()+"' AND x = "+plot.getId().x+" AND y = "+plot.getId();
		if (!fetchRows(query).isEmpty())
			return;

		query = "SELECT MAX(plotNumber) FROM " + table + " WHERE player = '" + player.getName() + "';";
		String result = fetchRows(query).get(0)[0];
		int plotNumber = 0;
		try {
			plotNumber = Integer.parseInt(result) + 1;
		} catch (NumberFormatException e) {
		}

		String update = "INSERT INTO " + table + " (player, plotNumber, daysPaid, world, x, y)" + " VALUES('"
				+ player.getName() + "'," + plotNumber + "," + RentConfig.defaultRentDays + ",'" + plot.getWorldName()
				+ "'," + plot.getId().x + ","+plot.getId().y+");";
		executeUpdate(update);
	}

	public static void removeEntry(OfflinePlayer offlinePlayer, Plot plot) {
		if (!checkPreconditions())
			return;

		String update = "DELETE FROM " + table + " WHERE world = '"+plot.getWorldName()+"' AND x = "+plot.getId().x+" AND y = "+plot.getId();
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
	public static List<Plot> getOverdueRents() {
		List<Plot> plots = new ArrayList<Plot>();
		if (!checkPreconditions())
			return plots;
		
		String query = "SELECT world, x, y FROM "+table+" WHERE daysPaid <= 0";
		List<String[]> list = fetchRows(query);
		
		for (String[] s: list){
			World world = Bukkit.getWorld(s[0]);
			int x = Integer.parseInt(s[1]);
			int y = Integer.parseInt(s[2]);
			plots.add(new PlotAPI().getPlot(world, x, y));
		}
		
		return plots;
	}

	private static boolean checkPreconditions() {
		if (autoReconnect())
			return false;
		return true;
	}
}
