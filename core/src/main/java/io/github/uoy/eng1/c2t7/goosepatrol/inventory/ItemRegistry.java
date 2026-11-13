package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import java.util.HashMap;
import java.util.Map;

/**
 * Every item in the game, keyed by id. Ids match object names in the Tiled
 * entities layer and config.json startingItems. Implements FR-07, FR-20, FR-21.
 */
public class ItemRegistry {
    private static final Map<String, Item> ITEMS = new HashMap<>();

    public static final Item BREAD = register(new Item(
            "bread", "Bread", "bread", 16,
            (world, player) -> {
                world.throwFood(player.getPosition(), player.getRotation(), ItemRegistry.get("bread"), 6f, 260f, 2.5f);
                return true;
            }));

    public static final Item ENERGY_DRINK = register(new Item(
            "energy_drink", "Energy Drink", "energy_drink", 5,
            (world, player) -> {
                if (player.getStaminaPercent() >= 1f) {
                    return false; // Already full, don't waste it
                }
                player.setStamina(player.getMaxStamina());
                return true;
            }));

    public static Item register(Item item) {
        ITEMS.put(item.getId(), item);
        return item;
    }

    public static Item get(String id) {
        return ITEMS.get(id);
    }
}
