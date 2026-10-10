package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

/**
 * Main gameplay screen.
 */
public class GameScreen  implements Screen {
    private final Main game;
    private OrthographicCamera camera;
    private Player player;
    private TiledMap map;
    private OrthogonalTiledMapRenderer mapRenderer;

    public GameScreen(Main game) {
        this.game = game;

        // Intialise camera and set window size
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 400);

        // Setup Player
        player = new Player(100, 100);

        // Setup Map 
        map = new TmxMapLoader().load("campus.tmx");
        mapRenderer = new OrthogonalTiledMapRenderer(map);
    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        // -- Update logic --

        // Update player position
        player.update(delta);

        // Make camera follow player
        camera.position.set(player.x, player.y, 0);
        camera.update();

        // -- Drawing --

        ScreenUtils.clear(Color.BLACK);

        // Render map first
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

    @Override
    public void dispose() {
        map.dispose();
        mapRenderer.dispose();
        player.dispose();
    }
    
}
