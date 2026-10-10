package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Main gameplay screen.
 */
public class GameScreen  implements Screen {
    // Physics time step - independent from frame rate
    private static final float TIME_STEP = 1 / 60f;
    // Pixels per meter
    public static final float PPM = 100f;

    private final Main game;
    private OrthographicCamera camera;
    private Player player;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;
    private World world;
    private Box2DDebugRenderer debugRenderer;
    private float accumulator = 0f;


    public GameScreen(Main game) {
        this.game = game;

        // Intialise camera and set window size
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 400);

        // Setup Physics World
        world = new World(new Vector2(0, 0), true);
        debugRenderer = new Box2DDebugRenderer();

        // Setup Player in World
        player = new Player(world, 100, 100);

        // Setup Map 
        map = new TmxMapLoader().load("campus.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // -- Update logic first --

        updatePhysics(delta);

        player.update(delta);

        updateCamera(delta);

        // -- Then draw --
        ScreenUtils.clear(Color.BLACK);

        // Render map 
        mapRenderer.setView(camera);
        mapRenderer.render();

        // Render player ontop of map
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        player.render(game.batch);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    
    public static float pixelToMeters(float pixels) {
        return pixels / PPM;
    }

    public static float metersToPixels(float meters) {
        return meters * PPM;
    }

    private void updateCamera(float delta) {
        float targetX = metersToPixels(player.getPosition().x);
        float targetY = metersToPixels(player.getPosition().y);

        Vector3 targetPosition = new Vector3(targetX, targetY, 0);

        camera.position.lerp(targetPosition, 5f * delta);
        camera.update();
    }

    private void updatePhysics(float delta) {
        // Add time passed between last frame.
        accumulator += delta;

        // If enough time has passed, do physics step.
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }
    }

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        player.dispose();
    }
}
