package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class GameCamera {
    private final OrthographicCamera camera = new OrthographicCamera();
    private final Viewport viewport;
    private final float mapWidth;
    private final float mapHeight;

    private final Vector2 focus = new Vector2();
    private boolean followTarget = true;
    private float panTimer;

    public GameCamera(float mapWidth, float mapHeight) {
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        viewport = new ExtendViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
    }

    public void snapTo(Vector2 position) {
        camera.position.set(position.x, position.y, 0);
    }

    public void panTo(float x, float y, float seconds) {
        focus.set(x, y);
        followTarget = false;
        panTimer = seconds;
    }

    public void update(float delta, Vector2 target) {
        if (!followTarget) {
            panTimer -= delta;
            if (panTimer <= 0) followTarget = true;
        }

        float targetX = followTarget ? target.x : focus.x;
        float targetY = followTarget ? target.y : focus. y;

        float halfWidth = camera.viewportWidth * camera.zoom / 2f;
        float halfHeight = camera.viewportHeight * camera.zoom / 2f;

        float smoothing = 1f - (float) Math.exp(-4f * delta); // lower = slower pan
        float x =  camera.position.x + (targetX - camera.position.x) * smoothing;
        float y =  camera.position.y + (targetY - camera.position.y) * smoothing;

        camera.position.x = clampAxis(x, halfWidth, mapWidth);
        camera.position.y = clampAxis(y, halfHeight, mapHeight);
        camera.update();
    }

    private float clampAxis(float target, float half, float mapSize) {
        if (mapSize <= half * 2f) return mapSize / 2f; // view bigger than map so center it 
        return MathUtils.clamp(target, half, mapSize - half);
    }


    /**
     * Fills {@code out} with the world space rectangle the camera can see.
     */
    public Rectangle getViewBounds(Rectangle out) {
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        return out.set(camera.position.x - width / 2f, camera.position.y - height / 2f, width, height);
    }

    public void apply() {
        viewport.apply();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    public OrthographicCamera getCamera() {
        return camera;
    }
}
