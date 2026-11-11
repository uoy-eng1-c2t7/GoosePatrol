package io.github.uoy.eng1.c2t7.goosepatrol.world;

import io.github.uoy.eng1.c2t7.goosepatrol.events.Event;

public interface WorldListener {
    default void onExitReached(boolean isChased) {

    }

    default void onInventoryChanged() {

    }

    default void onPlayerPecked() {

    }

    default void onGooseDispersed() {

    }

    default void onExitReached() {

    }

    default void onEventTriggered(Event event) {

    }
}
