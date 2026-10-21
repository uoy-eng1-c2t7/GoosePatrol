package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

/** 
 * Controls the main game camera, will always keep it 
 * in the map, follow the player.
 */
public class CameraController {
    private final OrthographicCamera camera;
    private Vector2 target;
    private float mapWidth;
    private float mapHeight;
    private float lerpSpeed;

    public CameraController(OrthographicCamera camera, float mapWidth, float mapHeight, GameConfig.CameraConfig config) {
        this.camera = camera;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
        this.lerpSpeed = config.lerpSpeed;
    }
    
    public void update(float delta) {
        if (target == null) return;

        // Lerp towards target
        float alpha = 1.0f - (float) Math.exp(-lerpSpeed * delta);
        camera.position.x += (target.x - camera.position.x) * alpha;
        camera.position.y += (target.y - camera.position.y) * alpha;

        // Clamp within map 
        clamp();

        camera.update(false);
    }

    private void clamp() {
        float halfViewportW = (camera.viewportWidth * camera.zoom) / 2f;
        float halfViewportH = (camera.viewportHeight * camera.zoom) / 2f;

        // If map wider than screen, clamp inside it, if not center it
        if (mapWidth > camera.viewportWidth * camera.zoom) {
            camera.position.x = MathUtils.clamp(camera.position.x, halfViewportW, mapWidth - halfViewportW);
        } else {
            camera.position.x = mapWidth / 2f;
        }

        if (mapHeight > camera.viewportHeight * camera.zoom) {
            camera.position.y = MathUtils.clamp(camera.position.y, halfViewportH, mapHeight - halfViewportH);
        } else {
            camera.position.y = mapHeight / 2f;
        }
    }

    /** Set the target for the camera to follow. */
    public void setTarget(Vector2 pos) {
        target = pos;
    }
}
