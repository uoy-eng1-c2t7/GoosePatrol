package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class TitleScreen implements Screen {
    private final Main game;
    private final ScreenViewport viewport = new ScreenViewport();

    public TitleScreen(Main game) {
        this.game = game;
    }

    @Override 
    public void show() {

    }

    @Override
    public void render(float delta) {
        // Space starts the game
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.setScreen(new GameScreen(game));
            return;
        }

        // Escape quits the game
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        ScreenUtils.clear(Color.BLUE);

        viewport.apply();
        game.batch.setProjectionMatrix(viewport.getCamera().combined);
        game.batch.begin();
        float midX = viewport.getWorldWidth() / 2f;
        float midY = viewport.getWorldHeight() / 2f;
        game.font.draw(game.batch, "GOOSE PATROL", midX - 40, midY + 20);
        game.font.draw(game.batch, "SPACE to start, ESC to quit", midX - 85, midY - 10);
        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0){
            return;
        }

        viewport.update(width, height, true);
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
