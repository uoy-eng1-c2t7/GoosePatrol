package io.github.uoy.eng1.c2t7.goosepatrol.ai;

/**
 * Represents a grid cell for A* pathfinding.
 */
public class Node {
    public final int gridX;
    public final int gridY;
    public final boolean isWalkable;

    // A* Costs
    public int gCost;
    public int hCost;
    public Node parent;

    public Node(boolean isWalkable, int gridX, int gridY) {
        this.isWalkable = isWalkable;
        this.gridX = gridX;
        this.gridY = gridY;
    }

    public int getFCost() {
        return gCost + hCost;
    }
}
