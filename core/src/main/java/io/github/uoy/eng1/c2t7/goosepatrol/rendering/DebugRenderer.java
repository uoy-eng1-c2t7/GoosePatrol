package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;

/**
 * Debug overlay for rendering things that wouldn't 
 * usually be seen like data, bounds etc. 
 */
public class DebugRenderer implements Disposable {
    private final GameWorld world;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final SpriteBatch batch;
    private boolean enabled;

    public DebugRenderer(GameWorld world, SpriteBatch batch) {
        this.world = world;
        this.batch = batch;
        font.getData().setScale(0.5f);
        font.setUseIntegerPositions(false);
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void render(OrthographicCamera camera) {
        if (!enabled) return;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);

        // Draw entity bounds
        shapes.setColor(Color.RED);
        for (Entity e : world.getEntities()) {
            draw(e.getBounds());
        }

        shapes.end();
    }

    private void draw(Circle c) {
        shapes.circle(c.x, c.y, c.radius);
    }

    private void draw(Rectangle r) {
        shapes.rect(r.x, r.y, r.width, r.height);
    }

    public void dispose() {
        shapes.dispose();
        font.dispose();
    }
}
