package net.lifeforge.skycycle;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class SkyCyclePlugin extends JavaPlugin {

    // Configurable durations in minutes
    private static final double DAY_DURATION_MINUTES = 14;
    private static final double NIGHT_DURATION_MINUTES = 12;
    private static final double SUNRISE_DURATION_MINUTES = 2.5;
    private static final double SUNSET_DURATION_MINUTES = 2.5;

    private static final long TICKS_PER_DAY = 24000;
    private static final long SCHEDULER_INTERVAL_TICKS = 100; // 20 = 1s

    private final List<World> controlledWorlds = new ArrayList<>();
    private double ticksPerRun;

    @Override
    public void onEnable() {
        // Add worlds to control
        addWorldIfExists("spawn");
        addWorldIfExists("world");

        if (controlledWorlds.isEmpty()) {
            getLogger().warning("No worlds found to control. Plugin disabled.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Disable vanilla daylight cycle
        controlledWorlds.forEach(world -> world.setGameRuleValue("doDaylightCycle", "false"));

        // Compute ticks per scheduler run
        double totalMinutes = DAY_DURATION_MINUTES + NIGHT_DURATION_MINUTES
                + SUNRISE_DURATION_MINUTES + SUNSET_DURATION_MINUTES;
        ticksPerRun = TICKS_PER_DAY / (totalMinutes * 60.0) * (SCHEDULER_INTERVAL_TICKS / 20.0);

        startScheduler();

        getLogger().info("SkyCyclePlugin enabled. Controlling worlds: " +
                controlledWorlds.stream().map(World::getName).toList());
    }

    private void addWorldIfExists(String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) controlledWorlds.add(world);
        else getLogger().warning("World '" + name + "' not found, skipping.");
    }

    private void startScheduler() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : controlledWorlds) {
                    long newTime = (world.getTime() + (long) ticksPerRun) % TICKS_PER_DAY;
                    world.setTime(newTime);
                }
            }
        }.runTaskTimer(this, 0L, SCHEDULER_INTERVAL_TICKS);
    }
}
