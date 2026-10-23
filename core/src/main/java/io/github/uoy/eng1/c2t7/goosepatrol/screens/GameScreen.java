package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Player;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.InventorySlot;

public class GameScreen implements Screen {
    private final GoosePatrol game;
    final FitViewport fitViewport;
    private final TextureRegion playerSprite;
    final SpriteBatch batch;
    OrthographicCamera camera;
    final Player player;

    private final Texture inventorySlotSprite;
    private boolean inventoryVisible;
    final InventorySlot slot1;


    public GameScreen(GoosePatrol game) {
        this.game = game;
        camera = new OrthographicCamera();
        fitViewport = new FitViewport(1280, 720, camera);

        playerSprite = game.getAssets().getSprite("student.png");
        batch = game.getBatch();
        player = new Player(new Vector2(), playerSprite);

        inventorySlotSprite = game.getAssets().getInventorySlot();
        slot1 = new InventorySlot(inventorySlotSprite, 1);
        inventoryVisible = false;

    }

    @Override
    public void show() {
    }


    @Override
    public void render(float delta) {
        int SPEED = 1000;

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) player.position.x += SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))  player.position.x -= SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.UP))    player.position.y += SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN))  player.position.y -= SPEED * delta;
        if (Gdx.input.isKeyJustPressed(Input.Keys.E))     toggleInventory();

        camera.position.x = 0;
        camera.position.y = 0;

        camera.update();

        ScreenUtils.clear(Color.PURPLE);
        fitViewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        player.draw(batch);

        if (inventoryVisible) {
            slot1.draw(batch, 0, 0);
        }
        batch.end();
    }

    private void toggleInventory() {
        inventoryVisible = !inventoryVisible;
    }

    @Override
    public void resize(int width, int height) {
        fitViewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {

    }
}
