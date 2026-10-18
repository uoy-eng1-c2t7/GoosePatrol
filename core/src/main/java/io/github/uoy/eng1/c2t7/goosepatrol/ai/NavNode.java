package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.utils.Array;

/** One cell of the navigation grid. */
public class NavNode {
    private final int index;
    private final int col;
    private final int row;
    private final float x; // center of the cell in world coords
    private final float y;
    private final boolean walkable;
    private final Array<Connection<NavNode>> connections = new Array<>();

    NavNode(int index, int col, int row, float x, float y, boolean walkable) {
        this.index = index;
        this.col = col;
        this.row = row;
        this.x = x;
        this.y = y;
        this.walkable = walkable;
    }

    void addConnection(NavNode to, float cost) {
        connections.add(new Link(this, to, cost));
    }

    public int getIndex() {
        return index;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public boolean isWalkable() {
        return walkable;
    }

    Array<Connection<NavNode>> getConnections() {
        return connections;
    }

    private static class Link implements Connection<NavNode> {
        private final NavNode from;
        private final NavNode to;
        private final float cost;

        Link(NavNode from, NavNode to, float cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }

        @Override
        public float getCost() {
            return cost;
        }

        @Override
        public NavNode getFromNode() {
            return from;
        }

        @Override
        public NavNode getToNode() {
            return to;
        }
    }
}
