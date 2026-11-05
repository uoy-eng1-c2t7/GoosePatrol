package io.github.uoy.eng1.c2t7.goosepatrol.world.pathfinding;

import com.badlogic.gdx.ai.pfa.Connection;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.Heuristic;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.ai.pfa.indexed.IndexedGraph;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

/**
 * A walkable grid built from the map's walls, used to find routes around them.
 * Walls are grown by the walker's radius, so a route never clips a wall.
 */
public class NavGrid implements IndexedGraph<NavGrid.Cell> {
    private static final int CELL_SIZE = 16;
    private static final int MAX_SEARCH_RINGS = 16;
    private static final float DIAGONAL_COST = 1.414f;

    /** One walkable square of the grid. */
    public static final class Cell {
        private final int column;
        private final int row;
        private final int index;
        private final Array<Connection<Cell>> connections = new Array<>(8);

        private Cell(int column, int row, int index) {
            this.column = column;
            this.row = row;
            this.index = index;
        }
    }

    /** A move between two neighbouring cells. */
    private static final class Step implements Connection<Cell> {
        private final Cell from;
        private final Cell to;
        private final float cost;

        private Step(Cell from, Cell to, float cost) {
            this.from = from;
            this.to = to;
            this.cost = cost;
        }

        @Override
        public float getCost() {
            return cost;
        }

        @Override
        public Cell getFromNode() {
            return from;
        }

        @Override
        public Cell getToNode() {
            return to;
        }
    }

    private final int columns;
    private final int rows;
    private final Cell[] cells; // Blocked cells are null
    private final Array<Rectangle> grownWalls = new Array<>();
    private final IndexedAStarPathFinder<Cell> finder;
    private final DefaultGraphPath<Cell> foundPath = new DefaultGraphPath<>();
    private final Heuristic<Cell> heuristic = (from, to) -> {
        int dx = Math.abs(from.column - to.column);
        int dy = Math.abs(from.row - to.row);
        return Math.max(dx, dy) + (DIAGONAL_COST - 1f) * Math.min(dx, dy);
    };

    public NavGrid(float mapWidth, float mapHeight, Array<Rectangle> walls, float walkerRadius) {
        columns = MathUtils.ceil(mapWidth / CELL_SIZE);
        rows = MathUtils.ceil(mapHeight / CELL_SIZE);
        cells = new Cell[columns * rows];

        // A little over the radius, so walkers don't catch on wall corners
        float grow = walkerRadius + 1f;
        for (Rectangle wall : walls) {
            grownWalls.add(new Rectangle(wall.x - grow, wall.y - grow,
                    wall.width + grow * 2, wall.height + grow * 2));
        }

        createCells();
        connectCells();
        finder = new IndexedAStarPathFinder<>(this);
    }

    /**
     * Finds a route between two world positions.
     *
     * @param out filled with the points to walk through, ending at {@code to}.
     * @return false if there is no route.
     */
    public boolean findPath(Vector2 from, Vector2 to, Array<Vector2> out) {
        out.clear();
        Cell start = nearestCell(from);
        Cell end = nearestCell(to);
        if (start == null || end == null) {
            return false;
        }

        foundPath.clear();
        if (!finder.searchNodePath(start, end, heuristic, foundPath)) {
            return false;
        }

        for (Cell cell : foundPath) {
            out.add(new Vector2(centreOf(cell.column), centreOf(cell.row)));
        }
        out.add(new Vector2(to));
        return true;
    }

    /** Returns true if a walker can go straight between two points. */
    public boolean hasClearLine(Vector2 from, Vector2 to) {
        for (Rectangle wall : grownWalls) {
            if (Intersector.intersectSegmentRectangle(from, to, wall)) {
                return false;
            }
        }
        return true;
    }

    private void createCells() {
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                if (!isBlocked(centreOf(column), centreOf(row))) {
                    int index = row * columns + column;
                    cells[index] = new Cell(column, row, index);
                }
            }
        }
    }

    private boolean isBlocked(float x, float y) {
        for (Rectangle wall : grownWalls) {
            if (wall.contains(x, y)) {
                return true;
            }
        }
        return false;
    }

    private void connectCells() {
        for (Cell cell : cells) {
            if (cell == null) {
                continue;
            }
            for (int dy = -1; dy <= 1; dy++) {
                for (int dx = -1; dx <= 1; dx++) {
                    if (dx != 0 || dy != 0) {
                        connect(cell, dx, dy);
                    }
                }
            }
        }
    }

    private void connect(Cell cell, int dx, int dy) {
        Cell neighbour = cellAt(cell.column + dx, cell.row + dy);
        if (neighbour == null) {
            return;
        }

        boolean diagonal = dx != 0 && dy != 0;
        // Don't cut across the corner of a wall
        if (diagonal && (cellAt(cell.column + dx, cell.row) == null || cellAt(cell.column, cell.row + dy) == null)) {
            return;
        }
        cell.connections.add(new Step(cell, neighbour, diagonal ? DIAGONAL_COST : 1f));
    }

    private Cell cellAt(int column, int row) {
        if (column < 0 || row < 0 || column >= columns || row >= rows) {
            return null;
        }
        return cells[row * columns + column];
    }

    /**
     * The cell at a position, or the closest walkable one if that cell is blocked.
     */
    private Cell nearestCell(Vector2 position) {
        int column = MathUtils.floor(position.x / CELL_SIZE);
        int row = MathUtils.floor(position.y / CELL_SIZE);

        for (int ring = 0; ring <= MAX_SEARCH_RINGS; ring++) {
            Cell closest = null;
            float closestDistance = Float.MAX_VALUE;
            for (int y = row - ring; y <= row + ring; y++) {
                for (int x = column - ring; x <= column + ring; x++) {
                    boolean onRing = Math.max(Math.abs(x - column), Math.abs(y - row)) == ring;
                    Cell cell = onRing ? cellAt(x, y) : null;
                    if (cell != null) {
                        float distance = position.dst2(centreOf(x), centreOf(y));
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            closest = cell;
                        }
                    }
                }
            }
            if (closest != null) {
                return closest;
            }
        }
        return null;
    }

    private float centreOf(int cell) {
        return cell * CELL_SIZE + CELL_SIZE / 2f;
    }

    public Array<Rectangle> getGrownWalls() {
        return grownWalls;
    }

    @Override
    public int getIndex(Cell cell) {
        return cell.index;
    }

    @Override
    public int getNodeCount() {
        return cells.length;
    }

    @Override
    public Array<Connection<Cell>> getConnections(Cell cell) {
        return cell.connections;
    }
}