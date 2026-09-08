package com.bgsoftware.wildchests.utils;

import com.bgsoftware.wildchests.WildChestsPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class CrafterChainUtils {

    private CrafterChainUtils() {
    }

    public static boolean isChainEntry(String entry) {
        return entry != null && entry.contains(">");
    }

    public static Optional<List<Material>> parseChain(String entry) {
        if (!isChainEntry(entry))
            return Optional.empty();

        String[] parts = entry.split(">");
        List<Material> chain = new ArrayList<>();

        for (String part : parts) {
            String materialName = part.trim();
            if (materialName.isEmpty())
                return Optional.empty();

            if (materialName.contains(":"))
                materialName = materialName.split(":")[0];

            Material material = Material.matchMaterial(materialName.toUpperCase(Locale.ENGLISH));
            if (material == null) {
                try {
                    material = Material.valueOf(materialName.toUpperCase(Locale.ENGLISH));
                } catch (IllegalArgumentException error) {
                    return Optional.empty();
                }
            }

            chain.add(material);
        }

        if (chain.size() < 2)
            return Optional.empty();

        return Optional.of(chain);
    }

    public static boolean isRecipeAllowedForChain(Recipe recipe, List<Material> chain) {
        Map<Material, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < chain.size(); i++)
            indexMap.put(chain.get(i), i);

        Material resultType = recipe.getResult().getType();
        Integer resultIndex = indexMap.get(resultType);
        if (resultIndex == null)
            return false;

        List<RecipeUtils.RecipeIngredient> ingredients = RecipeUtils.getIngredients(recipe);
        if (ingredients.isEmpty())
            return false;

        int maxIngredientIndex = -1;

        for (RecipeUtils.RecipeIngredient ingredient : ingredients) {
            for (ItemStack itemStack : ingredient.getIngredients()) {
                Integer ingredientIndex = indexMap.get(itemStack.getType());
                if (ingredientIndex == null)
                    return false;

                maxIngredientIndex = Math.max(maxIngredientIndex, ingredientIndex);
            }
        }

        return resultIndex > maxIngredientIndex;
    }

    public static int getChainResultIndex(Recipe recipe, List<Material> chain) {
        Map<Material, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < chain.size(); i++)
            indexMap.put(chain.get(i), i);

        Integer index = indexMap.get(recipe.getResult().getType());
        return index == null ? Integer.MAX_VALUE : index;
    }

    public static int getProcessOrder(Recipe recipe) {
        return getTier(recipe.getResult().getType());
    }

    public static int getTier(Material material) {
        String name = material.name();

        if (name.endsWith("_NUGGET"))
            return 0;
        if (name.endsWith("_BLOCK"))
            return 2;
        if (name.endsWith("_INGOT"))
            return 1;

        return Integer.MAX_VALUE - 1;
    }

    public static void logInvalidChain(String entry) {
        WildChestsPlugin.log("Found an invalid crafter chain entry for auto-crafter: " + entry + " - skipping...");
    }

}
