package io.github.uoy.eng1.c2t7.goosepatrol.world;

import java.util.Vector;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MapLoader {
    private final float width;
    private final float height; 
    private final Array<Rectangle> walls = new Array<>();
    private final Array<Vector2> gooseSpawns = new Array<>();
    private final Vector2 playerSpawn = new Vector2(100, 100); // Default if not set in Tiled

    public MapLoader(TiledMap map) {
        MapProperties props = map.getProperties();
        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);
        width = props.get("width", Integer.class) * tileWidth;
        height = props.get("height",Integer.class) * tileHeight;

        // Collision Layer
        MapLayer collisionLayer = map.getLayers().get("collision");
        if (collisionLayer != null) {
            for (var obj : collisionLayer.getObjects().getByType(RectangleMapObject.class)) {
                walls.add(obj.getRectangle());
            }
        }

        // Entities layer
        MapLayer entityLayer = map.getLayers().get("entities");
        if (entityLayer != null) {
            for (MapObject obj : entityLayer.getObjects()) {
                String name = obj.getName();
                if (name == null) continue;

                float x = obj.getProperties().get("x", float.class);
                float y = obj.getProperties().get("y", float.class);

                if ("player".equalsIgnoreCase(name)) {
                    playerSpawn.set(x, y);
                } else if ("goose".equalsIgnoreCase(name)) {
                    gooseSpawns.add(new Vector2(x, y));
                }
            } 
        }
    }

    public Vector2 getPlayerSpawn() {
        return playerSpawn;
    }

    public Array<Vector2> getGooseSpawns() {
        return gooseSpawns;
    }

    public Array<Rectangle> getWalls() {
        return walls;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
