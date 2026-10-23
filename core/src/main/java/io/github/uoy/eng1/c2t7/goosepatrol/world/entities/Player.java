package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Inventory;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

public class Player extends Entity {
    private final Vector2 moveInput = new Vector2();
    private final Inventory inventory = new Inventory(9);

    public Player(Vector2 startPosition, GameConfig.PlayerConfig config) {
        super(startPosition, config.radius, config.speed, config.turnSpeed);
    }

    public void setMoveInput(Vector2 direction) {
        moveInput.set(direction);
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void updateIntent(float delta) {
        if (moveInput.isZero()) {
            getVelocity().setZero();
        } else {
            getVelocity().set(moveInput).nor().scl(getSpeed());
        }
    }
}
