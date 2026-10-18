package io.github.uoy.eng1.c2t7.goosepatrol.world.map;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.PointMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.world.HiddenEvent;

/**
 * Reads game data out of a {@link TiledMap}. Expected object layers:
 * <ul>
 *   <li><b>collision</b> (optional): rectangles the player/geese can't enter</li>
 *   <li><b>spawns</b> (required): one point named {@code player}, any number named {@code goose}</li>
 *   <li><b>exit</b> (required): a rectangle, the bus stop</li>
 *   <li><b>events</b> (optional): rectangles with a string property {@code effect}
 *       (see {@link HiddenEvent.Effect})</li>
 *   <li><b>items</b> (optional): points with a string property {@code itemType}
 *       (an id from ItemRegistry)</li>
 * </ul>
 * Missing required layers throw with a message saying exactly what to add in Tiled.
 */
public class MapLoader {
    private static final String COLLISION_LAYER = "collision";
    private static final String SPAWNS_LAYER = "spawns";
    private static final String EXIT_LAYER = "exit";
    private static final String EVENTS_LAYER = "events";
    private static final String ITEMS_LAYER = "items";
    private static final String PLAYER_SPAWN_NAME = "player";
    private static final String GOOSE_SPAWN_NAME = "goose";

    /** An item point from the map: where it is and which item it is. */
    public record ItemSpawn(Vector2 position, String itemType) {
    }

    private final float width;
    private final float height;
    private final float tileSize;
    private final Array<Rectangle> walls = new Array<>();
    private final Array<Vector2> gooseSpawns = new Array<>();
    private final Array<HiddenEvent> events = new Array<>();
    private final Array<ItemSpawn> itemSpawns = new Array<>();
    private final Vector2 playerSpawn;
    private final Rectangle exitArea;

    public MapLoader(TiledMap map) {
        MapProperties props = map.getProperties();
        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);
        width = props.get("width", Integer.class) * tileWidth;
        height = props.get("height", Integer.class) * tileHeight;
        tileSize = tileWidth;

        for (RectangleMapObject wall : objectsOf(map, COLLISION_LAYER, RectangleMapObject.class, false)) {
            walls.add(new Rectangle(wall.getRectangle()));
        }

        Vector2 foundPlayer = null;
        for (PointMapObject spawn : objectsOf(map, SPAWNS_LAYER, PointMapObject.class, true)) {
            Vector2 point = new Vector2(spawn.getPoint());
            if (PLAYER_SPAWN_NAME.equals(spawn.getName())) {
                foundPlayer = point;
            } else if (GOOSE_SPAWN_NAME.equals(spawn.getName())) {
                gooseSpawns.add(point);
            }
        }
        if (foundPlayer == null) {
            throw new IllegalStateException("Layer '" + SPAWNS_LAYER + "' has no point named '"
                    + PLAYER_SPAWN_NAME + "'");
        }
        playerSpawn = foundPlayer;

        Array<RectangleMapObject> exits = objectsOf(map, EXIT_LAYER, RectangleMapObject.class, true);
        if (exits.isEmpty()) {
            throw new IllegalStateException("Layer '" + EXIT_LAYER + "' has no rectangle");
        }
        exitArea = new Rectangle(exits.first().getRectangle());

        for (RectangleMapObject e : objectsOf(map, EVENTS_LAYER, RectangleMapObject.class, false)) {
            String effect = e.getProperties().get("effect", String.class);
            if (effect == null) {
                throw new IllegalStateException("Event '" + e.getName() + "' has no 'effect' property");
            }
            events.add(new HiddenEvent(e.getName(), new Rectangle(e.getRectangle()),
                    HiddenEvent.Effect.valueOf(effect.trim().toUpperCase())));
        }

        for (PointMapObject obj : objectsOf(map, ITEMS_LAYER, PointMapObject.class, false)) {
            String type = obj.getProperties().get("itemType", String.class);
            if (type == null) {
                throw new IllegalStateException("Item point '" + obj.getName() + "' has no 'itemType' property");
            }
            itemSpawns.add(new ItemSpawn(new Vector2(obj.getPoint()), type.trim()));
        }
    }

    /** Objects of {@code type} in {@code layerName}. A missing layer is an error only if {@code required}. */
    private static <T extends MapObject> Array<T> objectsOf(TiledMap map, String layerName, Class<T> type,
            boolean required) {
        MapLayer layer = map.getLayers().get(layerName);
        if (layer == null) {
            if (required) {
                throw new IllegalStateException("Map is missing object layer '" + layerName + "'");
            }
            return new Array<>();
        }
        return layer.getObjects().getByType(type);
    }

    public Array<Rectangle> getWalls() {
        return walls;
    }

    public Array<Vector2> getGooseSpawns() {
        return gooseSpawns;
    }

    public Vector2 getPlayerSpawn() {
        return playerSpawn;
    }

    public Rectangle getExitArea() {
        return exitArea;
    }

    public Array<HiddenEvent> getEvents() {
        return events;
    }

    public Array<ItemSpawn> getItemSpawns() {
        return itemSpawns;
    }

    /** Map width in world units (pixels). */
    public float getWidth() {
        return width;
    }

    /** Map height in world units (pixels). */
    public float getHeight() {
        return height;
    }

    public float getTileSize() {
        return tileSize;
    }
}
