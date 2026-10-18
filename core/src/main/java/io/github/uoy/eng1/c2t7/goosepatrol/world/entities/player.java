package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

/** The student. Moves in whichever direction the input vector points/ */
public class Player extends Entity {
    private final Vector2 moveInput = new Vector2();
    private float speedMultiplier = 1f;
    private float modifierSecondsLeft;

    public Player(Vector2 startPosition, GameConfig.PlayerConfig config) {
        super(startPosition, config.speed, config.radius);
    }

    /** Set by GameScreen from the keyboard. */
    public void setMoveInput(Vector2 direction) {
        moveInput.set(direction);
    }

    /** Scales spped by {@code multiplier} for {@code seconds}. */
    public void applySpeedModifier(float multiplier, float seconds) {
        speedMultiplier = multiplier;
        modifierSecondsLeft = seconds;
    }

    /** True while a speed modifier is active. */
    public boolean hasSpeedModifier() {
        return modifierSecondsLeft > 0;
    }

    @Override 
    public float getSpeed() {
        return super.getSpeed() * speedMultiplier;
    }

    @Override 
    public void updateIntent(float delta) {
        steer(moveInput, getSpeed()); 
    }

    @Override 
    public void update(float delta) {
        super.update(delta);
        if (modifierSecondsLeft > 0) {
            modifierSecondsLeft -= delta;
            if (modifierSecondsLeft <= 0) {
                speedMultiplier = 1f;
            }
        }
    }
}
