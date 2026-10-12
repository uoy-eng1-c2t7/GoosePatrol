package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * Calculates the shorest path between two points using the A* algorithm;
 */
public class Pathfinder {
    private final NavGrid navGrid;

    public Pathfinder(NavGrid navGrid) {
        this.navGrid = navGrid;
    }

    public Array<Vector2> findPath(Vector2 startPos, Vector2 targetPos) {
        Node startNode = navGrid.getNodeFromWorldPosition(startPos.x, startPos.y);
        Node targetNode = navGrid.getNodeFromWorldPosition(targetPos.x, targetPos.y);

        Array<Vector2> waypoints = new Array<>();

        // If target is inside a wall no path can be found
        if (startNode == null || targetNode == null || !targetNode.isWalkable) {
            return waypoints;
        }

        Array<Node> openSet = new Array<>();
        Array<Node> closedSet = new Array<>();
        openSet.add(startNode);

        while (openSet.size > 0) {
            // Find node in openSet with lowest F-Cost
            Node currentNode = openSet.get(0);
            for (int i = 1; i < openSet.size; i++) {
                Node evaluate = openSet.get(i);
                if (evaluate.getFCost() < currentNode.getFCost() ||
                    (evaluate.getFCost() == currentNode.getFCost() && evaluate.hCost < currentNode.hCost)) {
                        currentNode = evaluate;
                    }
            }

            openSet.removeValue(currentNode, true);
            closedSet.add(currentNode);

            // Path found
            if (currentNode == targetNode) {
                return retracePath(startNode, targetNode);
            }

            for (Node neighbor : getNeighbors(currentNode)) {
                if (!neighbor.isWalkable || closedSet.contains(neighbor, true)) {
                    continue;
                }

                int movementCostToNeighbor = currentNode.gCost + getDistance(currentNode, neighbor);

                if (movementCostToNeighbor < neighbor.gCost || !openSet.contains(neighbor, true)) {
                    neighbor.gCost = movementCostToNeighbor;
                    neighbor.hCost = getDistance(neighbor, targetNode);
                    neighbor.parent = currentNode;

                    if (!openSet.contains(neighbor, true)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }

        return waypoints;
    }

    private Array<Vector2> retracePath(Node startNode, Node endNode) {
        Array<Node> path = new Array<>();
        Node currentNode = endNode;

        // Go back from the end node to the start node
        while (currentNode != startNode) {
            path.add(currentNode);
            currentNode = currentNode.parent;
        }

        // Path was traced backwards so need to reverse it
        path.reverse();

        // Convert grid nodes back into world coordinates
        Array<Vector2> worldPath = new Array<>();
        for (Node node : path) {
            float worldX = (node.gridX * navGrid.getTileWidth() + (navGrid.getTileWidth() / 2f));
            float worldY = (node.gridY * navGrid.getTileHeight() + (navGrid.getTileHeight() / 2f));
            worldPath.add(new Vector2(worldX, worldY));
        }

        return worldPath;
    }

    private Array<Node> getNeighbors(Node node) {
        Array<Node> neighbors = new Array<>();

        // Up down left right
        int[][] directions = { { 0, 1 }, { 0, -1 }, { -1, 0 }, { 1, 0 } };

        for (int[] dir : directions) {
            int checkX = node.gridX + dir[0];
            int checkY = node.gridY + dir[1];

            if (checkX >= 0 && checkX < navGrid.getGridSizeX() && checkY >= 0 && checkY < navGrid.getGridSizeY()) {
                neighbors.add(navGrid.getGrid()[checkX][checkY]);
            }
        }

        return neighbors;
    }

    private int getDistance(Node nodeA, Node nodeB) {
        int dstX = Math.abs(nodeA.gridX - nodeB.gridX);
        int dstY = Math.abs(nodeA.gridY - nodeB.gridY);
        return dstX + dstY;
    }

}
