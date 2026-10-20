package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.entities.Player;

public class GameScreen implements Screen {
    private final GoosePatrol game;
    final FitViewport fitViewport;
    private final TextureRegion playerSprite;
    final SpriteBatch batch;
    OrthographicCamera camera;

    final Player player;
    final int PLAYER_SPEED = 100;


    public GameScreen(GoosePatrol game) {
        this.game = game;
        camera = new OrthographicCamera();
        fitViewport = new FitViewport(1280, 720, camera);

        playerSprite = game.getAssets().getSprite("student.png");
        batch = game.getBatch();
        player = new Player(new Vector2(), playerSprite, PLAYER_SPEED);

    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        player.velocity.set(0, 0);

        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) player.velocity.x += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) player.velocity.x -= 1;
        if (Gdx.input.isKeyPressed(Input.Keys.UP)) player.velocity.y += 1;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) player.velocity.y -= 1;

        player.velocity.nor();

        camera.position.x = player.position.x;
        camera.position.y = player.position.y;

        camera.update();

        ScreenUtils.clear(Color.PURPLE);
        fitViewport.apply();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        player.updatePosition(delta);
        player.draw(batch);
        batch.end();
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
