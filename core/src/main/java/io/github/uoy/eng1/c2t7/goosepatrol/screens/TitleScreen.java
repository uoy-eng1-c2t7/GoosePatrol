package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;

/** Title screen, first screen to be displayed */
public class TitleScreen implements Screen {
    private final GoosePatrol game;
    private final OrthographicCamera camera;
    private final Viewport viewport;
    private final GlyphLayout layout;

    public TitleScreen(GoosePatrol game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new ExtendViewport(1280, 720, camera);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.justTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {
            game.setScreen(new GameScreen(game));
        }

        ScreenUtils.clear(Color.BLACK);
        viewport.apply();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        drawCentered("Goose Patrol", game.font, viewport.getWorldHeight() * 0.6f);
        drawCentered("Press to Start", game.font, viewport.getWorldHeight() * 0.4f);
        game.batch.end();
    }

    private void drawCentered(String text, BitmapFont font, float y) {
        layout.setText(font, text);
        font.draw(game.batch, text, (viewport.getWorldWidth() - layout.width) / 2f, y);
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;

        viewport.update(width, height, true);
    }

    @Override
    public void show() {
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