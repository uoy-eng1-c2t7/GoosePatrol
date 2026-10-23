package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.Assets;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.DroppedItem;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;

/**
 * Handles rendering the GameWorld to the screen.
 * Does not handle logic, just reads and renders.
 */
public class WorldRenderer implements Disposable {
    private final GameWorld world;
    private final SpriteBatch batch;
    private final Assets assets;
    private final OrthogonalTiledMapRenderer mapRenderer;
    private final TextureRegion playerSprite, gooseSprite;

    public WorldRenderer(GameWorld world, Assets assets, SpriteBatch batch) {
        this.world = world;
        this.batch = batch;
        this.assets = assets;
        mapRenderer = new OrthogonalTiledMapRenderer(assets.getMap(), batch);
        this.playerSprite = assets.getSprite("player.png");
        this.gooseSprite = assets.getSprite("goose.png");
    }

    public void render(OrthographicCamera camera) {
        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Entity entity : world.getEntities()) {
            if (entity instanceof Goose) {
                drawEntity(gooseSprite, entity);
            } else if (entity instanceof DroppedItem) {
                DroppedItem drop = (DroppedItem) entity;
                drawEntity(assets.getSprite(drop.getItem().getSpriteName()), entity);
            }
        }

        // Draw player last so its always ontop
        drawEntity(playerSprite, world.getPlayer());

        batch.end();
    }

    private void drawEntity(TextureRegion sprite, Entity entity) {
        Vector2 pos = entity.getPosition();
        float rotation = entity.getRotation();
        float width = sprite.getRegionWidth();
        float height = sprite.getRegionHeight();

        batch.draw(
                sprite,
                pos.x - width / 2f, // Bottom left x
                pos.y - height / 2f, // Bottom left y
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                rotation);
    }

    public void dispose() {
        mapRenderer.dispose();
    }
}
