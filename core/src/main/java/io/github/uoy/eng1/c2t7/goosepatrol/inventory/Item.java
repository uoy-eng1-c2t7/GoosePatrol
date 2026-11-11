package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;

/** Rperesnets one invenory item */
public class Item {
    @FunctionalInterface
    public interface ItemBehavior {
        /**
         * Triggered when the player uses the item.
         *
         * @return true if the item should be consumed, false otherwise.
         */
        boolean onUse(GameWorld world, Player player);
    }

    private final String id;
    private final String displayName;
    private final String spriteName;
    private final int maxStack;
    private final ItemBehavior behavior;

    public Item(String id, String displayName, String spriteName, int maxStack, ItemBehavior behavior) {
        this.id = id;
        this.displayName = displayName;
        this.spriteName = spriteName;
        this.maxStack = maxStack;
        this.behavior = behavior;
    }

    public boolean use(GameWorld world, Player player) {
        if (behavior == null || !behavior.onUse(world, player)) return false;
        world.setItemsUsed(world.getItemsUsed() + 1);
        return true;
    }

    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getSpriteName() {
        return spriteName;
    }

    public int getMaxStack() {
        return maxStack;
    }
}
