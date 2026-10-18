package io.github.uoy.eng1.c2t7.goosepatrol.world.items;

import com.badlogic.gdx.math.Vector2;

/**
 * The set of things an {@link ItemEffect} is allowed to do. To add a new effect
 * add it hereand implement it in {@code GameWorld.WorldItemContext}.
 * ItemContext
 */
public interface ItemContext {
    /** Where the player is right now. */
    Vector2 getPlayerPosition();

    /**
     * A point {@code distance} units in front of the player, shortened if a wall is in
     * the way.
     */
    Vector2 throwTarget(float distance);

    /** 
     * Multiplies player speed by {@code multiplier} for {@code seconds}.
     */
    void speedBoost(float multiplier, float seconds);

    /** Every goose within {@code radius} of {@code point} stops what its doing and 
     * runs towards {@code point} and stays there for {@code seconds}.
     */
    void lure(Vector2 point, float radius, float seconds);

    /** Adds seconds on the game clock/ */
    void addTime(float seconds);
    
}
