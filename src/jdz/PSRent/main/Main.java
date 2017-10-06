
package jdz.PSRent.main;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.intellectualcrafters.plot.object.PlotId;
import com.plotsquared.bukkit.events.PlayerClaimPlotEvent;
import com.plotsquared.bukkit.events.PlotClearEvent;
import com.plotsquared.bukkit.events.PlotMergeEvent;
import com.plotsquared.bukkit.events.PlotUnlinkEvent;

import jdz.BukkitJUtils.utils.BukkitJUtils;
import jdz.BukkitJUtils.utils.Config;
import jdz.BukkitJUtils.utils.SqlApi;

public class Main extends JavaPlugin {
	public static Main plugin;

	@SuppressWarnings("deprecation")
	@Override
	public void onEnable() {
		plugin = this;

		SqlApi.runOnConnect(() -> {
			SqlPlotRent.ensureCorrectTables();
			RentChecker.setLastCheck(RentChecker.getLastCheck());
		});

		BukkitJUtils.initialize(this, true);

		FileConfiguration config = Config.getConfig();

		RentConfig.reloadConfig(config);
		Messages.reloadMessages();

		for (Player player : Bukkit.getServer().getOnlinePlayers())
			for (Plot p : new PlotAPI().getPlayerPlots(player))
					SqlPlotRent.addEntry(player, p, false);

		new PlotAPI().registerCommand(new RentCommandHandler());

		getServer().getPluginManager().registerEvents(new Listener() {
			@EventHandler
			public void onJoin(PlayerJoinEvent e) {
				for (Plot p : new PlotAPI().getPlayerPlots(e.getPlayer()))
					SqlPlotRent.addEntry(e.getPlayer(), p, false);
			}

			@EventHandler
			public void onClaim(PlayerClaimPlotEvent event) {
				System.out.println("PING");
				SqlPlotRent.addEntry(event.getPlayer(), event.getPlot(), true);
			}

			@EventHandler
			public void onClaimDelete(PlotClearEvent event) {
				new BukkitRunnable() {
					@Override
					public void run() {
						if (event.getPlot().getOwners().isEmpty())
							SqlPlotRent.removeEntry(event.getPlot());
					}
				}.runTaskLaterAsynchronously(Main.plugin, 60L);
			}

			/*
			@EventHandler(ignoreCancelled = true)
			public void onMerge(PlotMergeEvent event) {
				OfflinePlayer owner;
				if (event.getPlot().getOwners().size() > 0) {
					owner = Bukkit.getOfflinePlayer(event.getPlot().getOwners().iterator().next());
					for (Plot p : event.getPlot().getConnectedPlots())
						SqlPlotRent.removeEntry(p);
					SqlPlotRent.addEntry(owner, event.getPlot(), true, event.getPlot().getConnectedPlots().size());
				}
			}

			@EventHandler(ignoreCancelled = true)
			public void onUnlink(PlotUnlinkEvent event) {
				System.out.println("PONG");
				ArrayList<PlotId> plotIDs = event.getPlots();
				List<Plot> plots = new ArrayList<Plot>();
				for (PlotId pid : plotIDs)
					plots.add(new PlotAPI().getPlot(event.getWorld(), pid.x, pid.y));

				Plot first = plots.get(0);
				OfflinePlayer owner;

				if (first.getOwners().size() > 0) {
					owner = Bukkit.getOfflinePlayer(first.getOwners().iterator().next());
					for (Plot p : plots) {
						SqlPlotRent.removeEntry(p);
						SqlPlotRent.addEntry(owner, p, true);
					}
				}
			}*/
		}, this);
	}
}
