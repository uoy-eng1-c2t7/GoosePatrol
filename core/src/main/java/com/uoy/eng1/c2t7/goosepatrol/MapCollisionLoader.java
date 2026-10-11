package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/**
 * Creates static Box2d bodies for a map with invsible walls
 * around the map edge, and a body for every shape in the collision
 * object layer.
 */
public class MapCollisionLoader {
    public static final String COLLISION_LAYER = "Collision";

    private MapCollisionLoader() {

    }

    /**
     * @return map width in meters.
     */
    public static float getMapWidth(TiledMap map) {
        return map.getProperties().get("width", Integer.class)
                * map.getProperties().get("tilewidth", Integer.class) / Constants.PPM;
    }

    /**
     * @return map height in meters.
     */
    public static float getMapHeight(TiledMap map) {
        return map.getProperties().get("height", Integer.class)
                * map.getProperties().get("tileheight", Integer.class) / Constants.PPM;
    }

    /**
     * Adds map boundary and all collision layer shapes to world.
     */
    public static void createBodies(TiledMap map, World world) {
        createMapBounds(world, getMapWidth(map), getMapHeight(map));

        MapLayer layer = map.getLayers().get(COLLISION_LAYER);
        if (layer == null) {
            Gdx.app.error("MapCollisionLoader", "No object layer named " + COLLISION_LAYER + " in map");
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof RectangleMapObject) {
                createRectangle(world, ((RectangleMapObject) object).getRectangle());
            } else if (object instanceof PolygonMapObject) {
                createPolygon(world, (PolygonMapObject) object);
            }
        }
    }

    /**
     * Invisable walls along the four edges so the player can't leave the map.
     */
    public static void createMapBounds(World world, float width, float height) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bd);

        EdgeShape edge = new EdgeShape();
        // Bottom.
        edge.set(0, 0, width, 0);
        body.createFixture(edge, 0f);
        // Right.
        edge.set(width, 0, width, height);
        body.createFixture(edge, 0f);
        // Top.
        edge.set(width, height, 0, height);
        body.createFixture(edge, 0f);
        // Left.
        edge.set(0, height, 0, 0);
        body.createFixture(edge, 0f);

        edge.dispose();

    }

    private static void createRectangle(World world, Rectangle rect) {
        float halfWidth = rect.width / 2f / Constants.PPM;
        float halfHeight = rect.height / 2f / Constants.PPM;

        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        bd.position.set(rect.x / Constants.PPM + halfWidth, rect.y / Constants.PPM + halfHeight);
        Body body = world.createBody(bd);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth, halfHeight);
        body.createFixture(shape, 0f);
        shape.dispose();
    }

        private static void createPolygon(World world, PolygonMapObject object) {
        // Vertices with the polygon position already applied, in pixels
        float[] vertices = object.getPolygon().getTransformedVertices().clone();
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] /= Constants.PPM;
        }
 
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.StaticBody;
        Body body = world.createBody(bd);
 
        // A loop handles concave shapes
        ChainShape shape = new ChainShape();
        shape.createLoop(vertices);
        body.createFixture(shape, 0f);
        shape.dispose();
    }

}
