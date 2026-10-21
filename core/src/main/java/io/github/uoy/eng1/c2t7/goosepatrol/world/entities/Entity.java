package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

/**
 * Anything that has a position and need to be updated
 * every frame. Every entity has a bounds represented 
 * by a circle.
 */
public abstract class Entity {
    private final Vector2 position;
    private final Vector2 velocity = new Vector2();
    private final Circle bounds;

    public Entity(Vector2 startPosition, float radius) {
        this.position = startPosition;
        this.bounds = new Circle(startPosition.x, startPosition.y, radius);
    }

    public Vector2 getPosition() {
        return position;
    }

    public Vector2 getVelocity() {
        return velocity;
    }

    public Circle getBounds() {
        return bounds;
    }

    public void syncBounds() {
        bounds.setPosition(position.x, position.y);
    }

    /** Set the velocity to whatever this entity wants to do this frame. */
    public abstract void updateIntent(float delta);
}
