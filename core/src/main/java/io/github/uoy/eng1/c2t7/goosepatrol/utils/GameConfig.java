package io.github.uoy.eng1.c2t7.goosepatrol.utils;

public class GameConfig {
    public int gameDurationSeconds;
    public float minLoadTimeSeconds;
    public String[] startingItems;
    public PlayerConfig player;
    public GooseConfig goose;
    public CameraConfig camera;

    public static class PlayerConfig {
        public float speed;
        public float radius;
    }

    public static class GooseConfig {
        public float speed;
        public float radius;            // hitbox
        public float leashRadius;       // idle goose notices player inside this
        public float aggroRadius;       // chasing goose gives up beyond this
        public float maxChaseSeconds;   // longest single chase
        public float loseSightSeconds;  // gives up after this long without line of sight
        public float reaggroCooldown;   // ignores player this long after giving up
        public float wanderRadius;      // how far from home an idle goose strolls
        public float wanderPauseMin;
        public float wanderPauseMax;    
        public float wanderSpeedFactor; // fraction of speed to use while wandering
        public float repathSeconds;     // How often goose will ask for new route
    }

    public static class CameraConfig {
        public float viewWidth;
        public float viewHeight;
        public float followSpeed;
    }
}
