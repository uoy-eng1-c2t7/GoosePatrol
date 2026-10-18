package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.math.Rectangle;

/**
 * Invisible rectangle on the map that fires once when the player steps in.
 * Loaded from the Tiled 'events' layer; the object's custom string property
 * {@code effect} picks the {@link Effect}. What each effect does lives in
 * {@code GameWorld.applyEffect}.
 */
public class HiddenEvent {
    /** To add one: add a constant here, then a case in GameWorld.applyEffect. */
    public enum Effect {
        GOOSE_ALERT("A goose spotted you! Nearby geese are coming!"),
        FREE_BREAD("Someone dropped a sandwich! +2 Bread"),
        BONUS_TIME("A kind stranger tells you the bus is running late. +15s"),
        LOSE_TIME("You dropped your phone and had to go back for it. -10s"),
        SLIPPERY("Puddle! You slip and slow down for a few seconds.");

        private final String message;

        Effect(String message) {
            this.message = message;
        }

        /** Text shown to the player when this effect fires. */
        public String getMessage() {
            return message;
        }
    }

    private final String name;
    private final Rectangle area;
    private final Effect effect;
    private boolean triggered;

    public HiddenEvent(String name, Rectangle area, Effect effect) {
        this.name = name;
        this.area = area;
        this.effect = effect;
    }

    public String getName() {
        return name;
    }

    public Rectangle getArea() {
        return area;
    }

    public Effect getEffect() {
        return effect;
    }

    public boolean isTriggered() {
        return triggered;
    }

    public void markTriggered() {
        triggered = true;
    }
}
