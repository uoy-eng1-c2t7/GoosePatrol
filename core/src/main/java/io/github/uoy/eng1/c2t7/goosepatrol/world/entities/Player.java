package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

public class Player extends Entity {
    private final Vector2 moveInput = new Vector2();
    private final float speed;

    public Player(Vector2 startPosition, GameConfig.PlayerConfig config) {
        super(startPosition, config.radius);
        this.speed = config.speed;
    }

    public void setMoveInput(Vector2 direction) {
        moveInput.set(direction);
    }

    @Override 
    public void updateIntent(float delta) {
        getVelocity().set(moveInput).nor().scl(speed);
    }
}
