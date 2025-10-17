package com.bgsoftware.wildchests.task;

import com.bgsoftware.wildchests.WildChestsPlugin;
import com.bgsoftware.wildchests.api.objects.chests.Chest;
import com.bgsoftware.wildchests.api.objects.ChestType;
import com.bgsoftware.wildchests.scheduler.ScheduledTask;
import com.bgsoftware.wildchests.scheduler.Scheduler;
import com.bgsoftware.wildchests.utils.BlockPosition;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public final class TrashCleanerTask {

    private static final WildChestsPlugin plugin = WildChestsPlugin.getPlugin();
    
    private static final Map<BlockPosition, Long> nextCleanupTimes = new HashMap<>();
    private static ScheduledTask task = null;

    private TrashCleanerTask() {
        // Start the task to run every second (20 ticks)
        task = Scheduler.runRepeatingTaskAsync(this::run, 20L);
    }

    public static void start() {
        if (task != null) {
            task.cancel();
        }
        
        new TrashCleanerTask();
    }

    public static void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        nextCleanupTimes.clear();
    }

    private void run() {
        long currentTime = System.currentTimeMillis();
        
        // Create a snapshot of chests to avoid concurrent modification
        for (Chest chest : plugin.getChestsManager().getChests()) {
            // Only process regular chests with trash mode enabled
            if (chest.getChestType() != ChestType.CHEST || !chest.getData().isTrashMode()) {
                continue;
            }
            
            BlockPosition position = new BlockPosition(chest.getLocation().getWorld().getName(),
                    chest.getLocation().getBlockX(), chest.getLocation().getBlockY(), chest.getLocation().getBlockZ());
            
            // Check if it's time to clean this chest
            long nextCleanup = nextCleanupTimes.getOrDefault(position, currentTime);
            
            if (currentTime >= nextCleanup) {
                // Schedule the cleanup for the next interval
                long intervalMs = chest.getData().getTrashIntervalSeconds() * 1000L;
                nextCleanupTimes.put(position, currentTime + intervalMs);
                
                // Schedule the actual cleanup on the correct region/thread
                Scheduler.runTask(chest.getLocation(), () -> cleanChestInventory(chest));
            }
        }
    }

    private void cleanChestInventory(Chest chest) {
        try {
            // Verify the chest is still valid and in trash mode
            if (chest.getData().isTrashMode()) {
                // Clear all pages of the chest
                for (Inventory inventory : chest.getPages()) {
                    if (inventory != null) {
                        inventory.clear();
                    }
                }
                
                // Optional: Save the chest inventory immediately to persist the changes
                // plugin.getDataHandler().saveChestInventory(chest);
            }
        } catch (Exception ex) {
            WildChestsPlugin.log("&cError cleaning trash chest at " + chest.getLocation() + ": " + ex.getMessage());
        }
    }

}
