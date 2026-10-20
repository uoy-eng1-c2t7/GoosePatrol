package io.github.uoy.eng1.c2t7.goosepatrol.utils;

public interface WorldListener {
    default void onEventTriggered(String message) {
    }
    default void onPlayerPecked() {
        
    }
}
