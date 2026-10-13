package io.github.uoy.eng1.c2t7.goosepatrol.utils;

/**
 * Game-wide constants shared by more than one class.
 */
public final class Constants {
    private Constants() {}

    // --- Display ---
    //** Size of the visible world, used by ExtendViewport */
    public final static float VIRTUAL_WIDTH = 960;
    public final static  float VIRTUAL_HEIGHT = 540; 
    
    // --- Assets --- 
    public final static String MAP_FILE = "test.tmx";
    public final static String PLAYER_TEXTURE = "player.png";
    public final static String GOOSE_TEXTURE = "goose.png";

    // --- Movement (pixels per second) --- 
    public final static float PLAYER_SPEED = 200f;
    public final static float GOOSE_SPEED = 150f;

    // --- Hitboxes (rdius in pixels) ---
    public static final float PLAYER_HITBOX_RADIUS = 14f;
    public static final float GOOSE_HITBOX_RADIUS = 12f;

    // --- Geese AI tuning values ---
    public static final float AGGRO_RADIUS = 250f;          // player this close alerts goose
    public static final float LEASH_RADIUS = 450f;          // player this loses goose 
    public static final float MAX_CHASE_SECONDS = 10f;      // gives up after this, even if close
    public static final float REPATH_TIME = 0.4f;           // seconds between pathfinding search
    public static final float ARRIVE_DISTANCE = 6f;         // how close is close enough to rep
    public static final float HOME_ARRIVE_DISTANCE = 20f;   // how close is close enough to home
    public static final float WANDER_RADIUS = 80f;          // how far idle goose will walk
    public static final float WANDER_SPEED_FACTOR = 0.3f;   // idle walking slower than chasing
    public static final float WANDER_PAUSE_MIN = 1f;
    public static final float WANDER_PAUSE_MAX = 3f;
    public static final float WANDER_TIMEOUT = 4f;          // give up if cant reach wander spot
    public static final float LOSE_SIGHT_SECONDS = 2f;      // time until geese give up and go home after losing sight
    public static final float TRACK_SECONDS = 1.5f;         // after losing sight follows real player position (cheats) for this long
    public static final float REAGGRO_COOLDOWN = 1.5f;      // after giving up, ignores player this long
}   
