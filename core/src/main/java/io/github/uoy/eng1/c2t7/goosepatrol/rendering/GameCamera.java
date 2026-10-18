package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

/**
 * Smoothly follows afocus point and never shows outside of the map.
 */
public class GameCamera {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport;
    private final float mapWidth;
    private final float mapHeight;
    private final float followSpeed;

    public GameCamera(GameConfig.CameraConfig config, float mapWidth, float mapHeight) {
        this.viewport = new ExtendViewport(config.viewWidth, config.viewHeight, camera);
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.followSpeed = config.followSpeed;
    }

    /** Jumps straight to {@code focus}. */
    public void snapTo(Vector2 focus) {
        camera.position.set(focus.x, focus.y, 0);
        clampToMap();
    } 

    /** Eases towards {@code focus}. Call once per frame. */
    public void follow(Vector2 focus, float delta) {
        float t = Math.min(1f, followSpeed * delta);
        camera.position.x += (focus.x - camera.position.x) * t;
        camera.position.y += (focus.y - camera.position.y) * t;
        clampToMap();
    }

    private void clampToMap() {
        float halfW = viewport.getWorldWidth() / 2f;
        float halfH = viewport.getWorldHeight() / 2f;
        // If map smaller than view, center it
        camera.position.x = mapWidth <= halfW * 2 ? mapWidth / 2f : MathUtils.clamp(camera.position.x, halfW, mapWidth - halfW);
        camera.position.y = mapHeight <= halfH * 2 ? mapHeight / 2f : MathUtils.clamp(camera.position.y, halfH, mapHeight - halfH);
        // Round to the nearest whole pixel so tile edges always land on exact
        // pixel boundaries — prevents sub-pixel blending seams between tiles.
        camera.position.x = Math.round(camera.position.x);
        camera.position.y = Math.round(camera.position.y);
        camera.update();
    }

    /** Sets the viewport. Call before drawing the world. */
    public void apply() {
        viewport.apply();
        camera.update();
    }
    
    public void resize(int width, int height) {
        viewport.update(width, height);
        clampToMap();
    }

    public OrthographicCamera getCamera(){
        return camera;
    }
}
