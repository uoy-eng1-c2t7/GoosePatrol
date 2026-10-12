package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class MapCollision {
    private final Array<Polygon> polygons = new Array<>();
    
    private final Vector2 edgeStart = new Vector2();
    private final Vector2 edgeEnd = new Vector2();
    private final Vector2 circleCenter = new Vector2();

    public void add(Polygon poly) {
        polygons.add(poly);
    }

    public Array<Polygon> getWalls() {
        return polygons;
    }

    public boolean hits(Circle circle) {
        circleCenter.set(circle.x, circle.y);
        float radiusSquared = circle.radius * circle.radius;
        
        // Check each edge of the wall , 
        for (Polygon polygon : polygons) {
            float[] vertices = polygon.getTransformedVertices(); // x0, y0, x1, y1 ..

            // check each edge of the wall, edge i fruns from vertex i to the next vertex
            for (int i = 0; i < vertices.length; i += 2) {
                int next = (i + 2) % vertices.length; 
                edgeStart.set(vertices[i], vertices[i + 1]);
                edgeEnd.set(vertices[next], vertices[next + 1]);

                if (Intersector.intersectSegmentCircle(edgeStart, edgeEnd, circleCenter, radiusSquared)) {
                    return true;
                }
            }
            
            // Circles center is within wall 
            if (polygon.contains(circleCenter)) return true; 
        }
        return false;
    }
    
}
