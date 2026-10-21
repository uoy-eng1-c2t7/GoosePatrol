package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.CameraController;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.DebugRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.WorldRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.WorldListener;

public class GameScreen implements Screen, WorldListener {
    private final GoosePatrol game;
    private final GameWorld world;
    private final WorldRenderer renderer;
    private final DebugRenderer debugRenderer;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final CameraController cameraController;
    private final ExtendViewport viewport;
    private final Vector2 moveInput = new Vector2();

    public GameScreen(GoosePatrol game) {
        this.game = game;
        GameConfig config = game.getConfig();

        world = new GameWorld(config, this, game.getAssets().getMap());
        renderer = new WorldRenderer(world, game.getAssets(), game.getBatch());
        debugRenderer = new DebugRenderer(world, game.getBatch());
        viewport = new ExtendViewport(config.camera.viewWidth, config.camera.viewHeight, camera);
        this.cameraController = new CameraController(camera, world.getMap().getWidth(), world.getMap().getHeight(), config.camera);
    }

    @Override
    public void show() {
        cameraController.setTarget(world.getPlayer().getPosition());
        debugRenderer.toggle();
    }

    @Override
    public void render(float delta) {
        // Pass input to world
        moveInput.set(
                (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0),
                (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0));
        world.update(delta, moveInput);

        // Camera follows player
        cameraController.update(delta);

        // Draw
        ScreenUtils.clear(Color.BLACK);
        renderer.render(camera);
        debugRenderer.render(camera);
    }

    @Override
    public void resize(int width, int height) {
        // If window is minimised it is 0, so don't do anything
        if (width <= 0 || height <= 0)
            return;
        viewport.update(width, height);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        renderer.dispose();
    }
}
