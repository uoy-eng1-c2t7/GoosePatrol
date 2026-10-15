package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    private Vector2 position;

    public Entity(Vector2 startPosition) {
        this.position = new Vector2(startPosition);
    }

    public Vector2 getPosition() {
        return position;
    }
}
