package io.github.uoy.eng1.c2t7.goosepatrol.world.pathfinding;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/** Tells one walker which point to head for next on its way to a target. */
public class PathFollower {
    private static final float REPATH_SECONDS = 0.4f;
    private static final float WAYPOINT_RADIUS = 12f;
    private static final float TARGET_JUMP_DISTANCE = 64f;

    private final NavGrid grid;
    private final Array<Vector2> waypoints = new Array<>();
    private final Vector2 lastTarget = new Vector2();
    private int nextWaypoint;
    private float repathTimer;

    public PathFollower(NavGrid grid) {
        this.grid = grid;
        // Random start so geese don't all search on the same frame
        repathTimer = MathUtils.random(REPATH_SECONDS);
    }

    /** Returns the point to walk towards this frame. */
    public Vector2 nextPoint(Vector2 position, Vector2 target, float delta) {
        repathTimer -= delta;
        if (repathTimer <= 0f || target.dst(lastTarget) > TARGET_JUMP_DISTANCE) {
            repath(position, target);
        }

        if (waypoints.isEmpty()) {
            return target;
        }

        while (nextWaypoint < waypoints.size - 1
                && position.dst(waypoints.get(nextWaypoint)) < WAYPOINT_RADIUS) {
            nextWaypoint++;
        }
        return waypoints.get(nextWaypoint);
    }

    private void repath(Vector2 position, Vector2 target) {
        repathTimer = REPATH_SECONDS;
        lastTarget.set(target);
        nextWaypoint = 0;

        if (grid.hasClearLine(position, target)) {
            waypoints.clear(); // No search needed, walk straight there
        } else {
            grid.findPath(position, target, waypoints);
        }
    }

    public Array<Vector2> getWaypoints() {
        return waypoints;
    }

    public int getNextWaypoint() {
        return nextWaypoint;
    }
}