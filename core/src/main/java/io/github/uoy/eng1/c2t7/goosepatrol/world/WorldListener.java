package io.github.uoy.eng1.c2t7.goosepatrol.world;

public interface WorldListener {
    default void onExitReached(boolean isChased) {

    }

    default void onInventoryChanged() {

    }

    default void onPlayerPecked() {

    }

    default void onGooseDispersed() {

    }
}
