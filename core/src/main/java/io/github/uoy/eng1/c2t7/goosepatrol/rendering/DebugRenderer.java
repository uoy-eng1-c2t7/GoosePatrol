package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.ai.NavGraph;
import io.github.uoy.eng1.c2t7.goosepatrol.ai.NavNode;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.map.HiddenEvent;

/**
 * Developer overlays, drawn on top of the world. Nothing here affects gameplay.
 * <ul>
 *   <li><b>world</b>: walls (red), hidden events (magenta), exit (green)</li>
 *   <li><b>nav grid</b>: every pathfinding cell, green = walkable, red = blocked</li>
 *   <li><b>AI</b>: per goose state, route, goal, home, aggro/leash rings, stuck flag</li>
 * </ul>
 */
public class DebugRenderer implements Disposable {
    private final GameWorld world;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont(); // libGDX built-in font, no asset needed
    private final Rectangle view = new Rectangle();

    private boolean showWorld;
    private boolean showNavGrid;
    private boolean showAi;

    public DebugRenderer(GameWorld world) {
        this.world = world;
        font.getData().setScale(0.5f);
        font.setUseIntegerPositions(false);
    }

    public void toggleWorld() { showWorld = !showWorld; }
    public void toggleNavGrid() { showNavGrid = !showNavGrid; }
    public void toggleAi() { showAi = !showAi; }

    /** Draws whichever overlays are on. Call after the world has been drawn. */
    public void render(OrthographicCamera cam, SpriteBatch batch) {
        if (!showWorld && !showNavGrid && !showAi) {
            return;
        }
        float halfW = cam.viewportWidth * cam.zoom / 2f;
        float halfH = cam.viewportHeight * cam.zoom / 2f;
        view.set(cam.position.x - halfW, cam.position.y - halfH, halfW * 2f, halfH * 2f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.setProjectionMatrix(cam.combined);

        if (showNavGrid) {
            drawNavGrid();
        }
        shapes.begin(ShapeType.Line);
        if (showWorld) {
            drawWorld();
        }
        if (showAi) {
            for (Goose g : world.getGeese()) {
                drawGoose(g);
            }
        }
        shapes.end();

        if (showAi) {
            drawLabels(cam, batch);
        }
    }

    private void drawNavGrid() {
        NavGraph graph = world.getNavGraph();
        float half = graph.getCellSize() * 0.2f;
        shapes.begin(ShapeType.Filled);
        for (NavNode n : graph.getNodes()) {
            if (!view.contains(n.getX(), n.getY())) {
                continue;
            }
            if (n.isWalkable()) {
                shapes.setColor(0f, 1f, 0f, 0.35f);
            } else {
                shapes.setColor(1f, 0f, 0f, 0.35f);
            }
            shapes.rect(n.getX() - half, n.getY() - half, half * 2f, half * 2f);
        }
        shapes.end();
    }

    private void drawWorld() {
        shapes.setColor(Color.RED);
        for (Rectangle wall : world.getMap().getWalls()) {
            shapes.rect(wall.x, wall.y, wall.width, wall.height);
        }
        shapes.setColor(Color.MAGENTA);
        for (HiddenEvent e : world.getEvents()) {
            Rectangle a = e.getArea();
            shapes.rect(a.x, a.y, a.width, a.height);
        }
        shapes.setColor(Color.GREEN);
        Rectangle exit = world.getMap().getExitArea();
        shapes.rect(exit.x, exit.y, exit.width, exit.height);
    }

    private void drawGoose(Goose g) {
        Vector2 p = g.getPosition();

        // Aggro ring (faint), leash ring while chasing
        shapes.setColor(1f, 1f, 1f, 0.15f);
        shapes.circle(p.x, p.y, g.getAggroRadius(), 32);
        if (g.isChasing()) {
            shapes.setColor(1f, 0.2f, 0.2f, 0.25f);
            shapes.circle(p.x, p.y, g.getLeashRadius(), 48);
        }

        // Home
        Vector2 h = g.getHome();
        shapes.setColor(0.3f, 0.5f, 1f, 1f);
        shapes.rect(h.x - 3, h.y - 3, 6, 6);

        // Goal
        if (g.hasGoal()) {
            shapes.setColor(1f, 0.9f, 0.1f, 1f);
            shapes.line(p, g.getGoal());
            shapes.circle(g.getGoal().x, g.getGoal().y, 4f);
        }

        // Route
        shapes.setColor(0.1f, 0.9f, 1f, 1f);
        Vector2 prev = p;
        for (Vector2 wp : g.getPath()) {
            shapes.line(prev, wp);
            shapes.circle(wp.x, wp.y, 2f);
            prev = wp;
        }

        // Stuck flag
        if (g.getStuckSeconds() > 0.2f) {
            shapes.setColor(1f, 0f, 0f, 1f);
            shapes.circle(p.x, p.y, g.getRadius() + 3f);
        }
    }

    private void drawLabels(OrthographicCamera cam, SpriteBatch batch) {
        batch.setProjectionMatrix(cam.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        for (Goose g : world.getGeese()) {
            String label = g.getStateName();
            if (g.getUnstickCount() > 0) {
                label += "  unstuck x" + g.getUnstickCount();
            }
            font.draw(batch, label, g.getPosition().x - 20f, g.getPosition().y + g.getRadius() + 12f);
        }
        batch.end();
    }

    @Override
    public void dispose() {
        shapes.dispose();
        font.dispose();
    }
}