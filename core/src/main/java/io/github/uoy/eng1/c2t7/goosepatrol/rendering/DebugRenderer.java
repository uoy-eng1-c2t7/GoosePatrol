package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.*;

public class DebugRenderer implements Disposable {
    private final GameWorld world;
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final SpriteBatch batch;
    private boolean enabled;

    public DebugRenderer(GameWorld world, SpriteBatch batch) {
        this.world = world;
        this.batch = batch;
        font.getData().setScale(0.8f);
        font.setUseIntegerPositions(false);
    }

    public void toggle() {
        enabled = !enabled;
    }

    public void render(OrthographicCamera camera) {
        if (!enabled) return;

        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Line);

        // Walls
        shapes.setColor(Color.GREEN);
        for (Rectangle wall : world.getMap().getWalls()) {
            shapes.rect(wall.x, wall.y, wall.width, wall.height);
        }

        // Exit (Blue)
        if (world.getMap().getExitZone() != null) {
            shapes.setColor(Color.CYAN);
            Rectangle exit = world.getMap().getExitZone();
            shapes.rect(exit.x, exit.y, exit.width, exit.height);
        }

        // Entities
        for (Entity e : world.getEntities()) {
            Vector2 pos = e.getPosition();
            Circle b = e.getBounds();

            if (e instanceof Player) {
                shapes.setColor(Color.SKY);
                shapes.circle(b.x, b.y, b.radius);
            } else if (e instanceof DroppedItem) {
                shapes.setColor(Color.ORANGE);
                shapes.circle(b.x, b.y, b.radius);
            } else if (e instanceof Goose) {
                Goose goose = (Goose) e;

                // Color based on Goose State
                if (goose.isChasing()) {
                    shapes.setColor(Color.RED); // Aggressive!
                } else if (goose.getState() == Goose.State.DISTRACTED) {
                    shapes.setColor(Color.MAGENTA); // Eating / distracted
                } else {
                    shapes.setColor(Color.YELLOW); // Wandering
                }
                shapes.circle(b.x, b.y, b.radius);

                // Draw Sight Radius
                shapes.setColor(1f, 1f, 0f, 0.25f);
                shapes.circle(pos.x, pos.y, 140f);

                //Draw path
                for (int i = 0; i < goose.path.size; i++) {
                    Vector2 step = goose.path.get(i);
                    shapes.setColor(Color.RED);
                    shapes.circle(step.x, step.y, 1f);
                }
            }

            // Draw Velocity Direction Rays
            if (!e.getVelocity().isZero()) {
                shapes.setColor(Color.WHITE);
                Vector2 end = pos.cpy().add(e.getVelocity().cpy().nor().scl(25f));
                shapes.line(pos.x, pos.y, end.x, end.y);
            }
        }
        shapes.end();

        // DRAW TEXT LABELS
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Entity e : world.getEntities()) {
            if (e instanceof Goose) {
                Goose goose = (Goose) e;
                // Floating text above goose: "State: CHASING"
                font.setColor(goose.isChasing() ? Color.RED : Color.WHITE);
                font.draw(batch, goose.getState().name(), e.getPosition().x - 20, e.getPosition().y + 25);
            }
        }

        // Stats
        font.setColor(Color.YELLOW);
        float startX = camera.position.x - (camera.viewportWidth / 2f) + 10;
        float startY = camera.position.y + (camera.viewportHeight / 2f) - 10;

        Player p = world.getPlayer();
        String stats = String.format(
            "FPS: %d | Entities: %d\nPos: (%.1f, %.1f) | Angle: %.1f°\nSpeed: %.1f | Stamina: %.0f%%",
            Gdx.graphics.getFramesPerSecond(),
            world.getEntities().size,
            p.getPosition().x, p.getPosition().y, p.getRotation(),
            p.getVelocity().len(),
            p.getStaminaPercent() * 100f
        );
        font.draw(batch, stats, startX, startY);

        batch.end();
    }

    public void dispose() {
        shapes.dispose();
        font.dispose();
    }
}
