package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Player;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;

/** The main gameplay screen */
public class GameScreen implements Screen {
    public enum State {
        RUNNING, PAUSED
    }

    private final GoosePatrol game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final TiledMap map;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private Array<Entity> entities;
    private Array<Rectangle> walls;
    private Player player;

    // Camera
    private final Vector2 focus = new Vector2();
    private boolean followPlayer = true;
    private float panTimer;

    private final float mapWidth;
    private final float mapHeight;

    private State state = State.RUNNING;

    public GameScreen(GoosePatrol game) {
        // initialise everything
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        map = new TmxMapLoader().load(Constants.MAP_FILE);
        mapRenderer = new OrthogonalTiledMapRenderer(map, game.batch);

        mapWidth = map.getProperties().get("width", Integer.class) 
                    * map.getProperties().get("tilewidth", Integer.class);
        mapHeight = map.getProperties().get("height", Integer.class) 
                    * map.getProperties().get("tileheight", Integer.class);

        walls = new Array<Rectangle>();
        entities = new Array<Entity>();

        // load map walls for collision
        for (MapObject object : map.getLayers().get("collision").getObjects()) {
            if (object instanceof RectangleMapObject) {
                Rectangle rect = ((RectangleMapObject) object).getRectangle();
                
                walls.add(rect);
            }
        }

        // test player
        player = new Player(new Vector2(100, 100));
        entities.add(player);
        camera.position.set(player.hitbox.x, player.hitbox.y, 0);
    }

    @Override
    public void render(float delta) {
        // INTENT - where do entities want to go
        for (Entity entity : entities) {
            entity.updateIntent(delta);
        }

        // PHYSICS - can they go there? resolve any collisionjs
        updatePhysics();

        // Update - rotation, animations etc
        for (Entity entity : entities) {
            entity.update(delta);
        }

        updateCamera(delta);

        // RENDER - map first then entities ontop
        viewport.apply();
        ScreenUtils.clear(Color.DARK_GRAY);
        mapRenderer.setView(camera);
        mapRenderer.render();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        for (Entity entity : entities) {
            entity.draw(game.batch);
        }
        game.batch.end();
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

    private boolean isColliding(Entity movingEntity) {
        // Check against walls
        for (Rectangle wall : walls) {
            if (Intersector.overlaps(movingEntity.hitbox, wall)) {
                return true;
            }
        }

        // Check against other entities
        for (int i = 0; i < entities.size; i++) {
            Entity otherEntity = entities.get(i);

            if (otherEntity != movingEntity) {    // skip checking self 
                if (Intersector.overlaps(movingEntity.hitbox, otherEntity.hitbox)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void updateCamera(float delta) { 
        // Camera can either follow player or focus on something depending on bool
        // I imagine using this for showing where the exit is at the start of the 
        // game
        if (!followPlayer) {
            panTimer -= delta;
            if (panTimer <= 0) followPlayer = true;
        }

        float targetX = followPlayer ? player.hitbox.x : focus.x;
        float targetY = followPlayer ? player.hitbox.y : focus.y;

        float halfw = camera.viewportWidth * camera.zoom / 2f;
        float halfh = camera.viewportHeight * camera.zoom / 2f;

        float t = 1f - (float) Math.exp(-4f * delta); // Lower = slower pan
        float x = camera.position.x + (targetX - camera.position.x) * t;
        float y = camera.position.y + (targetY - camera.position.y) * t;

        camera.position.x = clampAxis(x, halfw, mapWidth);
        camera.position.y = clampAxis(y, halfh, mapHeight);
        camera.update();
    }

    private void panTo(float x, float y, float seconds) {
        focus.set(x, y);
        followPlayer = false;
        panTimer = seconds;
    }

    private float clampAxis(float target, float half, float mapSize) {
        if (mapSize <= half * 2f) return mapSize / 2f; // view bigger than map so center it
        return MathUtils.clamp(target, half, mapSize - half);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;

        viewport.update(width, height, true);
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
    }

    @Override
    public void dispose() {
    }
}