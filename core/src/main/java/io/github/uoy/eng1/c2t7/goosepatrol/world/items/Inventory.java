package io.github.uoy.eng1.c2t7.goosepatrol.world.items;

import com.badlogic.gdx.utils.Array;

/**
 * The player's items grouped into stacks. A stack's position in the
 * list is its slot, shown as keys 1-9 in the HUD.
 */
public class Inventory {
    /** One slot per number key */
    public static final int MAX_SLOTS = 9;

    public static final class Stack {
        private final ItemDefinition definition;
        private int count;

        private Stack(ItemDefinition definition, int count) {
            this.definition = definition;
            this.count = count;
        }

        public ItemDefinition getDefinition() {
            return definition;
        }

        public int getCount() {
            return count;
        }
    }

    private final Array<Stack> stacks = new Array<>();

    /**
     * Adds items, stacking them if you already have the same type.
     * 
     * @return false if no free slot
     */
    public boolean add(ItemDefinition definition, int amount) {
        for (Stack s : stacks) {
            if (s.definition == definition) {
                s.count += amount;
                return true;
            }
        }
        if (stacks.size >= MAX_SLOTS) {
            return false;
        }
        stacks.add(new Stack(definition, amount));
        return true;

    }

    /** The stack in {@code slot}, or null if the slot is empty. */
    public Stack get(int slot) {
        return (slot >= 0 && slot < stacks.size) ? stacks.get(slot) : null;
    }

    /**
     * Takes one item out of {@code slot}. Empty stacks disappear and later slots
     * shift up.
     */
    public void removeOne(int slot) {
        Stack s = get(slot);
        if (s == null) {
            return;
        }
        s.count--;
        if (s.count <= 0) {
            stacks.removeIndex(slot);
        }
    }

    /** Number of occupied slots. */
    public int size() {
        return stacks.size;
    }
}
