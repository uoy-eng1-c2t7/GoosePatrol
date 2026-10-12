package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Player;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameCamera;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapCollision;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapLoader;

/** The main gameplay screen */
public class GameScreen implements Screen {
    public enum State {
        RUNNING, PAUSED
    }

    private final Array<Entity> entities = new Array<>();
    private final MapLoader mapLoader;
    private final MapCollision collision;
    private Player player;
    private State state = State.RUNNING;
    private final GameCamera gameCamera;
    private final GoosePatrol game;
    private final OrthogonalTiledMapRenderer mapRenderer;


    // Debug
    private final ShapeRenderer debugShapes = new ShapeRenderer();
    private boolean debug = false;

    public GameScreen(GoosePatrol game) {
        this.game = game;

        mapLoader = new MapLoader(Constants.MAP_FILE);
        collision = mapLoader.getCollision();
        gameCamera = new GameCamera(mapLoader.getWidth(), mapLoader.getHeight());
        mapRenderer = new OrthogonalTiledMapRenderer(mapLoader.getMap(), game.batch);

        spawnEntities();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            debug = !debug;
        }

        // INTENT - where do entities want to go
        for (Entity entity : entities) {
            entity.updateIntent(delta);
        }

        updatePhysics();

        for (Entity entity : entities) {
            entity.update(delta);
        }

        gameCamera.update(delta, player.position);

        draw();
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

    private void updatePhysics() {
        for (int i = 0; i < entities.size; i++) {
            Entity entity = entities.get(i);

            // X AXIS MOVEMENT
            if (entity.velocity.x != 0) {                    // any velocity?
                entity.position.x += entity.velocity.x;      // if so then move blindly
                entity.syncHithox();                    

                if (isColliding(entity)) {
                    entity.position.x -= entity.velocity.x; // undo, entity hit something!
                    entity.syncHithox();            
                }
            }

            // Y AXIS MOVEMENT
            if (entity.velocity.y != 0) {                   // any velocity?
                entity.position.y += entity.velocity.y;     // if so then move blindly
                entity.syncHithox();    

                if (isColliding(entity)) {
                    entity.position.y -= entity.velocity.y; // undo, entity hit something!
                    entity.syncHithox();
                }
            }
        }
    }

    private void draw() {
        gameCamera.apply();
        ScreenUtils.clear(Color.DARK_GRAY);
        mapRenderer.setView(gameCamera.getCamera());
        mapRenderer.render();

        game.batch.setProjectionMatrix(gameCamera.getCamera().combined);
        game.batch.begin();
        for (Entity entity : entities) {
            entity.draw(game.batch);
        }
        game.batch.end();

        if (debug) {
            drawDebug();
        }
    }

    private boolean isColliding(Entity movingEntity) {
        if (collision.hits(movingEntity.hitbox)) return true;

        for (int i = 0; i < entities.size; i++) {
            Entity other = entities.get(i);
            if (other != movingEntity && Intersector.overlaps(movingEntity.hitbox, other.hitbox)) {
                return true;
            }
        }
        return false;
    }

    private void drawDebug() {
        debugShapes.setProjectionMatrix(gameCamera.getCamera().combined);
        debugShapes.begin(ShapeRenderer.ShapeType.Line);

        debugShapes.setColor(Color.RED);
        for (Polygon wall : collision.getWalls()) {
            debugShapes.polygon(wall.getTransformedVertices());
        }

        debugShapes.setColor(Color.GREEN);
        for (Entity entity : entities) {
            debugShapes.circle(entity.hitbox.x, entity.hitbox.y, entity.hitbox.radius);
        }

        debugShapes.end();
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
        debugShapes.dispose();
        mapLoader.dispose();
        mapRenderer.dispose();
    }
}