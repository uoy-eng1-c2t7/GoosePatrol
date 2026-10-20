package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;

/**
 * Loading screen to show when loading the heavy game assets.
 */
public class LoadingScreen implements Screen {
    private final GoosePatrol game;
    private final Stage stage;
    private final Label percent;
    private float shown = 0f;

    /**
     * @param game the main GoosePatrol game instance.
     */
    public LoadingScreen(GoosePatrol game) {
        this.game = game;
        stage = new Stage(new FitViewport(1280, 720), game.getBatch());

        // Loading progress text
        Skin skin = game.getAssets().getSkin();
        percent = new Label("Loading... 0%", skin);

        // Table to hold loading text in center of screen
        Table root = new Table();
        root.setFillParent(true);
        root.add(percent);
        stage.addActor(root);

        // Get the heavy game assets ready to be loaded
        game.getAssets().queueGame();
    }

    @Override
    public void render(float delta) {
        shown += delta;
        boolean loaded = game.getAssets().update(16); // 16ms of loading this frame
        percent.setText("Loading... " + ((int) (game.getAssets().getProgress() * 100)) + "%");

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
         // If window is minimised it is 0, so don't do anything
        if (width <= 0 || height <= 0)
            return;
        stage.getViewport().update(width, height, true);
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
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
    
}
