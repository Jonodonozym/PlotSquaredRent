
package jdz.PSRent.main;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.intellectualcrafters.plot.api.PlotAPI;
import com.intellectualcrafters.plot.object.Plot;
import com.plotsquared.bukkit.events.PlayerClaimPlotEvent;
import com.plotsquared.bukkit.events.PlotClearEvent;
import jdz.MCPlugins.utils.Config;
import jdz.MCPlugins.utils.FileLogger;
import jdz.MCPlugins.utils.SqlApi;
import jdz.MCPlugins.utils.SqlMessageQueue;
import jdz.MCPlugins.utils.VaultLoader;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{
	public static Main plugin;
	public static Economy economy;
	
	@Override
	public void onEnable(){
		plugin = this;
		
		economy = VaultLoader.getEconomy();
		
		FileLogger.init(this);
		
		FileConfiguration config = Config.getConfig(this);
		SqlApi.addConnectHook(()-> { SqlPlotRent.ensureCorrectTables(); });
		SqlApi.reloadConfig(config, this);
		RentConfig.reloadConfig(config);
		Messages.reloadMessages();

		SqlMessageQueue.setTable("PlotSquared_Rent_Messages");
		SqlPlotRent.ensureCorrectTables();

		RentChecker.setLastCheck(RentChecker.getLastCheck());
		RentChecker.create().start();
		
		for (Player player: Bukkit.getServer().getOnlinePlayers())
			for (Plot p: new PlotAPI().getPlayerPlots(player))
				SqlPlotRent.addEntry(player, p, false);
		
		new PlotAPI().registerCommand(new RentCommandHandler());
		
		getServer().getPluginManager().registerEvents(new SqlMessageQueue(), this);
		
		getServer().getPluginManager().registerEvents(new Listener() {	
			@EventHandler
			public void onJoin(PlayerJoinEvent e){
				for (Plot p: new PlotAPI().getPlayerPlots(e.getPlayer()))
					SqlPlotRent.addEntry(e.getPlayer(), p, false);
			}
			
			@EventHandler
			public void onClaim(PlayerClaimPlotEvent event){
				SqlPlotRent.addEntry(event.getPlayer(), event.getPlot(), true);
			}
			
			@EventHandler
			public void onClaimDelete(PlotClearEvent event){
				new BukkitRunnable() {
					@Override
					public void run() {
						if (event.getPlot().getOwners().isEmpty())
							SqlPlotRent.removeEntry(event.getPlot());
					}
				}.runTaskLaterAsynchronously(Main.plugin, 60L);
			}
		}, this);
	}
}
