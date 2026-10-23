package io.github.uoy.eng1.c2t7.goosepatrol.world;

import java.util.Vector;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Item;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemRegistry;

public class MapLoader {
    public static class ItemSpawn {
        public final Item item;
        public final Vector2 position;

        public ItemSpawn(Item item, Vector2 position) {
            this.item = item;
            this.position = position;
        }
    }

    private final float width;
    private final float height; 
    private final Array<Rectangle> walls = new Array<>();
    private final Array<Vector2> gooseSpawns = new Array<>();
    private final Array<ItemSpawn> itemSpawns = new Array<>();
    private final Vector2 playerSpawn = new Vector2(100, 100); // Default if not set in Tiled
    private Rectangle exitZone = null;

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
                } else {
                    Item item = ItemRegistry.get(name.toLowerCase());
                    if (item != null) {
                        itemSpawns.add(new ItemSpawn(item, new Vector2(x, y)));
                    }
                }
            } 
        }

        // Exit win layer
        MapLayer exitLayer = map.getLayers().get("exit");
        if (exitLayer != null) {
            var obj = exitLayer.getObjects().get(0);
            if (obj instanceof RectangleMapObject) {
                exitZone = ((RectangleMapObject) obj).getRectangle();
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

    public Rectangle getExitZone() {
        return exitZone;
    }

    public Array<ItemSpawn> getItemSpawns() {
        return itemSpawns;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
