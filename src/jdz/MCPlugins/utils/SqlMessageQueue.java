
package jdz.MCPlugins.utils;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class SqlMessageQueue extends SqlApi implements Listener {
	private static String MessageQueueTable = null;

	public static void setTable(String table) {
		SqlMessageQueue.MessageQueueTable = table;
		ensureCorrectTables();
	}

	public static void ensureCorrectTables() {
		if (!checkPreconditions())
			return;

		String update = "CREATE TABLE IF NOT EXISTS " + MessageQueueTable
				+ " (player varchar(63), message varchar(1023), priority int);";
		executeUpdate(update);
	}

	public static void addQueuedMessage(OfflinePlayer offlinePlayer, String message) {
		addQueuedMessage(offlinePlayer, message, getHighestPirority(offlinePlayer) + 1);
	}

	public static void addQueuedMessage(OfflinePlayer offlinePlayer, String message, int priority) {
		if (message == "")
			return;
		if (!checkPreconditions())
			return;

		String update = "INSERT INTO " + MessageQueueTable + " (player, message, priority) VALUES(," + offlinePlayer.getName()
				+ "','" + message + "'," + priority + ");";
		executeUpdate(update);
	}

	public static void setQueuedMessages(OfflinePlayer offlinePlayer, List<String> messages) {
		if (!checkPreconditions())
			return;
		String update = "INSERT INTO " + MessageQueueTable + " (player, message, priority) VALUES('" + offlinePlayer.getName()
				+ "','{m}',{p});";
		clearQueuedMessages(offlinePlayer);
		int i = 0;
		for (String s : messages){
			if (s == "")
				continue;
			executeUpdate(update.replace("\\{m\\}", s).replace("\\{p\\}", "" + i++));
		}
	}

	public static void clearQueuedMessages(OfflinePlayer offlinePlayer) {
		if (!checkPreconditions())
			return;

		String update = "DELETE FROM " + MessageQueueTable + " WHERE player = '" + offlinePlayer.getName() + ";";
		executeUpdate(update);
	}

	public static List<String> getQueuedMessages(OfflinePlayer offlinePlayer) {
		if (!checkPreconditions())
			return new ArrayList<String>();

		String query = "SELECT message FROM " + MessageQueueTable + " WHERE player = '" + offlinePlayer.getName() + "' "
				+ "ORDER BY priotiry asc;";
		List<String[]> list = fetchRows(query);
		List<String> returnList = new ArrayList<String>();
		for (String[] str : list)
			returnList.add(str[0]);
		return returnList;
	}

	private static int getHighestPirority(OfflinePlayer offlinePlayer) {
		if (!checkPreconditions())
			return 1000;
		String query = "SELECT MAX(priority) FROM " + MessageQueueTable + " WHERE player = '" + offlinePlayer.getName() + "';";
		try {
			return Integer.parseInt(fetchRows(query).get(0)[0]);
		} catch (Exception e) {
			return 1000;
		}
	}

	private static boolean checkPreconditions() {
		if (MessageQueueTable == null)
			throw new RuntimeException("message queue table must be set first");

		if (autoReconnect())
			return false;
		return true;
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		List<String> messages = getQueuedMessages(event.getPlayer());
		if (!messages.isEmpty()) {
			for (String s : messages)
				event.getPlayer().sendMessage(s);
			clearQueuedMessages(event.getPlayer());
		}
	}
}
