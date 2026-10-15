package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;

public class WorldRenderer {
    private final GameWorld world;
    private final SpriteBatch batch;
    private final OrthogonalTiledMapRenderer mapRenderer;

    public WorldRenderer(GameWorld world, SpriteBatch batch, TiledMap map) {
        this.world = world;
        this.batch = batch;

        mapRenderer = new OrthogonalTiledMapRenderer(map, batch);
    }   
}
