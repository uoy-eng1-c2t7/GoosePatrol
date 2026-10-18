package io.github.uoy.eng1.c2t7.goosepatrol.world.Physics;

import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;

/** Applied entity velocities and keeps entities out of walls. */
public class PhysicsEngine {
    private final MapCollision collision;

    public PhysicsEngine(MapCollision collision) {
        this.collision = collision;
    }

    public void step(Array<Entity> entities, float delta) {
        for (Entity e : entities) {
            e.getPosition().mulAdd(e.getVelocity(), delta);
            collision.pushOut(e.getPosition(), e.getRadius());
        }
    }
}
