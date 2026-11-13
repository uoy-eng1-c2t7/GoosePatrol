package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
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
    private final float turnSpeed;
    private float speed;

    private float rotation;

    public Entity(Vector2 startPosition, float radius, float speed, float turnSpeed) {
        this.position = startPosition;
        this.turnSpeed = turnSpeed;
        this.speed = speed;
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

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) { this.speed = speed; }

    /** Direction last moved in, in degrees (0 = right, 90 = up). */
    public float getRotation() {
        return rotation;
    }

    /** Run after movement to update thigns like rotation, timers etc. */
    public void update(float delta) {
        if (!velocity.isZero()) {
            float targetAngle = velocity.angleDeg();
            float alpha = 1.0f - (float) Math.exp(-turnSpeed * delta);
            rotation = MathUtils.lerpAngleDeg(rotation, targetAngle, alpha);
        }
    }

    public void syncBounds() {
        bounds.setPosition(position.x, position.y);
    }

    /** True if this entity should pass through walls. */
    public boolean ignoresWalls() {
        return false;
    }

    /** Set the velocity to whatever this entity wants to do this frame. */
    public abstract void updateIntent(float delta);
}
