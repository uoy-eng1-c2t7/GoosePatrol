package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Inventory;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

/**
 * The student the user controls: movement, sprint/stamina, hearts and the
 * inventory. Implements UR_PLY_MOVEMENT, FR-02, FR-15, FR-17.
 */
public class Player extends Entity {
    private final Vector2 moveInput = new Vector2();
    private final Inventory inventory = new Inventory(9);
    private float stamina = 100f;
    private final float maxStamina = 100f;
    private boolean isSprinting = false;
    private boolean exhausted = false;
    private int hearts;
    private final float invulnerableSeconds;
    private float invulnerableTimer;
    private final float sprintMultiplier;

    public Player(Vector2 startPosition, GameConfig.PlayerConfig config) {
        super(startPosition, config.radius, config.speed, config.turnSpeed);
        hearts = config.maxHearts;
        invulnerableSeconds = config.invulnerableSeconds;
        sprintMultiplier = config.sprintMultiplier;
    }

    public void setMoveInput(Vector2 direction) {
        moveInput.set(direction);
    }

    public void setSprinting(boolean sprinting) {
        if (stamina <= 5f)
            exhausted = true;
        if (!sprinting && stamina >= 25f)
            exhausted = false; // Must let go of sprint before it works again
        this.isSprinting = sprinting && !exhausted;
    }

    /**
     * Removes a heart unless the player was pecked very recently. Returns true if
     * it landed.
     */
    public boolean takePeck() {
        if (invulnerableTimer > 0f || hearts == 0) {
            return false;
        }
        hearts--;
        invulnerableTimer = invulnerableSeconds;
        return true;
    }

    public int getHearts() {
        return hearts;
    }

    public boolean isInvulnerable() {
        return invulnerableTimer > 0f;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public float getStaminaPercent() {
        return stamina / maxStamina;
    }

    public float getMaxStamina() {
        return maxStamina;
    }

    public void setStamina(float stamina) {
        this.stamina = stamina;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        invulnerableTimer = Math.max(0f, invulnerableTimer - delta);

        if (isSprinting && !getVelocity().isZero()) {
            stamina = Math.max(0, stamina - delta * 35f);
            if (stamina <= 0)
                isSprinting = false;
        } else {
            stamina = Math.min(maxStamina, stamina + delta * 20f);
        }
    }

    @Override
    public void updateIntent(float delta) {
        if (moveInput.isZero()) {
            getVelocity().setZero();
        } else {
            float currentSpeed = getSpeed() * (isSprinting ? sprintMultiplier : 1.0f);
            getVelocity().set(moveInput).nor().scl(currentSpeed);
        }
    }
}
