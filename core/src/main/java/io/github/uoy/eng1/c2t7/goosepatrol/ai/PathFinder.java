package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.world.Physics.MapCollision;

/**
 * Finds routes around walls using gdx-ai A* on a {@link NavGraph}, then
 * straightens out the result using line of sight checks. Geese only call {@link #findPath} when
 * {@link #canSee} says the straight line is blocked
 */
public class PathFinder {
    private final NavGraph graph;
    private final MapCollision collision;
    private final float clearance;
    private final IndexedAStarPathFinder<NavNode> finder;
    private final Heuristic<NavNode> heuristic;
    private final DefaultGraphPath<NavNode> nodePath = new DefaultGraphPath<>();

    /**
     * @param clearance how far to stay from the walls (should be at least entitys radius)
     */
    public PathFinder(NavGraph graph, MapCollision collision, float clearance) {
        this.graph = graph;
        this.collision = collision;
        this.clearance = clearance;
        this.finder = new IndexedAStarPathFinder<NavNode>(graph);
        this.heuristic = (a, b) -> Vector2.dst2(a.getX(), a.getY(), b.getX(), b.getY()) / graph.getCellSize();
    }

    /** True if there is a clear straight line between the two points */
    public boolean canSee(Vector2 from, Vector2 to) {
        return !collision.segmentBlocked(from, to, 0f);
    }

    /** Can a body walk this line without clipping a wall?  */
    public boolean canWalk(Vector2 from, Vector2 to) {
        return !collision.segmentBlocked(from, to, clearance - 1f); // slightly under clearance so geese dont get stuck on walls
    }

    /**
     * Finds a route from {@code start} to {@code target}.
     * 
     * @return waypoints in order (excluding start), or an empty array if no route exists.
     */
    public Array<Vector2> findPath(Vector2 start, Vector2 target) {
        Array<Vector2> waypoints = new Array<>();
        NavNode from = graph.nearestWalkable(start);
        NavNode to = graph.nearestWalkable(target);
        if (from == null || to == null)
            return waypoints;

        nodePath.clear();
        if (!finder.searchNodePath(from, to, heuristic, nodePath))
            return waypoints;

        for (int i = 1; i < nodePath.getCount(); i++) { 
            NavNode n = nodePath.get(i);
            waypoints.add(new Vector2(n.getX(), n.getY()));
        }
        if (waypoints.isEmpty()) {
            waypoints.add(new Vector2(target));
        }
        return smooth(start, waypoints);
    }

    /** Skips ahead to the furthest waypoint within view to remove zig-zagging */
    private Array<Vector2> smooth(Vector2 start, Array<Vector2> raw) {
        Array<Vector2> result = new Array<>();
        Vector2 anchor = start;
        int i = 0;
        while (i < raw.size) {
            int farthest = i;
            for (int j = raw.size - 1; j > i; j--) {
                if (canWalk(anchor, raw.get(j))) {
                    farthest = j;
                    break;
                }
            }
            result.add(raw.get(farthest));
            anchor = raw.get(farthest);
            i = farthest + 1;
        }
        return result;
    }
}
