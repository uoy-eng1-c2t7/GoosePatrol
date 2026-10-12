package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;
import io.github.uoy.entities.Entity;

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

    private final float mapWidth;
    private final float mapHeight;

    private State state = State.RUNNING;

    public GameScreen(GoosePatrol game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new ExtendViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, camera);
        map = new TmxMapLoader().load(Constants.MAP_FILE);
        mapRenderer = new OrthogonalTiledMapRenderer(map, game.batch);

        mapWidth = map.getProperties().get("width", Integer.class) 
                    * map.getProperties().get("tilewidth", Integer.class);
        mapHeight = map.getProperties().get("height", Integer.class) 
                    * map.getProperties().get("tileheight", Integer.class);
    }

    @Override
    public void render(float delta) {
        // INTENT - where do entities want to go
        for (Entity entity : entities) {
            entity.updateIntent(delta);
        }

        // PHYSICS - can they go there? resolve any collisionjs
        updatePhysics();

        // RENDER 
        viewport.apply();
        ScreenUtils.clear(Color.DARK_GRAY);
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        for (Entity entity : entities) {
            entity.draw(game.batch);
        }
        game.batch.end();
    }

    private void updatePhysics() {
        for (Entity entity : entities) {
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
        for (Entity otherEntity : entities) {
            if (otherEntity != movingEntity) {    // skip checking self 
                if (Intersector.overlaps(movingEntity.hitbox, otherEntity.hitbox)) {
                    return true;
                }
            }
        }

        return false;
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