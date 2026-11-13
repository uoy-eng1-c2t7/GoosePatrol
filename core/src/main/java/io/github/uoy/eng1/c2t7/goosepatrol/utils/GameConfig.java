package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;

/**
 * Tuning values loaded from assets/config.json and validated at startup, so
 * balance can change without recompiling (QR-06).
 */
public class GameConfig {
    public int gameDurationSeconds;
    public float minLoadTimeSeconds;
    public int geeseToDisperse;
    public String[] startingItems;
    public PlayerConfig player;
    public GooseConfig goose;
    public AudioConfig audio;
    public CameraConfig camera;

    public static class PlayerConfig {
        public float speed;
        public float radius;
        public float turnSpeed;
        public int maxHearts;
        public float invulnerableSeconds;
        public float sprintMultiplier;
    }

    public static class GooseConfig {
        public float speed;
        public float radius;
        public float turnSpeed;
        public float sightRange;
        public float loseSightRange;
        public float territoryRadius;
        public float giveUpSeconds;
        public float alertRange;
        public float leaveSeconds;
        public int geesePerFood;
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
        public float lerpSpeed;
    }

    private void validate() {
        require(gameDurationSeconds > 0, "gameDurationSeconds");
        require(player != null && player.speed > 0 && player.radius > 0, "player.speed / player.radius");
        require(audio.sfxVolume >= 0 && audio.sfxVolume <= 1, "sfxVolume");
        require(player.maxHearts >= 1, "maxHearts");
        require(player.invulnerableSeconds >= 0.1, "invulnerableSeconds");
        require(player.sprintMultiplier > 1, "sprintMultiplier");
        require(geeseToDisperse >= 1, "geeseToDisperse");
        require(goose.leaveSeconds > 0 && goose.geesePerFood >= 1, "goose.leaveSeconds / goose.geesePerFood");
    }

    private static void require(boolean ok, String what) {
        if (!ok) {
            throw new IllegalStateException("config.json: missing or invalid " + what);
        }
    }
}
