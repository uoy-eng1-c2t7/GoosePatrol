package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import java.util.HashSet;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;

public class AStar {
    private final TiledMapTileLayer layer;
    final float TILE_WIDTH = 32f;


    private final Vector2[] DIRECTIONS;


    class ANode {
        int x;
        int y;
        int gCost;
        int hCost;
        int fCost;
        final ANode parent;

        public ANode(Vector2 pos) {
            this.x = (int) pos.x;
            this.y = (int) pos.y;
            this.parent = null;
        }

        public ANode(Vector2 pos, ANode parent) {
            this.x = (int) pos.x;
            this.y = (int) pos.y;
            this.parent = parent;
        }



        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ANode)) return false;

            ANode n = (ANode) o;
            return (this.x == n.x && this.y == n.y);
        }

        //override hashcode method for checking visited set membership
        @Override
        public int hashCode() {
            return Objects.hash(this.x, this.y);
        }

    }

    public AStar(TiledMapTileLayer layer) {
        this.layer = layer;
        DIRECTIONS = new Vector2[]{new Vector2(1, 0),
            new Vector2(-1, 0),
            new Vector2(0, 1),
            new Vector2(0, -1),
            new Vector2(1, 1),
            new Vector2(-1, 1),
            new Vector2(1, -1),
            new Vector2(-1, -1)};
    }


    public Array<Vector2> findPath(Vector2 from, Vector2 to) {
        // Use a heap to track the next node to visit dependent on its f cost (distance from start node + distance from end node)
        // Node with lowest f cost always appears on top of heap
        PriorityQueue<ANode> AStarHeap = new PriorityQueue<>((ANode n1, ANode n2) -> Integer.compare(n1.fCost, n2.fCost));
        Set<ANode> visited = new HashSet<>();

        Vector2 startCellCoords = tileCoords(from);
        ANode startNode = new ANode(startCellCoords);

        Vector2 endCellCoords = tileCoords(to);
        ANode endNode = new ANode(endCellCoords);

        startNode.hCost = calculateHCost(startCellCoords, endCellCoords);
        startNode.gCost = 0;
        startNode.fCost = startNode.hCost + startNode.gCost;
        AStarHeap.add(startNode);

        //test
        Vector2 tx = tileCoords(new Vector2(14, 3188));
        Object s = layer.getCell((int) tx.x, (int) tx.y).getTile().getId();

        while (!AStarHeap.isEmpty()) {
            ANode curNode = AStarHeap.poll();
            if (!visited.add(curNode)) continue;

            for (Vector2 direction: DIRECTIONS) {
                Vector2 newDirection = new Vector2(curNode.x + direction.x, curNode.y + direction.y);
                TiledMapTileLayer.Cell neighbour = layer.getCell((int) newDirection.x, (int) newDirection.y);
                if (neighbour == null || neighbour.getTile().getProperties().get("walkable") == null) continue;
                ANode neighbourNode = new ANode(newDirection, curNode);


                if (neighbourNode.equals(endNode)) return buildPath(neighbourNode);
                boolean diagonal = (direction.x != 0 && direction.y != 0);
                // 14 is approximately sqrt(2) * 10
                int distance = diagonal ? 14 : 10;

                neighbourNode.gCost = curNode.gCost + distance;
                neighbourNode.hCost = calculateHCost(newDirection, endCellCoords);
                neighbourNode.fCost = neighbourNode.gCost + neighbourNode.hCost;

                // if tile can be traversed, add to heap
                if (!visited.contains(neighbourNode)) {
                    AStarHeap.add(neighbourNode);
                }
            }
        }


        return new Array<>();
    }

    public Array<Vector2> buildPath(ANode node) {
        Array<Vector2> path = new Array<>();

        while (node != null) {
            Vector2 v = new Vector2(node.x, node.y);
            path.add(worldCoords(v));
            node = node.parent;
        }

        path.reverse();

        return path;
    }


    public Vector2 tileCoords(Vector2 worldCoordinates) {
        return new Vector2((float) Math.floor(worldCoordinates.x / TILE_WIDTH), (float) Math.floor((worldCoordinates.y) / TILE_WIDTH));
    }

    public Vector2 worldCoords(Vector2 tileCoordinates) {
        return new Vector2(tileCoordinates.x * 32 + 16, ((tileCoordinates.y * 32)) + 16);
    }

    private int calculateHCost(Vector2 startCellCoords, Vector2 endCellCoords) {
        int diffX = (int) Math.abs(endCellCoords.x - startCellCoords.x);
        int diffY = (int) Math.abs(endCellCoords.y - startCellCoords.y);

        Vector2 v = new Vector2(diffX, diffY);
        return (int) Math.floor(v.len() * 10);
    }
}
