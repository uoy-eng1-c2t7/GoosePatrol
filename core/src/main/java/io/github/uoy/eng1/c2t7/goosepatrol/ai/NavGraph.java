package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.ai.pfa.Connection;


import io.github.uoy.eng1.c2t7.goosepatrol.world.Physics.MapCollision;

/** Grid of cells over the map for gdx-ai A* algorithm. A cell is walkable if its center is
 * at least {@code clearance} away from every wall. Cells connect to their 8 neighbours and diagonals
 * are not allowed to cut a wall corner.
 */
public class NavGraph implements IndexedGraph<NavNode> {
    private static final float DIAGONAL_COST = 1.412f;
    private static final int NEAREST_SEARCH_RINGS = 3;

    private final int cols;
    private final int rows;
    private final float cellSize;
    private final Array<NavNode> nodes = new Array<>();

    public NavGraph(MapCollision collision, float mapWidth, float mapHeight, float cellSize, float clearance) {
        this.cellSize = cellSize;
        this.cols = (int) Math.ceil(mapWidth / cellSize);
        this.rows = (int) Math.ceil(mapHeight / cellSize);

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                float cx = (col + 0.5f) * cellSize;
                float cy = (row + 0.5f) * cellSize;
                boolean walkable = !collision.isBlocked(cx, cy, clearance);
                nodes.add(new NavNode(row * cols + col, col, row, cx, cy, walkable));
            }
        } 
        for (NavNode node : nodes) {
            if (node.isWalkable()) linkNeighbours(node);
        }
    }

    public float getCellSize() {
        return cellSize;
    }

    /** The walkable cell closest to {@code p}, or null if none is within a few cells. */
    public NavNode nearestWalkable(Vector2 p) {
        int col = MathUtils.clamp((int) (p.x / cellSize), 0, cols - 1);
        int row = MathUtils.clamp((int) (p.y / cellSize), 0, rows - 1);
        NavNode centre = nodeAt(col, row);
        if (centre.isWalkable()) return centre;

        for (int ring = 1; ring <= NEAREST_SEARCH_RINGS; ring++) {
            NavNode best = null;
            float bestDist = Float.MAX_VALUE;
            for (int dy = -ring; dy <= ring; dy++) {
                for (int dx = -ring; dx <= ring; dx++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != ring) continue;   
                    NavNode n = nodeAt(col + dx, row + dy);
                    if (n == null || !n.isWalkable()) continue;
                    float dist = Vector2.dst2(p.x, p.y, n.getX(), n.getY());
                    if (dist < bestDist) {
                        bestDist = dist;
                        best = n;
                    }
                }
            }
            if (best != null) return best;
        }
        return null;
    }
    
    private void linkNeighbours(NavNode node) {
        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                if (dx == 0 && dy == 0) continue;
                NavNode other = nodeAt(node.getCol() + dx, node.getRow() + dy);
                if (other == null || !other.isWalkable()) continue;

                boolean diagonal = dx != 0 && dy != 0;
                if (diagonal && (!isWalkable(node.getCol() + dx, node.getRow())
                        || !isWalkable(node.getCol(), node.getRow() + dy))) {
                    continue;   // would squeeze past a wall corner
                }
                node.addConnection(other, diagonal ? DIAGONAL_COST : 1f);
            }
        }
    }

     private NavNode nodeAt(int col, int row) {
        if (col < 0 || col >= cols || row < 0 || row >= rows) return null;
        return nodes.get(row * cols + col);
    }

    private boolean isWalkable(int col, int row) {
        NavNode n = nodeAt(col, row);
        return n != null && n.isWalkable();
    }

    public Array<NavNode> getNodes() {
        return nodes;   
    }

    @Override
    public int getIndex(NavNode node) {
        return node.getIndex();
    }

    @Override
    public int getNodeCount() {
        return nodes.size;
    }

    @Override
    public Array<Connection<NavNode>> getConnections(NavNode fromNode) {
        return fromNode.getConnections();
    }
}
