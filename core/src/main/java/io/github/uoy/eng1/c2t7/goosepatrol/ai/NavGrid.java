package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapCollision;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapLoader;

public class NavGrid {
    private static final float DEFAULT_AGENT_RADIUS = 10f;

    private final Node[][] grid;
    private final int gridSizeX;
    private final int gridSizeY;
    private final float tileWidth;
    private final float tileHeight;

    public NavGrid(MapLoader mapLoader) {
        this(mapLoader, DEFAULT_AGENT_RADIUS);
    }

    /**
     * @param agentRadius radius of the entity that wil walk the grid.
     */
    public NavGrid(MapLoader mapLoader, float agentRadius) {
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
        if (x < 0 || x >= gridSizeX || y < 0 || y >= gridSizeY) return null;
        return grid[x][y];
    }

    /**
     * If {@code origin} is blocked, look in growing square rings around it for closest 
     * walkable node. 
     * @return a walkable node or null if non within {@code maxRing} tiles
     */ 
    public Node findNearestWalkable(Node origin, int maxRing) {
        if (origin.isWalkable) return origin;

        for (int ring = 1; ring <= maxRing; ring++) {
            for (int dx = -ring; dx <= ring; dx++) {
                for (int dy = -ring; dy <= ring; dy++) {
                    if (Math.max(Math.abs(dx), Math.abs(dy)) != ring) continue;

                    Node candidate = getNode(origin.gridX + dx, origin.gridY + dy);
                    if (candidate != null && candidate.isWalkable) return candidate;
                }
            }
        }
        return null;
    }

    /**
     * Straight line, A-B - can I walk there? returns true if so. Cheaper than pathfinding
     */
    public boolean hasLineOfSight(Vector2 from, Vector2 to) {
        float distance = from.dst(to);
        float step = Math.min(tileWidth, tileHeight) / 2f;
        int samples = Math.max(1, (int) (distance / step));

        for (int i = 0; i <= samples; i++) {
            float t = i / (float) samples;
            float x = from.x + (to.x - from.x) * t;
            float y = from.y + (to.y - from.y) * t;
            if (!getNodeFromWorldPosition(x, y).isWalkable) return false;
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
        int minY = Math.max(0, (int) (view.y /tileHeight));
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

    public int getGridSizeX() { return gridSizeX; }
    public int getGridSizeY() { return gridSizeY; }
    public float getTileWidth() { return tileWidth; }
    public float getTileHeight() { return tileHeight; }
}