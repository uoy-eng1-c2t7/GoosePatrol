package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Player;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameCamera;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapLoader;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.PhysicsEngine;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.WorldRenderer;

/** The main gameplay screen */
public class GameScreen implements Screen {
    public enum State {
        RUNNING, PAUSED
    }

    private final GoosePatrol game;
    private final Array<Entity> entities = new Array<>();
    private final MapLoader mapLoader;
    private final GameCamera gameCamera;
    private Player player;
    private State state = State.RUNNING;
    private final PhysicsEngine physicsEngine;
    private final WorldRenderer worldRenderer;

    public GameScreen(GoosePatrol game) {
        this.game = game;

        mapLoader = new MapLoader(Constants.MAP_FILE);
        gameCamera = new GameCamera(mapLoader.getWidth(), mapLoader.getHeight());

        spawnEntities();

        physicsEngine = new PhysicsEngine(mapLoader.getCollision(), entities);
        worldRenderer = new WorldRenderer(game, gameCamera, mapLoader, entities);
    }

    private void spawnEntities() {
        player = new Player(mapLoader.getPlayerSpawn());
        entities.add(player);
        gameCamera.snapTo(player.position);

        for (Vector2 spawn : mapLoader.getGooseSpawns()) {
            entities.add(new Goose(spawn, player));
        }

        // Show exit at start of the game
        Polygon exit = mapLoader.getExitArea();
        if (exit != null) {
            Rectangle bounds = exit.getBoundingRectangle();
            gameCamera.panTo(bounds.x + bounds.width / 2f, bounds.y + bounds.height / 2f, 5);
        }
    }

    @Override
    public void render(float delta) {
        //  Toggle debug mode
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            worldRenderer.toggleDebug();
        }

        if (state == State.RUNNING) {
            // INTENTS - AI and Player input
            for (Entity entity : entities) {
                entity.updateIntent(delta);
            }

            // PHYSICS - Collisions and movement
            physicsEngine.updatePhysics();

            // UPDATE - Rotation, state changes
            for (Entity entity : entities) {
                entity.update(delta);
            }
        }

        gameCamera.update(delta, player.position);
        worldRenderer.render();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;

        gameCamera.resize(width, height);
    }

    @Override
    public void show() {
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
        mapLoader.dispose();
        worldRenderer.dispose();
    }
}