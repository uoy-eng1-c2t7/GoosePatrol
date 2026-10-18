package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.HiddenEvent;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.GroundItem;

/**
 * Draws the world: the Tiled map, then simple coloured shapes for entities.
 * Shapes are placeholders; to use sprites later, replace the {@code draw*} methods
 * and keep {@link #render()} the same. Reads the world, never changes it.
 *
 * <p>Press F3 in game to toggle debug view (walls, hidden events, exit).</p>
 */
public class WorldRenderer implements Disposable {
    private static final Color EXIT_COLOR = new Color(0.2f, 0.9f, 0.3f, 0.35f);
    private static final Color ITEM_COLOR = new Color(1f, 0.85f, 0.1f, 1f);
    private static final Color LURE_COLOR = new Color(1f, 0.55f, 0.1f, 0.5f);
    private static final Color GOOSE_IDLE = Color.WHITE;
    private static final Color GOOSE_CHASING = new Color(1f, 0.45f, 0.45f, 1f);
    private static final Color GOOSE_DISTRACTED = new Color(1f, 0.9f, 0.4f, 1f);
    private static final Color PLAYER_COLOR = new Color(0.2f, 0.5f, 1f, 1f);
    private static final Color PLAYER_BUFFED = new Color(0.2f, 0.9f, 0.9f, 1f);

    private final GameWorld world;
    private final GameCamera gameCamera;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private boolean debug;

    public WorldRenderer(GameWorld world, TiledMap map, GameCamera gameCamera, SpriteBatch batch) {
        this.world = world;
        this.gameCamera = gameCamera;
        this.mapRenderer = new OrthogonalTiledMapRenderer(map, 1f, batch);
    }

    public void toggleDebug() {
        debug = !debug;
    }

    /** Draws one frame of the world. */
    public void render() {
        gameCamera.apply();
        OrthographicCamera cam = gameCamera.getCamera();

        mapRenderer.setView(cam);
        mapRenderer.render();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(cam.combined);

        shapes.begin(ShapeType.Filled);
        drawExit();
        drawGroundItems();
        drawLures();
        drawGeese();
        drawPlayer();
        shapes.end();

        if (debug) {
            drawDebug();
        }
    }

    private void drawExit() {
        Rectangle r = world.getMap().getExitArea();
        shapes.setColor(EXIT_COLOR);
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    private void drawGroundItems() {
        shapes.setColor(ITEM_COLOR);
        for (GroundItem item : world.getGroundItems()) {
            if (!item.isCollected()) {
                Vector2 p = item.getPosition();
                shapes.circle(p.x, p.y, 5f);
            }
        }
    }

    private void drawLures() {
        shapes.setColor(LURE_COLOR);
        for (GameWorld.Lure lure : world.getLures()) {
            shapes.circle(lure.getPosition().x, lure.getPosition().y, 6f);
        }
    }

    private void drawGeese() {
        for (Goose g : world.getGeese()) {
            Vector2 p = g.getPosition();
            shapes.setColor(g.isChasing() ? GOOSE_CHASING : g.isDistracted() ? GOOSE_DISTRACTED : GOOSE_IDLE);
            shapes.circle(p.x, p.y, g.getRadius());
            drawBeak(p, g.getRotation(), g.getRadius());
        }
    }

    private void drawPlayer() {
        Vector2 p = world.getPlayer().getPosition();
        shapes.setColor(world.getPlayer().hasSpeedModifier() ? PLAYER_BUFFED : PLAYER_COLOR);
        shapes.circle(p.x, p.y, world.getPlayer().getRadius());
        drawBeak(p, world.getPlayer().getRotation(), world.getPlayer().getRadius());
    }

    /** Small orange dot on the edge of a circle so you can see which way it faces. */
    private void drawBeak(Vector2 centre, float rotationDeg, float radius) {
        shapes.setColor(Color.ORANGE);
        shapes.circle(centre.x + MathUtils.cosDeg(rotationDeg) * radius,
                centre.y + MathUtils.sinDeg(rotationDeg) * radius, 2.5f);
    }

    private void drawDebug() {
        shapes.begin(ShapeType.Line);
        shapes.setColor(Color.RED);
        for (Rectangle wall : world.getMap().getWalls()) {
            shapes.rect(wall.x, wall.y, wall.width, wall.height);
        }
        shapes.setColor(Color.MAGENTA);
        for (HiddenEvent e : world.getEvents()) {
            Rectangle a = e.getArea();
            shapes.rect(a.x, a.y, a.width, a.height);
        }
        shapes.end();
    }

    @Override
    public void dispose() {
        mapRenderer.dispose(); 
        shapes.dispose();
    }
}