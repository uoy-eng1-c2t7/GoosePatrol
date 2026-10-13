package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapCollision;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapLoader;

public class NavGrid {
    private final Node[][] grid;
    private final int gridSizeX;
    private final int gridSizeY;
    private final float tileWidth;
    private final float tileHeight;
    private final float agentRadius;

    /**
     * @param agentRadius radius of the entity that wil walk the grid.
     */
    public NavGrid(MapLoader mapLoader, float agentRadius) {
        this.agentRadius = agentRadius;

        this.gridSizeX = mapLoader.getMap().getProperties().get("width", Integer.class);
        this.gridSizeY = mapLoader.getMap().getProperties().get("height", Integer.class);
        this.tileWidth = mapLoader.getMap().getProperties().get("tilewidth", Integer.class);
        this.tileHeight = mapLoader.getMap().getProperties().get("tileheight", Integer.class);

        grid = new Node[gridSizeX][gridSizeY];
        createGrid(mapLoader.getCollision(), agentRadius);
    }

    private void createGrid(MapCollision collision, float agentRadius) {
        Circle probe = new Circle(0, 0, agentRadius);

        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                probe.setPosition((x * tileWidth) + (tileWidth) / 2f, (y * tileHeight) + (tileHeight / 2f));
                grid[x][y] = new Node(!collision.hits(probe), x, y);
            }
        }
    }

    /**
     * Node containing a world position, positions outside map clamped to edge
     */
    public Node getNodeFromWorldPosition(float worldX, float worldY) {
        int x = Math.max(0, Math.min((int) (worldX / tileWidth), gridSizeX - 1));
        int y = Math.max(0, Math.min((int) (worldY / tileHeight), gridSizeY - 1));
        return grid[x][y];
    }

    /**
     * Node at grid coordinates, or null if outside grid
     */
    public Node getNode(int x, int y) {
        if (x < 0 || x >= gridSizeX || y < 0 || y >= gridSizeY)
            return null;
        return grid[x][y];
    }

    /**
     * If {@code origin} is blocked, look in growing square rings around it for
     * closest
     * walkable node.
     * 
     * @return a walkable node or null if non within {@code maxRing} tiles
     */
    public Node findNearestWalkable(Node origin, int maxRing) {
        if (origin.isWalkable)
            return origin;

        for (int ring = 1; ring <= maxRing; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dy = -ring; dy <= ring; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != ring)
                        continue;

                    Node candidate = getNode(origin.gridX + dx, origin.gridY + dy);
                    if (candidate != null && candidate.isWalkable)
                        return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Can an agent walk straight from A to B? Checks the centre line plus a line
     * one agent radius to each side, so the whole body fits and doesn't clip wall corners.
     */
    public boolean hasLineOfSight(Vector2 from, Vector2 to) {
        float dx = to.x - from.x;
        float dy = to.y - from.y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length == 0f)
            return getNodeFromWorldPosition(from.x, from.y).isWalkable;

        // Sideways offset, perpendicular to the direction of travel, one agent radius
        // long
        float sideX = -dy / length * agentRadius;
        float sideY = dx / length * agentRadius;

        return lineIsWalkable(from.x, from.y, to.x, to.y)
                && lineIsWalkable(from.x + sideX, from.y + sideY, to.x + sideX, to.y + sideY)
                && lineIsWalkable(from.x - sideX, from.y - sideY, to.x - sideX, to.y - sideY);
    }

    /**
     * Samples points every half tile along a line and checks each is on a walkable tile
    */
    private boolean lineIsWalkable(float x0, float y0, float x1, float y1) {
        float distance = (float) Math.hypot(x1 - x0, y1 - y0);
        float step = Math.min(tileWidth, tileHeight) / 2f;
        int samples = Math.max(1, (int) (distance / step));

        for (int i = 0; i <= samples; i++) {
            float t = i / (float) samples;
            if (!getNodeFromWorldPosition(x0 + (x1 - x0) * t, y0 + (y1 - y0) * t).isWalkable) {
                return false;
            }
        }
        return true;
    }

    /**
     * Debug overlay. Only draws tiles inside {@code view} as gets laggy with
     * big map otherwise.
     */
    public void drawDebug(ShapeRenderer shapeRenderer, Rectangle view) {
        int minX = Math.max(0, (int) (view.x / tileWidth));
        int maxX = Math.min(gridSizeX - 1, (int) ((view.x + view.width) / tileWidth));
        int minY = Math.max(0, (int) (view.y / tileHeight));
        int maxY = Math.min(gridSizeY - 1, (int) ((view.y + view.height) / tileHeight));

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                float worldX = x * tileWidth;
                float worldY = y * tileHeight;

                if (!grid[x][y].isWalkable) {
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.line(worldX, worldY, worldX + tileWidth, worldY + tileHeight);

                    shapeRenderer.line(worldX + tileWidth, worldY, worldX, worldY + tileHeight);
                } else {
                    shapeRenderer.setColor(Color.DARK_GRAY);
                    shapeRenderer.rect(worldX, worldY, tileWidth, tileHeight);
                }
            }
        }
    }

    public int getGridSizeX() {
        return gridSizeX;
    }

    public int getGridSizeY() {
        return gridSizeY;
    }

    public float getTileWidth() {
        return tileWidth;
    }

    public float getTileHeight() {
        return tileHeight;
    }
}