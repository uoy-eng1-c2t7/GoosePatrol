package io.github.uoy.eng1.c2t7.goosepatrol.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;

public class Goose extends Entity {
    private enum State {
        IDLE, ANGRY, DISTRACTED
    }

    private final Entity target;
    private State state = State.ANGRY;
    private final Vector2 dir = new Vector2();

    public Goose(Vector2 startPosition, Entity target) {
        super(startPosition, new Texture(Constants.GOOSE_TEXTURE), Constants.GOOSE_SPEED);
        this.target = target;
    }

    @Override
    public void updateIntent(float delta) {
        velocity.set(0, 0);

        if (state != State.ANGRY) return;
        
        dir.set(target.position).sub(position);

        if (dir.len() > hitbox.radius + target.hitbox.radius) { // stop if touching
            velocity.set(dir).nor().scl(speed * delta);
            targetRotation = velocity.angleDeg();
        }
    }

}
