package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Vector;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Finds routes around walls using A* algorithm on a {@link NavGrid}.
 */
public class Pathfinder {
    private static final int STRAIGHT_COST = 10;
    private static final int DIAGONAL_COST = 14;
    private static final int MAX_EXPANSIONS = 4000;
    private static final int NEAREST_WALKABLE_RING = 3;

    // {dx, dy}, first four are straight, last four diagonal
    private static final int[][] DIRECTIONS = {
        { 0, 1 }, { 0, -1 }, { -1, 0}, { 1, 0 },
        { -1, 1 }, { 1, 1 }, {-1, -1 }, { 1, -1}
    };
    
    /**
     * A node waiting to be explored , with f-cost when it was queued
     */
    private record OpenEntry(Node node, int fCost) { }

    private final NavGrid navGrid;
    private final PriorityQueue<OpenEntry> open = 
        new PriorityQueue<>(Comparator.comparingInt(OpenEntry::fCost));
    private int searchCounter = 0;

    public Pathfinder(NavGrid navGrid) {
        this.navGrid = navGrid;
    }

    /**
     * True if there is a clear straight line between the two points
     */
    public boolean canSee(Vector2 from, Vector2 to) {
        return navGrid.hasLineOfSight(from, to);
    }

    /**
     * Finds a route from {@code startPos} to {@code targetPos}.
     * @return waypoints in world coordingates, in order not including the start.
     * Empty if no route exists or search limit was hit.
     */
    public Array<Vector2> findPath(Vector2 startPos, Vector2 targetPos) {
        Array<Vector2> none = new Array<>();

        Node startNode = navGrid.findNearestWalkable(
            navGrid.getNodeFromWorldPosition(startPos.x, startPos.y), NEAREST_WALKABLE_RING);
        Node targetNode = navGrid.findNearestWalkable(
            navGrid.getNodeFromWorldPosition(targetPos.x, targetPos.y), NEAREST_WALKABLE_RING);

        if (startNode == null || targetNode == null) return none;

        if (startNode == targetNode) {
            none.add(new Vector2(targetPos));
            return none;
        }

        searchCounter++;
        open.clear();

        touch(startNode);
        startNode.gCost = 0;
        startNode.hCost = heuristic(startNode, targetNode);
        open.add(new OpenEntry(startNode, startNode.hCost));

        int expansions = 0;
        while (!open.isEmpty() && expansions < MAX_EXPANSIONS) {
            Node current = open.poll().node();
            if (current.closed) continue;

            current.closed = true;
            expansions++;

            if (current == targetNode) {
                return smooth(startPos, targetPos, retrace(startNode, targetNode));
            }

            for (int[] dir : DIRECTIONS) {
                Node neighbour = navGrid.getNode(current.gridX + dir[0], current.gridY + dir[1]);

                if (neighbour == null || !neighbour.isWalkable) continue;

                touch(neighbour);
                if (neighbour.closed) continue;

                boolean diagonal = dir[0] != 0 && dir[1] != 0;
                if (diagonal && cutsCorner(current, dir)) continue;

                int newG = current.gCost + (diagonal ? DIAGONAL_COST : STRAIGHT_COST);
                if (newG < neighbour.gCost) {
                    neighbour.gCost = newG;
                    neighbour.hCost = heuristic(neighbour, targetNode);
                    neighbour.parent = current;
                    open.add(new OpenEntry(neighbour, newG + neighbour.hCost));
                }
            }
        }
        return none;
    }

    /**
     * Prepares a node for the current search the first time this search sees it
     */
    private void touch(Node node) {
        if (node.searchID == searchCounter) return;
        node.searchID = searchCounter;
        node.gCost = Integer.MAX_VALUE;
        node.hCost = 0;
        node.closed = false;
        node.parent = null;
    }

    /**
     * A diagonal step is not allowed if either side tile it squeezes between is a wall.
     */
    private boolean cutsCorner(Node from, int[] dir) {
        Node sideX = navGrid.getNode(from.gridX + dir[0], from.gridY);
        Node sideY = navGrid.getNode(from.gridX, from.gridY + dir[1]);
        return sideX == null || !sideX.isWalkable || sideY == null || !sideY.isWalkable;
    }

    /**
     * True cost to the target on empty 8-direction grid 
     */
    private int heuristic(Node a, Node b) {
        int dx = Math.abs(a.gridX - b.gridX);
        int dy = Math.abs(a.gridY - b.gridY);
        return DIAGONAL_COST * Math.min(dx, dy) + STRAIGHT_COST * Math.abs(dx - dy);
    }

    /**
     * Walks the parent links back from the target to the start and returns tile centers in order.
     */
    private Array<Vector2> retrace(Node startNode, Node endNode) {
        Array<Vector2> path = new Array<>();
        for (Node n = endNode; n != startNode; n =n.parent) {
            path.add(new Vector2(
                n.gridX * navGrid.getTileWidth() + navGrid.getTileWidth() / 2f,
                n.gridY * navGrid.getTileHeight() + navGrid.getTileHeight() / 2f));
        }
        path.reverse();
        return path;
    }

    /**
     * Skip ahead to furthest waypoint to see if goose can see in straight line, this removes zig-zag betweeen tile centers.
     */
    private Array<Vector2> smooth(Vector2 startPos, Vector2 targetPos, Array<Vector2> raw) {
        Array<Vector2> result = new Array<>();
        Vector2 anchor = startPos;
        int i = 0;

        while (i < raw.size) {
            int farthest = i;
            for (int j = raw.size - 1; j > i; j--) {
                if (navGrid.hasLineOfSight(anchor, raw.get(j))) {
                    farthest = j;
                    break;
                }
            }
            result.add(raw.get(farthest));
            anchor = raw.get(farthest);
            i = farthest + 1;
        }

        // Finish on exact target position instead of tile center if reachable
        if (result.size > 0) {
            Vector2 before = result.size > 1 ? result.get(result.size - 2) : startPos;
            if (navGrid.hasLineOfSight(before, targetPos)) {
                result.set(result.size - 1, new Vector2(targetPos));
            }
        }
        return result;
    }
}
