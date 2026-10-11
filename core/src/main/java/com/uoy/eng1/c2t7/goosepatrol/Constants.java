package com.uoy.eng1.c2t7.goosepatrol;

/**
 * Game-wide constants. World units are meters, PPM is number of pixels
 * to meter, so one 32x32 tile is 1m.
 */
public class Constants {
    private Constants() {
    }

    public static final float PPM = 32f;
 
    // Minimum visible area in metres
    public static final float VIEWPORT_WIDTH = 20f;
    public static final float VIEWPORT_HEIGHT = 10f;
 
    // Physics
    public static final float TIME_STEP = 1 / 60f;
    public static final float MAX_FRAME_TIME = 0.25f;
 
    // Player 
    public static final float PLAYER_START_X = 3f;
    public static final float PLAYER_START_Y = 3f;
    public static final float PLAYER_RADIUS = 0.4f;
    public static final float PLAYER_SPEED = 6f;
 
    // Assets
    public static final String PLAYER_TEXTURE = "student.png";
    public static final String MAP_FILE = "campus.tmx";
}
