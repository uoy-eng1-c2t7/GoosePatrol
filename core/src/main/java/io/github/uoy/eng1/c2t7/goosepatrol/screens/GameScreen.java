package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;

public class GameScreen implements Screen {
    private final GoosePatrol game;
    final FitViewport fitViewport;
    private final TextureRegion playerSprite;
    final SpriteBatch batch;
    OrthographicCamera camera;


    public GameScreen(GoosePatrol game) {
        this.game = game;
        camera = new OrthographicCamera();
        fitViewport = new FitViewport(8, 5, camera);

        playerSprite = game.getAssets().getSprite("student.png");
        batch = game.getBatch();


    }

    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {


        ScreenUtils.clear(Color.PURPLE);
        batch.setProjectionMatrix(fitViewport.getCamera().combined);

        batch.begin();
        camera.position.x = 0;
        camera.position.y = 0;

        batch.draw(playerSprite, 100, 100, playerSprite.getRegionWidth(), playerSprite.getRegionHeight());
        batch.end();
    }

    @Override
    public void resize(int width, int height) {

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
