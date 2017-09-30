
package jdz.MCPlugins.utils;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class TimedTask {
	private boolean isRunning = false;
	private final BukkitRunnable runnable;
	private final int time;
	private final JavaPlugin plugin;

	public TimedTask(int time, JavaPlugin plugin, Task t){
		this.time = time;
		this.plugin = plugin;
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				t.execute();
			}
		};
	}
	
	public void run(){
		runnable.run();
	}
	
	public void start() {
		if (!isRunning) {
			runnable.runTaskTimer(plugin, time, time);
			isRunning = true;
		}
	}

	public void stop() {
		if (isRunning) {
			runnable.cancel();
			isRunning = false;
		}
	}
	
	public boolean isRunning(){
		return isRunning;
	}

	public interface Task{
		public void execute();
	}
}
