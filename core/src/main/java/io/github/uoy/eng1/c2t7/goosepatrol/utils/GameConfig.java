package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class GameConfig {
    public int gameDurationSeconds;
    public float minLoadTimeMiliseconds;
    public String[] startingItems;
    public PlayerConfig player;
    public PhysicsConfig physics;

    public static class PlayerConfig {
        public float speed;
        public float radius;
    }

    public static class PhysicsConfig {
        public float pixelsPerMeter;
    }

    public static GameConfig load(FileHandle file) {
        GameConfig config = new Json().fromJson(GameConfig.class, file);
        config.validate();
        return config;
    }

    private void validate() {
        require(gameDurationSeconds > 0, "gameDurationSeconds");
        require(player != null && player.speed > 0 && player.radius > 0, "player.speed / player.radius");
        require(physics != null && physics.pixelsPerMeter > 0, "physics.pixelsPerMeter");
    }

    private static void require(boolean ok, String what) {
        if (!ok) {
            throw new IllegalStateException("config.json: missing or invalid " + what);
        }
    }
}
