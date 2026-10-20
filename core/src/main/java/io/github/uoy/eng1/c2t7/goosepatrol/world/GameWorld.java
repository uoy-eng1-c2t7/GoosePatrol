package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;

public class GameWorld {
    public enum Status {
        RUNNING, 
        WON,
        LOST
    }

    private final WorldListener listener;
    private final GameConfig config;
    private final Player player;

    public GameWorld(GameConfig config, WorldListener listener) {
        this.config = config;
        this.listener = listener;
        this.player = new Player(new Vector2(100, 100), config.player);
    }

    public void update(float delta, Vector2 moveInput) {
        player.setMoveInput(moveInput);
    }
}
