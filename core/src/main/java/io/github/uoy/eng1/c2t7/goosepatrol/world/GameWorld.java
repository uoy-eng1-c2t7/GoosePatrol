package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;

public class GameWorld {
    private final GoosePatrol game;
    private final Player player;

    public GameWorld(GoosePatrol game) {
        this.game = game;

        player = new Player(new Vector2(100, 100));
    }
}
