package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

/**
 * Anything that can moves and makes decisions.
 * <p>
 * An entity never moves itself, each frame it states where it wants to go with
 * {@link #steer} or {@link #stop} and then {@code PhysicsEngine} applies the
 * velocity and keeps it out of walls and in the map.
 */
public abstract class Entity {
    private final Vector2 position;
    private final Vector2 velocity = new Vector2();
    private final float speed;
    private final float radius;
    private float rotation;

    public Entity(Vector2 startPosition, float speed, float radius) {
        this.position = new Vector2(startPosition);
        this.speed = speed;
        this.radius = radius;
    }

    /** Treat as read only, only PhysicsEngine should modify it. */
    public Vector2 getPosition() {
        return position;
    }

    /** Set this through {@link #steer}, only physics should read it. */
    public Vector2 getVelocity() {
        return velocity;
    }

    /**
     * Top speed in world units per second, subclasses can override to apply buffs.
     */
    public float getSpeed() {
        return speed;
    }

    /** Hitbox radius. */
    public float getRadius() {
        return radius;
    }

    /** Direction last moved in, in degrees (0 = right, 90 = up) */
    public float getRotation() {
        return rotation;
    }

    /** Decide what to do this frame by calling {@link #steer} or {@link #stop}. */
    public abstract void updateIntent(float delta);

    /** Runs after movement, updates things like rotation, timers etc. */
    public void update(float delta) {
        if (!velocity.isZero()) {
            rotation = velocity.angleDeg();
        }
    }

    /**
     * Head in {@code direction} at
     * {@code moveSpeed. Only the direction of the vector matters. }
     */
    protected void steer(Vector2 direction, float moveSpeed) {
        velocity.set(direction).nor().scl(moveSpeed);
    }

    protected void stop() {
        velocity.setZero();
    }
}
