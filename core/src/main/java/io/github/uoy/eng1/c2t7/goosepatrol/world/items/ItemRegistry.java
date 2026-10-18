package io.github.uoy.eng1.c2t7.goosepatrol.world.items;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The one place every item in the game is defined.
 *
 * <h3>Adding a new item</h3>
 * Add one {@code register(...)} line in the static block below. That's it:
 * <ul>
 * <li>to start with it: put its id in {@code startingItems} in config.json</li>
 * <li>to find it on the map: add a point in the Tiled 'items' layer with a
 * custom
 * string property {@code itemType} = its id</li>
 * </ul>
 */
public final class ItemRegistry {
    private static final Map<String, ItemDefinition> ITEMS = new LinkedHashMap<>();

    static {
        // --- Lures ---
        register("bread", "Bread",
                "Throw it ahead. Nearby geese run over and eat it.",
                ctx -> ctx.lure(ctx.throwTarget(120f), 220f, 6f));

        register("chips", "Chips",
                "A big, tasty lure that keeps geese busy for longer.",
                ctx -> ctx.lure(ctx.throwTarget(140f), 300f, 9f));

        register("frisbee", "Frisbee",
                "Fling it far away. Geese chase it for a short while.",
                ctx -> ctx.lure(ctx.throwTarget(300f), 160f, 4f));

        // --- Buffs ---
        register("energy_drink", "Energy Drink",
                "Drink it for a big speed boost for 5 seconds.",
                ctx -> ctx.speedBoost(1.6f, 5f));

        register("pocket_watch", "Pocket Watch",
                "Somehow it turns the clock back 15 seconds.",
                ctx -> ctx.addTime(15f));
    }

    private ItemRegistry() {
    }

    private static void register(String id, String name, String description, ItemEffect effect) {
        if (ITEMS.containsKey(id)) {
            throw new IllegalStateException("Duplicate item id '" + id + "'");
        }
        ITEMS.put(id, new ItemDefinition(id, name, description, effect));
    }

    /** The item with this id, or null if there isn't one. */
    public static ItemDefinition get(String id) {
        return ITEMS.get(id);
    }

    /**
     * Like {@link #get} but fails loudly, so typos in config/map data are found
     * fast.
     */
    public static ItemDefinition require(String id) {
        ItemDefinition def = ITEMS.get(id);
        if (def == null) {
            throw new IllegalArgumentException(
                    "Unknown item id '" + id + "'. Known ids: " + ITEMS.keySet()
                            + ". Define new items in ItemRegistry.");
        }
        return def;
    }
}
