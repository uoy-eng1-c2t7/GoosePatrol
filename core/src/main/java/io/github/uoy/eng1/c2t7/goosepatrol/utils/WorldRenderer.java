package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Entity;

public class WorldRenderer {
    private final GoosePatrol game;
    private final GameCamera gameCamera;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final Array<Entity> entities;
    private final MapCollision collision;

    private final ShapeRenderer debugShapes = new ShapeRenderer();
    private boolean debug = false;

    public WorldRenderer(GoosePatrol game, GameCamera camera, MapLoader mapLoader, Array<Entity> entities) {
        this.game = game;
        this.gameCamera = camera;
        this.mapRenderer = new OrthogonalTiledMapRenderer(mapLoader.getMap(), game.batch);
        this.entities = entities;
        this.collision = mapLoader.getCollision();
    }

    public void render() {
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
    }

    public void toggleDebug() {
        debug = !debug;
    }

    public void dispose() {
        debugShapes.dispose();
        mapRenderer.dispose();
    }

}
