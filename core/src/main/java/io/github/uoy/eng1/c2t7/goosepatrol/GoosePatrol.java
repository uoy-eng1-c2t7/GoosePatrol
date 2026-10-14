package io.github.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
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
    private GameConfig config;

    @Override
    public void create() {
        loadAssets();
        loadConfig();
        setScreen(new MainMenuScreen(this));
    }

    public AssetManager getAssetManager() {
        return assetManger;
    }

    public GameConfig getConfig() {
        return config;
    }

    private void loadAssets() {
        // Tell the AssetManager that we are using a non-default loader
        InternalFileHandleResolver resolver = new InternalFileHandleResolver();
        assetManger.setLoader(TiledMap.class, new TmxMapLoader(resolver)); // For loading Tiled maps

        // Load all of our assets
        // assetManger.load("campus.tmx", TiledMap.class);
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