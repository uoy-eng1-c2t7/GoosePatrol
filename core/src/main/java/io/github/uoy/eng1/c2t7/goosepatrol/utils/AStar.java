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
        Array<Vector2> currPath;

        public ANode(Vector2 pos) {
            this.x = (int) pos.x;
            this.y = (int) pos.y;
            this.currPath = new Array<>();
        }

        public ANode(Vector2 pos, Array<Vector2> path) {
            this.x = (int) pos.x;
            this.y = (int) pos.y;
            this.currPath = path;
        }


        public void updateCosts(Vector2 startCellCoords, Vector2 endCellCoords) {
            setGCost(startCellCoords);
            setHCost(endCellCoords);
            setFCost();
        }

        private void setGCost(Vector2 startCellCoords) {
            int diffX = Math.abs((int) startCellCoords.x - this.x);
            int diffY = Math.abs((int) startCellCoords.y - this.y);

            Vector2 v = new Vector2(diffX, diffY);
            this.gCost = (int) Math.floor(v.len() * 10);
        }

        private void setHCost(Vector2 endCellCoords) {
            int diffX = Math.abs((int) endCellCoords.x - this.x);
            int diffY = Math.abs((int) endCellCoords.y - this.y);

            Vector2 v = new Vector2(diffX, diffY);
            this.hCost = (int) Math.floor(v.len() * 10);
        }

        private void setFCost() {
            this.fCost = this.gCost + this.hCost;
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
        PriorityQueue<ANode> AStarHeap = new PriorityQueue<>((ANode n1, ANode n2) -> Integer.compare(n2.fCost, n1.fCost));
        Set<ANode> visited = new HashSet<>();

        Vector2 startCellCoords = tileCoords(from);
        ANode startNode = new ANode(startCellCoords);
        AStarHeap.add(startNode);
        visited.add(startNode);

        Vector2 endCellCoords = tileCoords(to);
        ANode endNode = new ANode(endCellCoords);

        while (!AStarHeap.isEmpty()) {
            ANode curNode = AStarHeap.poll();
            Array<Vector2> curNodePath = curNode.currPath;

            for (Vector2 direction: DIRECTIONS) {
                Vector2 newDirection = new Vector2(curNode.x + direction.x, curNode.y + direction.y);
                TiledMapTileLayer.Cell neighbour = layer.getCell((int) newDirection.x, (int) newDirection.y);
                if (neighbour == null || neighbour.getTile().getProperties().get("walkable") == null) continue;
                ANode neighbourNode = new ANode(newDirection, curNodePath);


                if (neighbourNode.equals(endNode)) return curNodePath;
                neighbourNode.updateCosts(startCellCoords, endCellCoords);

                // if tile can be traversed, add to heap
                if ((boolean) neighbour.getTile().getProperties().get("walkable") && !visited.contains(neighbourNode)) {
                    curNodePath.add(newDirection);
                    AStarHeap.add(neighbourNode);
                    // this is suboptimal, a path to any given node is now not guaranteed to be the shortest
                    // to be fixed
                    visited.add(neighbourNode);
                }
            }
        }


        return new Array<>();
    }

    public boolean inBounds(Vector2 pos) {
        return pos.x >= 0 && pos.y >= 0;
    }

    public Vector2 tileCoords(Vector2 worldCoordinates) {
        return new Vector2((float) Math.floor(worldCoordinates.x / TILE_WIDTH), (float) Math.floor(worldCoordinates.y / TILE_WIDTH));
    }

}
