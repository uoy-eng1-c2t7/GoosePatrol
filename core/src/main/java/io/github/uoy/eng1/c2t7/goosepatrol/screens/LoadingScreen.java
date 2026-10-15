package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.AssetPaths;

/** Loading screen so we can load heavier assets (e.g map, sounds..) without freezing. */
public class LoadingScreen implements Screen {
    private final GoosePatrol game;
    private final AssetManager assets;
    private final Stage stage = new Stage(new FitViewport(1280, 720));
    private final Label percent;
    private float shown = 0f;

    public LoadingScreen(GoosePatrol game) {
        this.game = game;
        this.assets = game.getAssetManager();

        Skin skin = assets.get(AssetPaths.UI_SKIN, Skin.class);
        percent = new Label("Loading... 0%", skin);

        // Table to hold loading text in center of screen
        Table root = new Table();
        root.setFillParent(true);
        root.add(percent);
        stage.addActor(root);

        queueGameplayAssets();
    }

    @Override
    public void render(float delta) {
        shown += delta;
        boolean loaded = assets.update(); // up to 16ms of loading each frame
        percent.setText("Loading... " + (int) (assets.getProgress() * 100) + "%"); 

        ScreenUtils.clear(Color.WHITE);
        stage.act(delta);
        stage.draw();

        // When everything is loaded and minimum load time has passed (prevents flickering)
        if (loaded && shown >= game.getConfig().minLoadTimeSeconds) {
            game.setScreen(new GameScreen(game));
        }
    }

    /** Queues everything needed for a run. */
    private void queueGameplayAssets() {
        assets.load(AssetPaths.CAMPUS_MAP, TiledMap.class);
    }

    @Override  
    public void resize(int width, int height) {
        // If window is minimised it is 0, so don't do anything
        if (width <= 0 || height <= 0)
            return;
        stage.getViewport().update(width, height, true);
    }

    @Override 
    public void hide() {
        dispose();
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
    public void dispose() {
        stage.dispose();
    }
}