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

        // Wall collision – 3 passes so corner cases (literally) settle correctly.
        // A single pass can push into a second wall; iterating converges quickly.
        for (int pass = 0; pass < 3; pass++) {
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
        for (Rectangle r : walls) {
            if (segmentHitsBox(a.x, a.y, b.x, b.y,
                    r.x - margin, r.y - margin, r.x + r.width + margin, r.y + r.height + margin)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Segment vs axis-aligned box (slab method). Counts a start point inside the
     * box as a hit.
     */
    private static boolean segmentHitsBox(float ax, float ay, float bx, float by,
            float minX, float minY, float maxX, float maxY) {
        float t0 = 0f, t1 = 1f;
        float dx = bx - ax, dy = by - ay;
        if (Math.abs(dx) < 1e-6f) {
            if (ax < minX || ax > maxX)
                return false;
        } else {
            float ta = (minX - ax) / dx, tb = (maxX - ax) / dx;
            if (ta > tb) {
                float t = ta;
                ta = tb;
                tb = t;
            }
            t0 = Math.max(t0, ta);
            t1 = Math.min(t1, tb);
            if (t0 > t1)
                return false;
        }
        if (Math.abs(dy) < 1e-6f) {
            if (ay < minY || ay > maxY)
                return false;
        } else {
            float ta = (minY - ay) / dy, tb = (maxY - ay) / dy;
            if (ta > tb) {
                float t = ta;
                ta = tb;
                tb = t;
            }
            t0 = Math.max(t0, ta);
            t1 = Math.min(t1, tb);
            if (t0 > t1)
                return false;
        }
        return true;
    }
}
