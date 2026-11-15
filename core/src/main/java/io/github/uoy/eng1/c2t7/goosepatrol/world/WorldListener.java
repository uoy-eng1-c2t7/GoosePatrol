package io.github.uoy.eng1.c2t7.goosepatrol.world;

import io.github.uoy.eng1.c2t7.goosepatrol.events.Event;

/**
 * Callbacks the world uses to tell the screen something happened, so the world
 * never depends on UI code.
 */
public interface WorldListener {

    // jj hb
    default void onInventoryChanged() {

    }

    default void onPlayerPecked() {

    }

    default void onGooseDispersed() {

    }

    /**
     * Called once when the player reaches the bus with enough geese dispersed
     * (FR-11).
     */
    default void onExitReached() {

    }

    default void onEventTriggered(Event event) {

    }
}
