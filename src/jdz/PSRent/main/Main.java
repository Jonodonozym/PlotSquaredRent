
package jdz.PSRent.main;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import com.intellectualcrafters.plot.api.PlotAPI;

import jdz.MCPlugins.utils.Config;
import jdz.MCPlugins.utils.FileLogger;
import jdz.MCPlugins.utils.SqlApi;
import jdz.MCPlugins.utils.SqlMessageQueue;
import jdz.MCPlugins.utils.VaultLoader;
import net.milkbowl.vault.economy.Economy;

public class Main extends JavaPlugin{
	public static Main plugin;
	public static Economy economy;
	
	@SuppressWarnings("deprecation")
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

		//SqlMessageQueue.setTable("PlotSquared_Rent_Messages");
		SqlPlotRent.ensureCorrectTables();

		RentChecker.setLastCheck(RentChecker.getLastCheck());
		RentChecker.create().start();
		
		new PlotAPI().registerCommand(new RentCommandHandler());
		
		getServer().getPluginManager().registerEvents(new SqlMessageQueue(), this);
	}
}
