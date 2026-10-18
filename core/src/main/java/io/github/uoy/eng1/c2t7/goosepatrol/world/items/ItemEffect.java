package io.github.uoy.eng1.c2t7.goosepatrol.world.items;

/**
 * What an item does when the player uses it. Written as a lambda that only talks to 
 * the world through {@link ItemContext} so effects cant mess with the game state.
 */
@FunctionalInterface 
public interface ItemEffect {
    /** Runs once each time the item is used. */
    void apply(ItemContext ctx);
}
