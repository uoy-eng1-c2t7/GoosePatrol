package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.AssetPaths;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.GroundItem;

/**
 * Draws the world: the Tiled map, then simple coloured shapes for entities.
 * Shapes are placeholders; to use sprites later, replace the {@code draw*}
 * methods
 * and keep {@link #render()} the same. Reads the world, never changes it.
 *
 * <p>
 * Press F3 in game to toggle debug view (walls, hidden events, exit).
 * </p>
 */
public class WorldRenderer implements Disposable {
    private static final float ITEM_SIZE = 16f;
    private static final float LURE_SIZE = 16f;

    private final GameWorld world;
    private final GameCamera gameCamera;
    private final SpriteBatch batch;
    private final OrthogonalTiledMapRenderer mapRenderer;

    private final Texture playerTexture;
    private final Texture gooseTexture;
    private final Texture itemTexture;
    private final Texture lureTexture;

    private final DebugRenderer debug;

    public WorldRenderer(GameWorld world, TiledMap map, GameCamera gameCamera, SpriteBatch batch, AssetManager assets) {
        this.world = world;
        this.gameCamera = gameCamera;
        this.batch = batch;
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
        this.debug = new DebugRenderer(world);

        playerTexture = assets.get(AssetPaths.PLAYER, Texture.class);
        gooseTexture = assets.get(AssetPaths.GOOSE, Texture.class);
        itemTexture = assets.get(AssetPaths.ITEM, Texture.class);
        lureTexture = assets.get(AssetPaths.LURE, Texture.class);
    }


    /** Draws one frame of the world. */
    public void render() {
        gameCamera.apply();
        OrthographicCamera cam = gameCamera.getCamera();

        mapRenderer.setView(cam);
        mapRenderer.render();

        batch.begin();
        drawGroundItems();
        drawLures();
        drawGeese();
        drawPlayer();
        batch.end();

        debug.render(cam, batch);
    }

    public void toggleWorldDebug() {
        debug.toggleWorld();
    }

    public void toggleNavDebug() {
        debug.toggleNavGrid();
    }

    public void toggleAiDebug() {
        debug.toggleAi();
    }

    private void drawGroundItems() {
        for (GroundItem item : world.getGroundItems()) {
            if (!item.isCollected()) {
                drawCentred(itemTexture, item.getPosition(), ITEM_SIZE, ITEM_SIZE, false);
            }
        }
    }

    private void drawLures() {
        for (GameWorld.Lure lure : world.getLures()) {
            drawCentred(lureTexture, lure.getPosition(), LURE_SIZE, LURE_SIZE, false);
        }
    }

    private void drawGeese() {
        for (Goose g : world.getGeese()) {
            float size = g.getRadius() * 2f;
            drawRotated(gooseTexture, g.getPosition(), size, size, g.getRotation());
        }
    }

    private void drawPlayer() {
        float size = world.getPlayer().getRadius() * 2f;
        drawRotated(playerTexture, world.getPlayer().getPosition(), size, size,
                world.getPlayer().getRotation());
    }

    /**
     * Draws {@code texture} centred on {@code centre}, turned to
     * {@code rotationDeg}.
     */
    private void drawRotated(Texture texture, Vector2 centre, float width, float height, float rotationDeg) {
        batch.draw(texture,
                centre.x - width / 2f, centre.y - height / 2f,
                width / 2f, height / 2f, // rotate around the sprite's centre
                width, height,
                1f, 1f,
                rotationDeg,
                0, 0, texture.getWidth(), texture.getHeight(),
                false, false);
    }

    /**
     * Draws {@code texture} centred on {@code centre}, optionally mirrored
     * left-right.
     */
    private void drawCentred(Texture texture, Vector2 centre, float width, float height, boolean flipX) {
        batch.draw(texture,
                centre.x - width / 2f, centre.y - height / 2f, width, height,
                0, 0, texture.getWidth(), texture.getHeight(),
                flipX, false);
    }

    @Override
    public void dispose() {
        mapRenderer.dispose();
        debug.dispose();
    }
}