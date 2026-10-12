package io.github.uoy.eng1.c2t7.goosepatrol.ai;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapCollision;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.MapLoader;

/**
 * Converts the tile map into a node grid for A* algorithm.
 */
public class NavGrid {
    private final Node[][] grid;
    private final int gridSizeX;
    private final int gridSizeY;
    private final float tileWidth;
    private final float tileheight;

    public NavGrid(MapLoader mapLoader) {
        this.gridSizeX = mapLoader.getMap().getProperties().get("width", Integer.class);
        this.gridSizeY = mapLoader.getMap().getProperties().get("height", Integer.class);
        this.tileWidth = mapLoader.getMap().getProperties().get("tilewidth", Integer.class);
        this.tileheight = mapLoader.getMap().getProperties().get("tileheight", Integer.class);

        grid = new Node[gridSizeX][gridSizeY];
        createGrid(mapLoader.getCollision());
    }

    private void createGrid(MapCollision collision) {
        // Radius to match goose hitbox
        Circle dummyHitbox = new Circle(0, 0, 10f);

        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                // Find world coordinate center of tile
                float worldX = (x * tileWidth) + (tileWidth / 2f);
                float worldY = (y * tileheight) + (tileheight / 2f);

                dummyHitbox.setPosition(worldX, worldY);

                // If collision detected, tile is unwalkable
                boolean walkable = !collision.hits(dummyHitbox);
                grid[x][y] = new Node(walkable, x ,y);
            }
        }
    }

    public Node getNodeFromWorldPosition(float worldX, float worldY) {
        int x = (int) (worldX / tileWidth);
        int y = (int) (worldY / tileheight);

        // Make sure array index doesn't go out of bounds
        x = Math.max(0, Math.min(x, gridSizeX - 1));
        y = Math.max(0, Math.min(y, gridSizeY - 1));

        return grid[x][y];
    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        for (int x = 0; x < gridSizeX; x++) {
            for (int y = 0; y < gridSizeY; y++) {
                Node node = grid[x][y];
                float worldX = x * tileWidth;
                float worldY = y * tileheight;

                if (!node.isWalkable) {
                    // Draw red X - unwalkable
                    shapeRenderer.setColor(Color.RED);
                    shapeRenderer.line(worldX, worldY, worldX + tileWidth, worldY + tileheight);
                    shapeRenderer.line(worldX + tileWidth, worldY, worldX, worldY + tileheight);
                } else {
                    // Draw box - walkable
                    shapeRenderer.setColor(Color.DARK_GRAY);
                    shapeRenderer.rect(worldX, worldY, tileWidth, tileheight);
                }
            }
        }
    }

    public Node[][] getGrid() {
        return grid;
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
        return tileheight;
    }
}
