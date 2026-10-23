package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Inventory;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

public class Player extends Entity {
    private final Vector2 moveInput = new Vector2();
    private final Inventory inventory = new Inventory(9);
    private float stamina = 100f;
    private final float maxStamina = 100f;
    private boolean isSprinting = false;

    public Player(Vector2 startPosition, GameConfig.PlayerConfig config) {
        super(startPosition, config.radius, config.speed, config.turnSpeed);
    }

    public void setMoveInput(Vector2 direction) {
        moveInput.set(direction);
    }

    public void setSprinting(boolean sprinting) {
        this.isSprinting = sprinting && stamina > 5f;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public float getStaminaPercent() {
        return stamina / maxStamina;
    }

    @Override 
    public void update(float delta) {
        super.update(delta);

        if (isSprinting && !getVelocity().isZero()) {
            stamina = Math.max(0, stamina - delta * 35f);
            if (stamina <= 0) isSprinting = false;
        } else  {
            stamina = Math.min(maxStamina, stamina + delta * 20f);
        }
    }

    @Override
    public void updateIntent(float delta) {
        if (moveInput.isZero()) {
            getVelocity().setZero();
        } else {
            float currentSpeed = getSpeed() * (isSprinting ? 1.6f : 1.0f);
            getVelocity().set(moveInput).nor().scl(currentSpeed);
        }
    }
}
