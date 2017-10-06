/**
 * TimedTask.java
 *
 * Created by Jonodonozym on god knows when
 * Copyright © 2017. All rights reserved.
 * 
 * Last modified on Oct 5, 2017 9:22:58 PM
 */

package jdz.BukkitJUtils.utils;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Asynchronosly runs a task on a loop for you
 * e.g. new TimedTask(20, ()-> {do stuff here}).start();
 * 
 * can .start() and .stop() at will, or just .run() to run it once.
 * 
 * Though if all you're doing is .run() with this class, 
 * you're a special kind of retard.
 *
 * @author Jonodonozym
 */
public final class TimedTask {
	private boolean isRunning = false;
	private final BukkitRunnable runnable;
	private final int tickInterval;
	
	public TimedTask(int tickInterval, Task t){
		this.tickInterval = tickInterval;
		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				t.execute();
			}
		};
		start();
	}
	
	/**
	 * Runs the task a single time
	 */
	public void run(){
		runnable.runTaskAsynchronously(BukkitJUtils.plugin);
	}
	
	/**
	 * Starts the task on a loop if it is not already running
	 */
	public void start() {
		if (!isRunning) {
			runnable.runTaskTimer(BukkitJUtils.plugin, tickInterval, tickInterval);
			isRunning = true;
		}
	}

	/**
	 * Stops the task if it is currently looping
	 */
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
