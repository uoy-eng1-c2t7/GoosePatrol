package io.github.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Json;

import io.github.uoy.eng1.c2t7.goosepatrol.screens.MainMenuScreen;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.AssetPaths;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class GoosePatrol extends Game {
    private final AssetManager assetManger = new AssetManager();
    private SpriteBatch batch;
    private GameConfig config;

    @Override
    public void create() {
        batch = new SpriteBatch();
        loadGlobalAssets();
        loadConfig();
        setScreen(new MainMenuScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManger;
    }

    public GameConfig getConfig() {
        return config;
    }

    public SpriteBatch getBatch() {
        return batch;
    }

    /** Loads the small set of assets that every screen needs, very fast so no need to  */
    private void loadGlobalAssets() {
        // Tell AssetManager how to load our map - Tmx isn't a default
        assetManger.setLoader(TiledMap.class, new TmxMapLoader());

        // Load all global assets - these use default loaders
        assetManger.load(AssetPaths.UI_LOGO, Texture.class);
        assetManger.load(AssetPaths.UI_SKIN, Skin.class);

        // Force the AssetManager to finish loading before continuing
        assetManger.finishLoading();
    }

    private void loadConfig() {
        Json json = new Json();
        config = json.fromJson(GameConfig.class, Gdx.files.internal("config.json"));
    }

    @Override
    public void dispose() {
        assetManger.dispose();
    }
}