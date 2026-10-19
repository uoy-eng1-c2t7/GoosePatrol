package io.github.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.Assets;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.screens.MainMenuScreen;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GoosePatrol extends Game {
    private Assets assets;
    private SpriteBatch batch;
    private GameConfig config;

    @Override
    public void create() {
        config = GameConfig.load(Gdx.files.internal("config.json"));
        batch = new SpriteBatch();
        assets = new Assets();
        assets.loadBoot(); // load skin + logo so first screen can draw
        setScreen(new MainMenuScreen(this));
    }

    public Assets getAssets() {
        return assets;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    public GameConfig getConfig() {
        return config;
    }

    @Override 
    public void dispose() {
        super.dispose();
        batch.dispose();
        assets.dispose();
    }
}