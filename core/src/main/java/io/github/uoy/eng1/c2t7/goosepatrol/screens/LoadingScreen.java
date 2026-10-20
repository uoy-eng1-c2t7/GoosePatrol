package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;

/**
 * Loading screen to show when loading the heavy game assets.
 */
public class LoadingScreen implements Screen {
    private final GoosePatrol game;
    private final Stage stage;
    private final Label percentage;
    private float shown = 0f;

    /**
     * @param game the main GoosePatrol game instance.
     */
    public LoadingScreen(GoosePatrol game) {
        this.game = game;
        stage = new Stage(new FitViewport(1280, 720), game.getBatch());

        percentage = new Label("Loading... 0%", game.getAssets().getSkin());
        percentage.setAlignment(Align.center);

        Table table = new Table();
        table.setFillParent(true);
        table.add(percentage);
        stage.addActor(table);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(null);
        game.getAssets().queueGame();
    }

    @Override
    public void render(float delta) {
        shown += delta;
        boolean loaded = game.getAssets().update(16); // 16ms of loading this frame
        percentage.setText("Loading... " + ((int) (game.getAssets().getProgress() * 100)) + "%");

        ScreenUtils.clear(Color.WHITE);
        stage.act(delta);
        stage.draw();

        // When everything has finished loading and minimum load time has passed (prevents flickering)
        if (loaded && shown >= game.getConfig().minLoadTimeSeconds) {
            game.setScreen(new GameScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
    
}
