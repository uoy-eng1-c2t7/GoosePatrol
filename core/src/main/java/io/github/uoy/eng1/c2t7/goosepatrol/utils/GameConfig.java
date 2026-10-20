package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

public class GameConfig {
    public int gameDurationSeconds;
    public float minLoadTimeSeconds;
    public String[] startingItems;
    public PlayerConfig player;
    public AudioConfig audio;
    public CameraConfig camera;

    public static class PlayerConfig {
        public float speed;
        public float radius;
    }

    public static GameConfig load(FileHandle file) {
        GameConfig config = new Json().fromJson(GameConfig.class, file);
        config.validate();
        return config;
    }

    public static class AudioConfig {
        public float sfxVolume;
    }

    public static class CameraConfig {
        public float viewWidth;
        public float viewHeight;
    }

    private void validate() {
        require(gameDurationSeconds > 0, "gameDurationSeconds");
        require(player != null && player.speed > 0 && player.radius > 0, "player.speed / player.radius");
        require(audio.sfxVolume >= 0 && audio.sfxVolume <= 1, "sfxVolume");
    }

    private static void require(boolean ok, String what) {
        if (!ok) {
            throw new IllegalStateException("config.json: missing or invalid " + what);
        }
    }
}
