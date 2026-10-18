package io.github.uoy.eng1.c2t7.goosepatrol.world.items;

import com.badlogic.gdx.math.Vector2;

/** An item lying on the map waiting to be picked up. Doesn't move, so it isn't an Entity. */
public class GroundItem {
    private final Vector2 position;
    private final ItemDefinition definition;
    private boolean collected;

    public GroundItem(Vector2 position, ItemDefinition definition) {
        this.position = new Vector2(position);
        this.definition = definition;
    }

    public Vector2 getPosition() {
        return position;
    }

    public ItemDefinition getDefinition() {
        return definition;
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }
}