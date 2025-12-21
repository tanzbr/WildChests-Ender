package com.bgsoftware.wildchests.api.objects.data;

import com.bgsoftware.wildchests.api.key.Key;
import com.bgsoftware.wildchests.api.objects.DepositMethod;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import com.bgsoftware.wildchests.api.objects.ChestType;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ChestData is an object to store all chest settings from config.
 */
public interface ChestData {

    /**
     * Get the name of the chest.
     */
    String getName();

    /**
     * Get the item-stack of the chest.
     */
    ItemStack getItemStack();

    /**
     * Get the type of the chest.
     */
    ChestType getChestType();

    /**
     * Get the default size for pages for the chest.
     */
    int getDefaultSize();

    /**
     * Get the default title for pages for the chest.
     */
    String getDefaultTitle();

    /**
     * Get a title for a specific page.
     * @param page The page to check.
     */
    String getTitle(int page);

    /**
     * Whether or not the chest has sell-mode enabled.
     */
    boolean isSellMode();

    /**
     * Whether or not the chest has trash-mode enabled.
     */
    boolean isTrashMode();

    /**
     * Get the trash interval in seconds for trash-mode chests.
     */
    int getTrashIntervalSeconds();

    /**
     * Whether or not the chest has hopper-filter mode enabled for auto crafters.
     */
    boolean isHopperFilter();

    /**
     * Whether or not the chest has auto-crafting mode enabled.
     */
    boolean isAutoCrafter();

    /**
     * Get all the recipes for the auto-crafter.
     */
    Iterator<Recipe> getRecipes();

    /**
     * Check if an item can be crafted by the chest.
     */
    boolean containsRecipe(ItemStack result);

    /**
     * Get all the pages's data.
     */
    Map<Integer, InventoryData> getPagesData();

    /**
     * Get the default amount of pages.
     */
    int getDefaultPagesAmount();

    /**
     * Get the default multiplier for the chest.
     */
    double getMultiplier();

    /**
     * Check whether or not this chest has auto-collect enabled.
     */
    boolean isAutoCollect();

    /**
     * Check whether or not this chest has suction-mode enabled.
     */
    boolean isAutoSuction();

    /**
     * Get the suction-mode's check range.
     */
    int getAutoSuctionRange();

    /**
     * Check whether or not this chest has it's suction-mode restricted to it's chunk.
     */
    boolean isAutoSuctionChunk();

    /**
     * Check whether or not this chest has instant-collection enabled.
     * When enabled, items are collected immediately on spawn, preventing entity creation.
     */
    boolean isInstantCollection();

    /**
     * Get the deposit method.
     */
    DepositMethod getDepositMethod();

    /**
     * Get all the blacklisted items.
     */
    Set<Key> getBlacklisted();

    /**
     * Get all the whitelisted items.
     */
    Set<Key> getWhitelisted();

    /**
     * Get the default max amount for storage chest.
     */
    BigInteger getStorageUnitMaxAmount();

    /**
     * Get the particles of this chest.
     */
    List<String> getChestParticles();

    /**
     * Get the price boosts for sell-mode chests.
     * Returns a map of material names to their price multipliers.
     * For example, "CACTUS" -> 2.0 means cactus prices are doubled.
     * @return Map of material names to multipliers
     */
    Map<String, Double> getPriceBoosts();

    /**
     * Get the price boost for a specific item.
     * @param itemStack The item to check
     * @return The multiplier for this item, or 1.0 if no boost is configured
     */
    double getPriceBoost(ItemStack itemStack);

    /**
     * Check if cactus nerf is enabled for this chest.
     * When enabled, naturally grown cactus has 75% chance to be collected with instant-collection.
     * Player-dropped cactus always has 100% collection rate.
     * @return true if cactus nerf is enabled
     */
    boolean isCactusNerfEnabled();

    /**
     * Set the default size of the pages.
     * @param size The size to set.
     */
    void setDefaultSize(int size);

    /**
     * Set the default title of the pages.
     * @param title The title to set.
     */
    void setDefaultTitle(String title);

    /**
     * Set the sell mode status for this chest.
     * @param sellMode The new status.
     */
    void setSellMode(boolean sellMode);

    /**
     * Set the trash mode status for this chest.
     * @param trashMode The new status.
     */
    void setTrashMode(boolean trashMode);

    /**
     * Set the trash interval in seconds for this chest.
     * @param trashIntervalSeconds The new interval in seconds.
     */
    void setTrashIntervalSeconds(int trashIntervalSeconds);

    /**
     * Set the deposit method for this chest.
     * @param depositMethod The new status.
     */
    void setDepositMethod(DepositMethod depositMethod);

    /**
     * Set whether or not the chest should have an hopper filter.
     * @param hopperFilter The hopper-filter status
     */
    void setHopperFilter(boolean hopperFilter);

    /**
     * Set the recipes for the auto-crafter.
     * If the provided recipes are empty, the chest will no longer be an auto-crafter.
     * Otherwise, it will have the provided recipes.
     * @param recipes The recipes to set.
     */
    void setAutoCrafter(List<String> recipes);

    /**
     * Set a pages data for the chest.
     * @param pagesData The new data.
     */
    void setPagesData(Map<Integer, InventoryData> pagesData);

    /**
     * Set the amount of default pages.
     * @param defaultPagesAmount The new amount.
     */
    void setDefaultPagesAmount(int defaultPagesAmount);

    /**
     * Set the mutliplier of the chest.
     * @param multiplier The new multiplier
     */
    void setMultiplier(double multiplier);

    /**
     * Set whether or not this chest should have auto-collect enabled.
     * @param autoCollect The new auto-collect status
     */
    void setAutoCollect(boolean autoCollect);

    /**
     * Set the auto suction range for this chest.
     * @param autoSuctionRange The new range.
     */
    void setAutoSuctionRange(int autoSuctionRange);

    /**
     * Set whether or not the suction should be restricted to the chest's chunk.
     * @param autoSuctionChunk The new status.
     */
    void setAutoSuctionChunk(boolean autoSuctionChunk);

    /**
     * Set whether or not this chest should have instant-collection enabled.
     * @param instantCollection The new instant-collection status
     */
    void setInstantCollection(boolean instantCollection);

    /**
     * Set the blacklisted items for the chest.
     * @param blacklisted The new blacklisted items.
     */
    void setBlacklisted(Set<Key> blacklisted);

    /**
     * Set the whitelisted items for the chest.
     * @param whitelisted The new whitelisted items.
     */
    void setWhitelisted(Set<Key> whitelisted);

    /**
     * Set the max amount of the storage-chest's item for this chest.
     * @param maxAmount The new max amount
     */
    void setStorageUnitMaxAmount(BigInteger maxAmount);

    /**
     * Set a list of particles for the chest.
     * @param particles The new particles
     */
    void setParticles(List<String> particles);

    /**
     * Set the price boosts for sell-mode chests.
     * @param boosts List of boost strings in format "MATERIAL:MULTIPLIER" (e.g., "CACTUS:2.0")
     */
    void setPriceBoosts(List<String> boosts);

    /**
     * Set whether cactus nerf is enabled for this chest.
     * When enabled, naturally grown cactus has 75% chance to be collected.
     * Player-dropped cactus always has 100% collection rate.
     * @param cactusNerf true to enable cactus nerf
     */
    void setCactusNerf(boolean cactusNerf);

}
