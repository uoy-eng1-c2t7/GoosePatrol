package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.Assets;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.DroppedItem;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;

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
    private final Animation<TextureRegion> playerWalk, gooseWalk, gooseEat, gooseFly;
    private float animationTime;
    private float playerWalkTime;

    private static final float BUS_SPEED = 420f;
    private static final float ARROW_DISTANCE = 56f;
    private static final float ARROW_HIDE_RANGE = 320f;

    private final TextureRegion busSprite, arrowSprite;
    private final Drawable bubble;
    private final BitmapFont font;
    private final GlyphLayout bubbleText = new GlyphLayout();
    private boolean busLeaving = false;
    private float busOffset = 0f;

    private static final float WALK_FRAME_SECONDS = 0.15f;

    public WorldRenderer(GameWorld world, Assets assets, SpriteBatch batch) {
        this.world = world;
        this.batch = batch;
        this.assets = assets;
        mapRenderer = new OrthogonalTiledMapRenderer(assets.getMap(), batch);
        this.gooseEat = new Animation<>(0.25f, assets.getFrames("goose_eat"), Animation.PlayMode.LOOP);
        this.gooseFly = new Animation<>(0.1f, assets.getFrames("goose_fly"), Animation.PlayMode.LOOP);
        this.playerSprite = assets.getSprite("player");
        this.gooseSprite = assets.getSprite("goose");
        this.busSprite = assets.getSprite("bus");
        this.arrowSprite = assets.getSkin().getRegion("arrow");
        this.bubble = assets.getSkin().getDrawable("panel");
        this.font = assets.getSkin().getFont("ds_goose_large");

        this.playerWalk = new Animation<>(WALK_FRAME_SECONDS,
                assets.getFrames("player_walk"), Animation.PlayMode.LOOP);

        this.gooseWalk = new Animation<>(WALK_FRAME_SECONDS,
                assets.getFrames("goose_walk"), Animation.PlayMode.LOOP);
    }

    public void render(OrthographicCamera camera, float delta) {
        animationTime += delta;

        Player player = world.getPlayer();
        playerWalkTime += delta * player.getVelocity().len() / player.getSpeed();

        mapRenderer.setView(camera);
        mapRenderer.render();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (busLeaving) {
            busOffset += BUS_SPEED * delta;
        }
        drawBus();

        for (Entity entity : world.getEntities()) {
            if (entity instanceof Goose) {
                drawEntity(gooseFrame((Goose) entity), entity);
            } else if (entity instanceof DroppedItem) {
                DroppedItem drop = (DroppedItem) entity;
                drawEntity(assets.getSprite(drop.getItem().getSpriteName()), entity);
            }
        }

        // Draw player last so its always ontop
        boolean blinkedOff = player.isInvulnerable() && (int) (animationTime * 10f) % 2 == 0;
        if (!blinkedOff && !busLeaving) {
            TextureRegion frame = player.getVelocity().isZero() ? playerSprite : playerWalk.getKeyFrame(playerWalkTime);
            drawEntity(frame, player);
        }

        drawBusArrow();
        drawDriverBubble();

        batch.end();
    }

    private TextureRegion gooseFrame(Goose goose) {
        switch (goose.getState()) {
            case EATING:
                return gooseEat.getKeyFrame(animationTime);
            case LEAVING:
                return gooseFly.getKeyFrame(animationTime);
            default:
                return goose.getVelocity().isZero() ? gooseSprite : gooseWalk.getKeyFrame(animationTime);
        }
    }

    /** Hides the player and drives the bus away. */
    public void startBusLeaving() {
        busLeaving = true;
    }

    private void drawBus() {
        Rectangle exit = world.getMap().getExitZone();
        if (exit != null) {
            float x = exit.x + (exit.width - busSprite.getRegionWidth()) / 2f + busOffset;
            batch.draw(busSprite, x, exit.y + 8f);
        }
    }

    /** Once enough geese are gone, points the player towards the bus. */
    private void drawBusArrow() {
        Rectangle exit = world.getMap().getExitZone();
        if (exit == null || busLeaving || !world.hasDispersedEnough()) {
            return;
        }
        Vector2 player = world.getPlayer().getPosition();
        Vector2 toBus = new Vector2(exit.x + exit.width / 2f, exit.y + exit.height / 2f).sub(player);
        if (toBus.len() < ARROW_HIDE_RANGE) {
            return;
        }
        float angle = toBus.angleDeg();
        toBus.nor().scl(ARROW_DISTANCE).add(player);
        float width = arrowSprite.getRegionWidth();
        float height = arrowSprite.getRegionHeight();
        batch.draw(arrowSprite, toBus.x - width / 2f, toBus.y - height / 2f,
                width / 2f, height / 2f, width, height, 1.5f, 1.5f, angle);
    }

    private void drawDriverBubble() {
        String message = world.getDriverMessage();
        if (message == null || busLeaving) {
            return;
        }
        Rectangle exit = world.getMap().getExitZone();
        bubbleText.setText(font, message);
        float width = bubbleText.width + 32f;
        float height = bubbleText.height + 28f;
        float x = Math.min(exit.x + exit.width / 2f - width / 2f, world.getMap().getWidth() - width - 8f);
        float y = exit.y + busSprite.getRegionHeight() + 20f;
        bubble.draw(batch, x, y, width, height);
        font.draw(batch, bubbleText, x + 16f, y + height - 14f);
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
