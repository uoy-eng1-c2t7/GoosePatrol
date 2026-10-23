package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Item;

public class DroppedItem extends Entity {
    private final Item item;
    private final int count;
    private final boolean isDistraction;
    private float pickupCooldown;
    private boolean destroyed = false;
    private float lifetime = 12.0f;

    /** Constructor for STATIC pickups placed on the map (never distracts geese) */
    public DroppedItem(Vector2 position, Item item, int count) {
        this(position, item, count, new Vector2(), false, 0f);
    }

    /** Constructor for THROWN items (distracts geese, slides to a halt) */
    public DroppedItem(Vector2 position, Item item, int count, Vector2 throwVelocity, boolean isDistraction,
            float pickupCooldown) {
        super(position, 8f, 0f, 0f); // 8px collision radius
        this.item = item;
        this.count = count;
        this.isDistraction = isDistraction;
        this.pickupCooldown = pickupCooldown;
        getVelocity().set(throwVelocity);
    }

    @Override
    public void updateIntent(float delta) {
        // Friction to smoothly slow down thrown items until they stop
        if (!getVelocity().isZero()) {
            getVelocity().scl((float) Math.exp(-5.0f * delta));
            if (getVelocity().len2() < 1f) {
                getVelocity().setZero();
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        if (pickupCooldown > 0) {
            pickupCooldown -= delta;
        }

        if (isDistraction) {
            lifetime -= delta;
            if (lifetime <= 0) {
                destroy();
            }
        }
    }

    public boolean canPickup() {
        return !isDistraction && pickupCooldown <= 0 && !destroyed;
    }

    public boolean isDistraction() {
        return isDistraction && !destroyed;
    }

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public void destroy() {
        this.destroyed = true;
    }

    public boolean isDestroyed() {
        return destroyed;
    }
}