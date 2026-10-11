package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameScreen implements Screen {
    public enum State {
        RUNNING, PAUSED
    }

    private final Main game;

    // World rendering.
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ExtendViewport viewport;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Texture playerTexture;
    private final float mapWidth;
    private final float mapHeight;

    // Hud rendering.
    private final ScreenViewport hudViewport = new ScreenViewport();

    // Physics.
    private final World world = new World(new Vector2(0, 0), true);
    private float accumulator = 0f;

    // Debug
    private final Box2DDebugRenderer debugRenderer = new Box2DDebugRenderer();
    private boolean debugMode = false;
    private static final String TAG = "GameScreen";

    // Player.
    private final Player player;
    private final Vector2 mouseWorld = new Vector2();

    // Game state.
    private State state = State.RUNNING;

    public GameScreen(Main game) {
        this.game = game;

        viewport = new ExtendViewport(Constants.VIEWPORT_WIDTH, Constants.VIEWPORT_HEIGHT, camera);

        // Map drawn at 1/PPM so one 32x32 tile is 1m
        map = new TmxMapLoader().load(Constants.MAP_FILE);
        mapRenderer = new OrthogonalTiledMapRenderer(map, 1 / Constants.PPM, game.batch);

        mapWidth = MapCollisionLoader.getMapWidth(map);
        mapHeight = MapCollisionLoader.getMapHeight(map);

        // Load in all the collisions
        MapCollisionLoader.createBodies(map, world);
        Gdx.app.log(TAG, "Loaded " + Constants.MAP_FILE + " (" + mapWidth + " x " + mapHeight + " m)");
        Gdx.app.debug(TAG, "Physics bodies: " + world.getBodyCount());

        playerTexture = new Texture(Constants.PLAYER_TEXTURE);
        player = new Player(world, Constants.PLAYER_START_X, Constants.PLAYER_START_Y);

        camera.position.set(player.getPosition().x, player.getPosition().y, 0);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        // Cap frame time so lag spikes don't mess up physics.
        delta = Math.min(delta, Constants.MAX_FRAME_TIME);

        if (handleInput()) {
            // Screen changed.
            return;
        }
        // -- Update --
        if (state == State.RUNNING) {
            player.setDirection(readDirectionX(), readDirectionY());
            player.update();
            mouseWorld.set(Gdx.input.getX(), Gdx.input.getY());
            viewport.unproject(mouseWorld);
            player.faceToward(mouseWorld, delta);
            stepPhysics(delta);
        }
        updateCamera();

        // -- Draw --
        ScreenUtils.clear(Color.BLACK);

        viewport.apply();
        mapRenderer.setView(camera);
        mapRenderer.render();

        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        player.render(game.batch, playerTexture);
        game.batch.end();

        if (debugMode) {
            debugRenderer.render(world, camera.combined);
            renderDebugOverlay();
        }

        if (state == State.PAUSED) {
            renderPauseOverlay();
        }
    }

    /**
     * Returns true if the screen was changed.
     */
    private boolean handleInput() {
        // Pause with ESCAPE.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (state == State.RUNNING) {
                pauseGame();
            } else {
                state = State.RUNNING;
                Gdx.app.log(TAG, "Resumed");
            }
        }
        // If paused, quit with Q.
        if (state == State.PAUSED && Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
            game.setScreen(new TitleScreen(game));
            return true;
        }
        // Enable debug mode with F3.
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            debugMode = !debugMode;
            Gdx.app.setLogLevel(debugMode ? Application.LOG_DEBUG : Application.LOG_INFO);
            Gdx.app.log(TAG, "Debug mode " + (debugMode ? "on" : "off"));
        }
        return false;
    }

    private void pauseGame() {
        state = State.PAUSED;
        Gdx.app.log(TAG, "Paused");
    }

    private float readDirectionX() {
        float x = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT))
            x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            x += 1;
        return x;

    }

    private float readDirectionY() {
        float y = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN))
            y -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP))
            y += 1;
        return y;
    }

    /**
     * Fixed timestep physics independent of frame time.
     */
    private void stepPhysics(float delta) {
        accumulator += delta;
        while (accumulator >= Constants.TIME_STEP) {
            world.step(Constants.TIME_STEP, 6, 2);
            accumulator -= Constants.TIME_STEP;
        }
    }

    /**
     * Follows player while staying in map.
     */
    private void updateCamera() {
        camera.position.x = clampAxis(player.getPosition().x, camera.viewportWidth, mapWidth);
        camera.position.y = clampAxis(player.getPosition().y, camera.viewportHeight, mapHeight);
        camera.update();
    }

    /**
     * Clamps a camera coordinate, centers map if smaller than view.
     */
    private static float clampAxis(float pos, float viewSize, float mapSize) {
        if (mapSize <= viewSize) {
            return mapSize / 2f;
        }
        return MathUtils.clamp(pos, viewSize / 2f, mapSize - viewSize / 2f);
    }

    private void renderPauseOverlay() {
        hudViewport.apply();
        game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
        game.batch.begin();
        float midX = hudViewport.getWorldWidth() / 2f;
        float midY = hudViewport.getWorldHeight() / 2f;
        game.font.draw(game.batch, "PAUSED", midX - 25, midY + 20);
        game.font.draw(game.batch, "ESC to resume, Q to return to title", midX - 100, midY - 10);
        game.batch.end();
    }

    private void renderDebugOverlay() {
        hudViewport.apply();
        game.batch.setProjectionMatrix(hudViewport.getCamera().combined);
        game.batch.begin();
        float top = hudViewport.getWorldHeight() - 10;
        Vector2 p = player.getPosition();
        game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, top);
        game.font.draw(game.batch, String.format("Player: %.2f, %.2f m", p.x, p.y), 10, top - 20);
        game.font.draw(game.batch, String.format("Mouse: %.2f, %.2f m", mouseWorld.x, mouseWorld.y), 10, top - 40);
        game.font.draw(game.batch, "State: " + state, 10, top - 60);
        game.font.draw(game.batch, "Physics bodies: " + world.getBodyCount(), 10, top - 80);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        viewport.update(width, height);
        hudViewport.update(width, height, true);
    }

    @Override
    public void pause() {
        if (state == State.RUNNING) {
            pauseGame();
        }
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
        mapRenderer.dispose();
        map.dispose();
        playerTexture.dispose();
        world.dispose();
    }
}