package io.github.uoy.eng1.c2t7.goosepatrol.world.Physics;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** Static walls from the Tiled collision layer plus the map edge. */
public class MapCollision {
    private final Array<Rectangle> walls;
    private final float mapWidth;
    private final float mapHeight;

    public MapCollision(Array<Rectangle> walls, float mapWidth, float mapHeight) {
        this.walls = walls;
        this.mapWidth = mapWidth;
        this.mapHeight = mapHeight;
    }

    public Array<Rectangle> getWalls() {
        return walls;
    }

    /**
     * Moves {@code pos} out of any wall a circle of {@code radius} overlaps and
     * keeps inside the map.
     */
    public void pushOut(Vector2 pos, float radius) {
        // Keep inside map edges
        pos.x = MathUtils.clamp(pos.x, radius, mapWidth - radius);
        pos.y = MathUtils.clamp(pos.y, radius, mapHeight - radius);

        // Wall collision
        for (Rectangle r : walls) {
            float nearestX = MathUtils.clamp(pos.x, r.x, r.x + r.width);
            float nearestY = MathUtils.clamp(pos.y, r.y, r.y + r.height);
            float dx = pos.x - nearestX;
            float dy = pos.y - nearestY;
            float dist2 = dx * dx + dy * dy;
            if (dist2 >= radius * radius)
                continue;

            if (dist2 > 0) {
                float dist = (float) Math.sqrt(dist2);
                pos.add(dx / dist * (radius - dist), dy / dist * (radius - dist));
            } else {
                // Center is inside wall, leave by nearest edge
                float left = pos.x - r.x;
                float right = r.x + r.width - pos.x;
                float down = pos.y - r.y;
                float up = r.y + r.height - pos.y;
                float min = Math.min(Math.min(left, right), Math.min(down, up));
                if (min == left)
                    pos.x = r.x - radius;
                else if (min == right)
                    pos.x = r.x + r.width + radius;
                else if (min == down)
                    pos.y = r.y - radius;
                else
                    pos.y = r.y + r.height + radius;
            }
        }
    }

    /**
     * True if the point is inside any wall grown by {@code margin} on every side.
     */
    public boolean isBlocked(float x, float y, float margin) {
        for (Rectangle r : walls) {
            if (x >= r.x - margin && x <= r.x + r.width + margin
                    && y >= r.y - margin && y <= r.y + r.height + margin) {
                return true;
            }
        }
        return false;
    }

    /** True if the straight line a-b crosses any wall grown by {@code margin}. */
    public boolean segmentBlocked(Vector2 a, Vector2 b, float margin) {
        Rectangle grown = new Rectangle();
        for (Rectangle r : walls) {
            grown.set(r.x - margin, r.y - margin, r.width + 2 * margin, r.height + 2 * margin);
            if (Intersector.intersectSegmentRectangle(a, b, grown))
                return true;
        }
        return false;
    }
}
