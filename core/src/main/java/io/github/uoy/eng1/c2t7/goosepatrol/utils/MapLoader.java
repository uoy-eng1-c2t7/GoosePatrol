package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class MapLoader {
    private static final float BOUNDARY_THICKNESS = 64;

    private final TiledMap map;
    private final float width;
    private final float height;
    private final MapCollision collision = new MapCollision();
    private final Vector2 playerSpawn = new Vector2();
    private final Array<Vector2> gooseSpawns = new Array<>();
    private Polygon exitArea;

    public MapLoader(String mapFile) {
        map = new TmxMapLoader().load(mapFile);

        width = map.getProperties().get("width", Integer.class)
                * map.getProperties().get("tilewidth", Integer.class);

        height = map.getProperties().get("height", Integer.class)
                * map.getProperties().get("tileheight", Integer.class);

        loadCollisionPolygons();
        addMapBoundary();
        loadSpawns();
    }

    private void loadCollisionPolygons() {
        for (MapObject object : getLayer("collision").getObjects()) {
            if (object instanceof PolygonMapObject) {
                collision.add(((PolygonMapObject) object).getPolygon());
            } 
        }
    }

    private void addMapBoundary() {
        float t = BOUNDARY_THICKNESS;
        addBox(-t, -t, width + 2 * t, t);
        addBox(-t, height, width + 2 * t, t);
        addBox(-t, 0, t, height);
        addBox(width, 0, t, height);
    }

    private void addBox(float x, float y, float w, float h) {
        collision.add(new Polygon(new float[] {
            x, y, x + w, y, x + w, y + h, x, y + h
        }));
    }

    private void loadSpawns() {
        boolean foundPlayer = false;

        for (MapObject object : getLayer("spawns").getObjects()) {
            String kind = object.getProperties().get("kind", "", String.class);

            if (kind.equals("player")) {
                playerSpawn.set(objectPosition(object));
                foundPlayer = true;
            } else if (kind.equals("goose")) {
                gooseSpawns.add(objectPosition(object));
            } else if (kind.equals("exit")) {
                exitArea = ((PolygonMapObject) object).getPolygon();
            }
        }

        if (!foundPlayer) {
            throw new GdxRuntimeException("No object with kind 'player' in the 'spawns' layer");
        }
    }

    private MapLayer getLayer(String name) {
        MapLayer layer = map.getLayers().get(name);
        if (layer == null) {
            throw new GdxRuntimeException("Map is missing the '" + name + "' layer");
        }
        return layer;
    }

    private Vector2 objectPosition(MapObject object) {
        return new Vector2(
            object.getProperties().get("x", Float.class),
            object.getProperties().get("y", Float.class));
    }    

    public TiledMap getMap() { return map; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }
    public MapCollision getCollision() { return collision; }
    public Vector2 getPlayerSpawn() { return playerSpawn; }
    public Array<Vector2> getGooseSpawns() { return gooseSpawns;}
    public Polygon getExitArea() { return exitArea; }

    public void dispose() {
        map.dispose();
    }
}
