package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;

public class MapLoader {
    private final float width;
    private final float height; 

    public MapLoader(TiledMap map) {
        MapProperties props = map.getProperties();
        int tileWidth = props.get("tilewidth", Integer.class);
        int tileHeight = props.get("tileheight", Integer.class);
        width = props.get("width", Integer.class) * tileWidth;
        height = props.get("height",Integer.class) * tileHeight;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
