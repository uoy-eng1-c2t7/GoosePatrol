import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import javax.imageio.ImageIO;

/**
 * Builds the extruded Tiled tileset from a folder of individual tile PNGs.
 *
 * <p>Run from the project root: {@code java tools/BuildTileset.java}</p>
 *
 * <p>Tiles are placed in filename order. Tiled stores tiles by position, so
 * only ever add new tiles at the end of the order.</p>
 */
public class BuildTileset {
    private static final int TILE_SIZE = 64;
    private static final int COLUMNS = 8;
    private static final int EXTRUDE = 2;
    private static final int MARGIN = EXTRUDE;
    private static final int SPACING = EXTRUDE * 2;

    public static void main(String[] args) throws IOException {
        File inputDir = new File(args.length > 0 ? args[0] : "raw-assets/tiles");
        File output = new File(args.length > 1 ? args[1] : "assets/tiled/tilemap.png");

        File[] tiles = inputDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".png"));
        if (tiles == null || tiles.length == 0) {
            throw new IOException("No PNG tiles found in " + inputDir);
        }
        Arrays.sort(tiles, Comparator.comparing(File::getName));

        int rows = (tiles.length + COLUMNS - 1) / COLUMNS;
        BufferedImage sheet = new BufferedImage(sheetSize(COLUMNS), sheetSize(rows), BufferedImage.TYPE_INT_ARGB);

        for (int i = 0; i < tiles.length; i++) {
            BufferedImage tile = ImageIO.read(tiles[i]);
            if (tile == null || tile.getWidth() != TILE_SIZE || tile.getHeight() != TILE_SIZE) {
                throw new IOException(tiles[i].getName() + " must be a " + TILE_SIZE + "x" + TILE_SIZE + " PNG");
            }
            int left = MARGIN + (i % COLUMNS) * (TILE_SIZE + SPACING);
            int top = MARGIN + (i / COLUMNS) * (TILE_SIZE + SPACING);
            drawExtruded(sheet, tile, left, top);
            System.out.println("tile " + (i + 1) + ": " + tiles[i].getName());
        }

        ImageIO.write(sheet, "png", output);
        System.out.printf("Wrote %s (%dx%d). In Tiled use margin %d, spacing %d.%n",
            output, sheet.getWidth(), sheet.getHeight(), MARGIN, SPACING);
    }

    private static int sheetSize(int tiles) {
        return MARGIN * 2 + tiles * TILE_SIZE + (tiles - 1) * SPACING;
    }

    /** Draws a tile, then repeats its edge pixels outwards. */
    private static void drawExtruded(BufferedImage sheet, BufferedImage tile, int left, int top) {
        for (int y = -EXTRUDE; y < TILE_SIZE + EXTRUDE; y++) {
            for (int x = -EXTRUDE; x < TILE_SIZE + EXTRUDE; x++) {
                int sourceX = Math.max(0, Math.min(TILE_SIZE - 1, x));
                int sourceY = Math.max(0, Math.min(TILE_SIZE - 1, y));
                sheet.setRGB(left + x, top + y, tile.getRGB(sourceX, sourceY));
            }
        }
    }
}