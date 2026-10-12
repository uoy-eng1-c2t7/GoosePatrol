package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.entities.Entity;

public class PhysicsEngine {
    private final MapCollision collision;
    private final Array<Entity> entities;

    public PhysicsEngine(MapCollision collision, Array<Entity> entities) {
        this.collision = collision;
        this.entities = entities;
    }

    public void updatePhysics() {
        for (int i = 0; i < entities.size; i++) {
            Entity entity = entities.get(i);

            // X AXIS MOVEMENT
            if (entity.velocity.x != 0) {                       // any velocity?
                entity.position.x += entity.velocity.x;         // if so then move blindly
                entity.syncHithox();

                if (isColliding(entity)) {
                    entity.position.x -= entity.velocity.x;     // undo, entity hit something!
                    entity.syncHithox();
                }
            }

            // Y AXIS MOVEMENT
            if (entity.velocity.y != 0) {                       // any velocity?
                entity.position.y += entity.velocity.y;         // if so then move blindly
                entity.syncHithox();

                if (isColliding(entity)) {
                    entity.position.y -= entity.velocity.y;     // undo, entity hit something!
                    entity.syncHithox();
                }
            }
        }
    }

    private boolean isColliding(Entity movingEntity) {
        // Check map collision
        if (collision.hits(movingEntity.hitbox))        
            return true;

        // Check entity collision
        for (int i = 0; i < entities.size; i++) {
            Entity other = entities.get(i);
            if (other != movingEntity && Intersector.overlaps(movingEntity.hitbox, other.hitbox)) {
                return true;
            }
        }

        return false;
    }

}
