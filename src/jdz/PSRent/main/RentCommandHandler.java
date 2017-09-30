
package jdz.PSRent.main;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.commands.CommandCategory;
import com.intellectualcrafters.plot.commands.RequiredType;
import com.intellectualcrafters.plot.commands.SubCommand;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotPlayer;
import com.plotsquared.general.commands.CommandDeclaration;

import jdz.MCPlugins.utils.FileLogger;
import jdz.MCPlugins.utils.SqlApi;

@CommandDeclaration(
	    command = "rent",
	    permission = "plotrent.rent",
	    description = "Charges rent for plot usage",
	    usage = "/plot rent",
	    category = CommandCategory.CLAIMING,
	    requiredType = RequiredType.PLAYER
	)
public class RentCommandHandler extends SubCommand {
	@Override
	public boolean onCommand(PlotPlayer pp, String[] args) {
		Player player = Bukkit.getPlayer(pp.getUUID());
		
		if (args.length == 0){
			player.sendMessage(ChatColor.RED + Messages.notEnoughArgumentsPayRent);
			return true;
		}
		
		switch (args[0].toLowerCase()) {
			case "help":
				for (String s: Messages.help)
					player.sendMessage(s);
				break;
			
			case "pay":
				if (args.length < 2)
					player.sendMessage(ChatColor.RED + Messages.notEnoughArgumentsPayRent);
				else {
					boolean first = false;
					try {
						int plotNumber = Integer.parseInt(args[1]);
						first = true;
						if (args.length == 3)
							player.sendMessage(payRent(player, plotNumber, 1));
						else
							player.sendMessage(payRent(player, plotNumber, Integer.parseInt(args[2])));
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED
								+ Messages.notANumber.replaceAll("\\{n\\}", first ? args[2] : args[1]));
					}
				}
				break;

			case "list":
				player.sendMessage(list(player));
				break;

			case "payall":
				if (args.length < 1)
					player.sendMessage(ChatColor.RED + Messages.notEnoughArgumentsPayRent);
				else {
					try {
						player.sendMessage(payAllRent(player, Integer.parseInt(args[1])));
					} catch (NumberFormatException e) {
						player.sendMessage(ChatColor.RED
								+ Messages.notANumber.replaceAll("\\{n\\}", args[1]));
					}
				}
				break;
			default:
					player.sendMessage(ChatColor.RED+Messages.notASubCommand.replaceAll("\\{s\\}", args[0]));
			}

		return false;
	}

	public static String payRent(Player player, int plotNumber, int days) {
		if (!SqlApi.isConnected())
			return ChatColor.RED + "Couldn't connect to the rent database D:";

		Plot plot = null;
		int i = 0;
		for (Plot p : new PlotAPI().getPlayerPlots(player)) {
			if (i++ == plotNumber) {
				plot = p;
				break;
			}
		}

		if (plot == null)
			return ChatColor.RED + Messages.notAPlotNumber.replaceAll("\\{n\\}", "" + plotNumber);

		int numDaysPaid = SqlPlotRent.getRentDays(player, plotNumber);
		int totalDaysPaid = days + numDaysPaid;
		if (totalDaysPaid > RentConfig.maxDays)
			days = RentConfig.maxDays - numDaysPaid;

		int numMergedPlots = plot.getConnectedPlots().size() + 1;
		double required = RentConfig.rentCost * numMergedPlots * days;

		if (Main.economy.has(player, required)) {
			Main.economy.withdrawPlayer(player, required);
			SqlPlotRent.setRentDays(player, plotNumber, days + numDaysPaid);
			FileLogger.log(player.getName() + " paid rent for " + days + " days for plot " + plotNumber);
			return ChatColor.GREEN + Messages.rentPaid.replaceAll("\\{totalDays\\}", "" + totalDaysPaid)
					.replaceAll("\\{cost\\}", Main.economy.format(required));
		}

		return ChatColor.RED + Messages.notEnoughMoney.replaceAll("\\{cost\\}", Main.economy.format(required))
				.replaceAll("\\{balance\\}", Main.economy.format(Main.economy.getBalance(player)))
				.replaceAll("\\{difference\\}", Main.economy.format(required - Main.economy.getBalance(player)));
	}

	public static String payAllRent(Player player, int days) {
		if (!SqlApi.isConnected())
			return ChatColor.RED + "Couldn't connect to the rent database D:";

		int totalCost = 0;
		int i = 0;
		List<Integer> plotDays = new ArrayList<Integer>();

		for (Plot plot : new PlotAPI().getPlayerPlots(player)) {
			int thisPlotDays = days;
			int numDaysPaid = SqlPlotRent.getRentDays(player, i++);
			if (days + numDaysPaid > RentConfig.maxDays)
				thisPlotDays = RentConfig.maxDays - numDaysPaid;

			int numMergedPlots = plot.getConnectedPlots().size() + 1;
			totalCost += RentConfig.rentCost * numMergedPlots * thisPlotDays;
			plotDays.add(thisPlotDays + numDaysPaid);
		}

		if (Main.economy.has(player, totalCost)) {
			Main.economy.withdrawPlayer(player, totalCost);
			for (i = 0; i < plotDays.size(); i++)
				SqlPlotRent.setRentDays(player, i, plotDays.get(i));
			FileLogger.log(player.getName() + " paid rent for (up to, if it didn't go over " + RentConfig.maxDays + ") "
					+ days + " days on all plots");
			return ChatColor.GREEN + Messages.rentPaidAll.replaceAll("\\{days\\}", "" + days).replaceAll("\\{cost\\}",
					Main.economy.format(totalCost));
		}

		return ChatColor.RED + Messages.notEnoughMoney.replaceAll("\\{cost\\}", Main.economy.format(totalCost))
				.replaceAll("\\{balance\\}", Main.economy.format(Main.economy.getBalance(player)))
				.replaceAll("\\{difference\\}", Main.economy.format(totalCost - Main.economy.getBalance(player)));
	}

	public static String[] list(Player player) {
		if (!SqlApi.isConnected())
			return new String[] { ChatColor.RED + "Couldn't connect to the rent database D:" };

		int numPlots = new PlotAPI().getPlayerPlots(player).size();
		String[] list = new String[numPlots + 2];
		list[0] = Messages.plotListHeadder;
		int i = 1;
		for (Plot plot : new PlotAPI().getPlayerPlots(player)) {
			list[i] = ChatColor.GREEN + "Plot " + i + ": " + plot.getWorldName() + " , (" + plot.getHome().getX() + ","
					+ plot.getHome().getZ() + ") " + ChatColor.GOLD + "Days paid: "
					+ SqlPlotRent.getRentDays(player, i);
			i++;
		}
		list[list.length - 1] = Messages.plotListFooter;
		return list;
	}
}
