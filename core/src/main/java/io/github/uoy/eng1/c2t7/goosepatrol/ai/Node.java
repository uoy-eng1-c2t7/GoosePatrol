package io.github.uoy.eng1.c2t7.goosepatrol.ai;

/**
 * Represents a grid cell for A* pathfinding.
 */
public class Node {
    public final int gridX;
    public final int gridY;
    public final boolean isWalkable;

    // A* data
    int searchID = -1;  // which search last touched this node
    int gCost;          // cost of the cheapest known route from start to here
    int hCost;          // estimated cost from here to target
    boolean closed;     // true once nodes best route is finl
    Node parent;        // mode came from on cheapest route - for rebuilding path

    public Node(boolean isWalkable, int gridX, int gridY) {
        this.isWalkable = isWalkable;
        this.gridX = gridX;
        this.gridY = gridY;
    }
}
