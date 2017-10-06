package jdz.PSRent.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

import jdz.BukkitJUtils.utils.FileLogger;
import jdz.BukkitJUtils.utils.SqlApi;
import jdz.BukkitJUtils.utils.Vault;

@CommandDeclaration(command = "rent", permission = "plotrent.rent", description = "Charges rent for plot usage", usage = "/plot rent", category = CommandCategory.CLAIMING, requiredType = RequiredType.PLAYER)
public class RentCommandHandler extends SubCommand {
	@Override
	public boolean onCommand(PlotPlayer pp, String[] args) {
		Player player = Bukkit.getPlayer(pp.getUUID());

		if (args.length == 0) {
			player.sendMessage(ChatColor.RED + Messages.notEnoughArgumentsPayRent);
			return true;
		}

		switch (args[0].toLowerCase()) {
		case "help":
			for (String s : Messages.help)
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
					if (args.length == 2)
						player.sendMessage(payRent(player, plotNumber, 1));
					else
						player.sendMessage(payRent(player, plotNumber, Integer.parseInt(args[2])));
				} catch (NumberFormatException e) {
					player.sendMessage(ChatColor.RED + Messages.notANumber.replace("{n}", first ? args[2] : args[1]));
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
					player.sendMessage(ChatColor.RED + Messages.notANumber.replace("{n}", args[1]));
				}
			}
			break;
		default:
			player.sendMessage(ChatColor.RED + Messages.notASubCommand.replace("{s}", args[0]));
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
			return ChatColor.RED + Messages.notAPlotNumber.replace("{n}", "" + plotNumber);

		int numDaysPaid = SqlPlotRent.getRentDays(player, plotNumber);

		if (numDaysPaid >= RentConfig.maxDays)
			return ChatColor.RED + "You have already paid the maximum of " + RentConfig.maxDays + " days in advance!";

		if (days < 1)
			days = 1;

		int totalDaysPaid = days + numDaysPaid;
		if (totalDaysPaid > RentConfig.maxDays)
			days = RentConfig.maxDays - numDaysPaid;

		double required = RentConfig.rentCost * days;

		if (Vault.getEconomy().has(player, required)) {
			Vault.getEconomy().withdrawPlayer(player, required);
			SqlPlotRent.setRentDays(player, plotNumber, days + numDaysPaid);
			FileLogger.log(player.getName() + " paid rent for " + days + " days for plot " + plotNumber);
			return ChatColor.GREEN + Messages.rentPaid.replace("{n}", "" + plotNumber).replace("{totalDays}", "" + days)
					.replace("{cost}", Vault.getEconomy().format(required));
		}

		return ChatColor.RED + Messages.notEnoughMoney.replace("{cost}", Vault.getEconomy().format(required))
				.replace("{balance}", Vault.getEconomy().format(Vault.getEconomy().getBalance(player)))
				.replace("{difference}", Vault.getEconomy().format(required - Vault.getEconomy().getBalance(player)));
	}

	public static String payAllRent(Player player, int days) {
		if (!SqlApi.isConnected())
			return ChatColor.RED + "Couldn't connect to the rent database D:";

		if (days < 1)
			days = 1;

		double totalCost = 0;
		List<Integer> plotDays = new ArrayList<Integer>();

		int i = 0;

		for (Plot plot : SqlPlotRent.getPlots(player)) {
			int thisPlotDays = days;
			int numDaysPaid = SqlPlotRent.getRentDays(player, i);

			int totalDaysPaid = thisPlotDays + numDaysPaid;
			if (totalDaysPaid > RentConfig.maxDays)
				thisPlotDays = RentConfig.maxDays - numDaysPaid;

			totalCost += RentConfig.rentCost * thisPlotDays;
			plotDays.add(thisPlotDays + numDaysPaid);

			i++;
		}

		if (Vault.getEconomy().has(player, totalCost)) {
			Vault.getEconomy().withdrawPlayer(player, totalCost);
			for (i = 0; i < plotDays.size(); i++)
				SqlPlotRent.setRentDays(player, i, plotDays.get(i));
			FileLogger.log(player.getName() + " paid rent for (up to, if it didn't go over " + RentConfig.maxDays + ") "
					+ days + " days on all plots");
			return ChatColor.GREEN + Messages.rentPaidAll.replace("{days}", "" + days).replace("{cost}",
					Vault.getEconomy().format(totalCost));
		}

		return ChatColor.RED + Messages.notEnoughMoney.replace("{cost}", Vault.getEconomy().format(totalCost))
				.replace("{balance}", Vault.getEconomy().format(Vault.getEconomy().getBalance(player)))
				.replace("{difference}", Vault.getEconomy().format(totalCost - Vault.getEconomy().getBalance(player)));
	}

	public static String[] list(Player player) {
		if (!SqlApi.isConnected())
			return new String[] { ChatColor.RED + "Couldn't connect to the rent database D:" };

		int numPlots = new PlotAPI().getPlayerPlots(player).size();
		String[] list = new String[numPlots + 2];
		list[0] = Messages.plotListHeadder;
		int i = 0;
		for (Plot plot : SqlPlotRent.getPlots(player)) {
			list[i + 1] = ChatColor.GREEN + "Plot " + i + ": " + plot.getWorldName() + " (" + plot.getCenter().getX()
					+ "," + plot.getCenter().getZ() + ") " + ChatColor.GOLD + " Days paid: "
					+ SqlPlotRent.getRentDays(player, i);
			i++;
		}
		list[list.length - 1] = Messages.plotListFooter;
		return list;
	}
}
