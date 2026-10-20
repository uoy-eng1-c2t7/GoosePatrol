package io.github.uoy.eng1.c2t7.goosepatrol.entities;

import com.badlogic.gdx.math.Vector2;

/**
 * Anything that has a position and need to be updated
 * every frame.
 */
public abstract class Entity {
    private final Vector2 position;
    private final Vector2 velocity = new Vector2();
    private final float radius;

    public Entity(Vector2 startPosition, float radius) {
        this.position = startPosition;
        this.radius = radius;
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public float getRadius() {
        return radius;
    }

    /** Set the velocity to whatever this entity wants to do this frame. */
    public abstract void updateIntent(float delta);
}
