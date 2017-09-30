
package jdz.MCPlugins.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public class VaultLoader {
	private static Economy economy = null;
	
	public static Economy getEconomy(){
		if (economy == null)
			loadEconomy();
		return economy;
	}
	
    public static void loadEconomy(){
        if ( Bukkit.getServer().getPluginManager().getPlugin("Vault") != null){
            RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
            if(rsp!=null){
               economy = rsp.getProvider();
            }
        }
    }   

}
