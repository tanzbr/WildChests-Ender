package com.bgsoftware.wildchests.listeners;

import com.bgsoftware.wildchests.WildChestsPlugin;
import com.bgsoftware.wildchests.api.objects.chests.Chest;
import com.bgsoftware.wildchests.api.objects.data.ChestData;
import com.bgsoftware.wildchests.utils.ChestUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Listener responsible for instant item collection.
 * When an item spawns, it checks for nearby chests with instant-collection enabled
 * and collects the item directly without creating an entity.
 */
public class ItemSpawnListener implements Listener {

    private final WildChestsPlugin plugin;
    
    // Cache to store last collected material per chest to avoid spam
    // Structure: <ChestLocation, <Material, LastCollectionTime>>
    private final Map<Location, Map<Material, Long>> collectionCooldowns = new ConcurrentHashMap<>();
    
    // Set to track items dropped by players (for cactus nerf feature)
    // Items in this set are player-dropped and should be collected regardless of nerf settings
    private final Set<UUID> playerDroppedItems = ConcurrentHashMap.newKeySet();

    public ItemSpawnListener(WildChestsPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Tracks items dropped by players for cactus nerf feature.
     * Player-dropped items are marked and have 100% collection rate.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        UUID itemUUID = event.getItemDrop().getUniqueId();
        playerDroppedItems.add(itemUUID);
        
        // Schedule cleanup after 10 seconds to prevent memory leaks
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            playerDroppedItems.remove(itemUUID);
        }, 200L); // 10 seconds
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Location itemLocation = event.getLocation();
        ItemStack itemStack = event.getEntity().getItemStack();
        UUID itemUUID = event.getEntity().getUniqueId();

        // Get all nearby chests with suction enabled
        List<Chest> nearbyChests = plugin.getChestsManager().getNearbyChests(itemLocation);
        
        if (nearbyChests.isEmpty())
            return;
        
        // Try to collect the item in the nearest chest with instant-collection enabled
        for (Chest chest : nearbyChests) {
            ChestData chestData = chest.getData();
            
            // Check if instant-collection is enabled
            if (!chestData.isInstantCollection())
                continue;
            
            // Check cactus nerf: if enabled, naturally grown cactus has only 25% collection chance
            // Player-dropped cactus has 100% collection chance
            if (chestData.isCactusNerfEnabled() && itemStack.getType() == Material.CACTUS) {
                boolean isPlayerDropped = playerDroppedItems.contains(itemUUID);
                if (!isPlayerDropped) {
                    // Natural cactus: 75% chance to collect (25% chance to skip)
                    double randomChance = Math.random();
                    if (randomChance > 0.5) {
                        // Skip this chest - failed the 75% collection chance
                        event.setCancelled(true);
                        return;
                    }
                }
                // If player-dropped, continue normally with 100% collection
            }
            
            // Check if item passes the suction filter
            if (!ChestUtils.SUCTION_PREDICATE.test(event.getEntity(), chestData))
                continue;
            
            // Try to add the item to the chest
            Map<Integer, ItemStack> leftOvers = chest.addItems(itemStack.clone());
            
            // If the entire item was collected
            if (leftOvers.isEmpty()) {
                // Cancel the spawn event - item collected instantly
                event.setCancelled(true);
                
                // Remove from player-dropped tracking since it's collected
                playerDroppedItems.remove(itemUUID);
                
                // Notify nearby players
                notifyNearbyPlayers(chest, itemLocation, itemStack);
                
                return; // Item fully collected, stop processing
            } 
            // If partial collection occurred
            else if (leftOvers.size() == 1 && leftOvers.containsKey(0)) {
                ItemStack leftOver = leftOvers.get(0);
                
                // Check if any items were actually collected
                if (leftOver.getAmount() < itemStack.getAmount()) {
                    // Update the spawned item to only have the leftover amount
                    event.getEntity().setItemStack(leftOver);
                    
                    // Keep in player-dropped tracking if it was player-dropped
                    // (partial collection means item still exists)
                    
                    // Notify nearby players about partial collection
                    notifyNearbyPlayers(chest, itemLocation, itemStack);
                    
                    // Don't cancel event, let the remaining items spawn
                    return;
                }
            }
        }
    }

    /**
     * Notifies players within the notification radius about item collection.
     * Includes a cooldown system to prevent spam for the same item type.
     */
    private void notifyNearbyPlayers(Chest chest, Location location, ItemStack itemStack) {
        // Check if notifications are enabled
        if (!plugin.getSettings().suctionNotificationsEnabled)
            return;
        
        Material material = itemStack.getType();
        Location chestLocation = chest.getLocation();
        
        // Check cooldown for this chest and material
        Map<Material, Long> chestCooldowns = collectionCooldowns.computeIfAbsent(chestLocation, k -> new HashMap<>());
        Long lastNotification = chestCooldowns.get(material);
        long currentTime = System.currentTimeMillis();
        
        // If cooldown is active, don't send notification
        long cooldownTime = plugin.getSettings().suctionNotificationsDuplicateDelay;
        if (lastNotification != null && (currentTime - lastNotification) < cooldownTime) {
            return;
        }
        
        // Update cooldown
        chestCooldowns.put(material, currentTime);
        
        // Clean old cooldowns (older than 10 seconds) to prevent memory leaks
        cleanOldCooldowns(chestCooldowns, currentTime, cooldownTime * 3);
        
        // Get chest owner info
        UUID placerUUID = chest.getPlacer();
        String ownerName = null;
        
        if (placerUUID != null) {
            OfflinePlayer placer = Bukkit.getOfflinePlayer(placerUUID);
            if (placer.getName() != null) {
                ownerName = placer.getName();
            }
        }
        
        // Build the notification message
        String materialName = formatMaterialName(material);
        String message;
        
        if (ownerName != null) {
            message = plugin.getSettings().suctionNotificationsMessage
                    .replace("{material}", materialName)
                    .replace("{owner}", ownerName);
        } else {
            message = plugin.getSettings().suctionNotificationsMessageNoOwner
                    .replace("{material}", materialName);
        }
        
        message = ChatColor.translateAlternateColorCodes('&', message);
        
        // Send message to nearby players
        int radius = plugin.getSettings().suctionNotificationsRadius;
        for (Player player : location.getWorld().getPlayers()) {
            if (player.getLocation().distance(location) <= radius) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Formats material name to be more readable.
     */
    private String formatMaterialName(Material material) {
        String name = material.name().replace("_", " ");
        String[] words = name.split(" ");
        StringBuilder formattedName = new StringBuilder();
        
        for (String word : words) {
            if (formattedName.length() > 0) {
                formattedName.append(" ");
            }
            formattedName.append(word.substring(0, 1).toUpperCase())
                         .append(word.substring(1).toLowerCase());
        }
        
        return formattedName.toString();
    }

    /**
     * Cleans old cooldown entries to prevent memory leaks.
     */
    private void cleanOldCooldowns(Map<Material, Long> cooldowns, long currentTime, long maxAge) {
        cooldowns.entrySet().removeIf(entry -> 
            (currentTime - entry.getValue()) > maxAge
        );
    }

    /**
     * Cleans all cooldowns for a specific chest location.
     * Should be called when a chest is removed.
     */
    public void cleanChestCooldowns(Location chestLocation) {
        collectionCooldowns.remove(chestLocation);
    }

}
