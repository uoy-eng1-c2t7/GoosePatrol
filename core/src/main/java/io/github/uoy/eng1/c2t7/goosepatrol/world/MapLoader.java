package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

public class MapLoader {
    private final float width;
    private final float height; 
    private final Array<Rectangle> walls = new Array<>();

    public MapLoader(TiledMap map) {
        MapProperties props = map.getProperties();
        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);
        width = props.get("width", Integer.class) * tileWidth;
        height = props.get("height",Integer.class) * tileHeight;

        var layer = map.getLayers().get("collision");
        if (layer != null) {
            for (var obj : layer.getObjects().getByType(RectangleMapObject.class)) {
                walls.add(obj.getRectangle());
            }
        }
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
