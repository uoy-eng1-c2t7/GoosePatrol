package io.github.uoy.eng1.c2t7.goosepatrol.world;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.WorldListener;

public class GameWorld {
    public enum Status {
        RUNNING, 
        WON,
        LOST
    }

    private final WorldListener listener;
    private final GameConfig config;

    public GameWorld(GameConfig config, WorldListener listener) {
        this.config = config;
        this.listener = listener;
    }
}
