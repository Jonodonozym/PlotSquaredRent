/**
 * Vault.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

/**
 * Automatically loads Vault shiz so you don't have to worry about that
 * RegisteredServiceProvider stuff thingy.
 *
 * @author Jonodonozym
 */
public final class Vault {
	private static Economy economy = null;
	private static Chat chat = null;
	private static Permission perms = null;

	public static Economy getEconomy() {
		return economy;
	}

	public static Chat getChat() {
		return chat;
	}

	public static Permission getPermission() {
		return perms;
	}

	static void init() {
		setupEconomy();
		setupChat();
		setupPermissions();
	}

	private static void setupEconomy() {
		if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) {
			RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager()
					.getRegistration(Economy.class);
			if (rsp != null)
				economy = rsp.getProvider();
		}
	}

	private static void setupChat() {
		RegisteredServiceProvider<Chat> rsp = Bukkit.getServer().getServicesManager().getRegistration(Chat.class);
		if (rsp != null)
			chat = rsp.getProvider();
	}

	private static void setupPermissions() {
		RegisteredServiceProvider<Permission> rsp = Bukkit.getServer().getServicesManager()
				.getRegistration(Permission.class);
		if (rsp != null)
			perms = rsp.getProvider();
	}

}
