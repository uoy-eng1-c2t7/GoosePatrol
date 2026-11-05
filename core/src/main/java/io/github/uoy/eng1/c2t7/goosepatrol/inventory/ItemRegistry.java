package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import java.util.HashMap;
import java.util.Map;

public class ItemRegistry {
    private static final Map<String, Item> ITEMS = new HashMap<>();

    public static final Item BREAD = register(new Item(
            "bread", "Bread", "bread.png", 16,
            (world, player) -> {
                world.throwFood(player.getPosition(), player.getRotation(), ItemRegistry.get("bread"), 8f, 180f, 3.0f);
                return true;
            }));

    public static final Item ENERGY_DRINK = register(new Item(
            "energy_drink", "Energy Drink", "energy_drink.png", 5,
            (world, player) -> {
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