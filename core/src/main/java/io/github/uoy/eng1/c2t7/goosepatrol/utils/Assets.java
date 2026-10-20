package io.github.uoy.eng1.c2t7.goosepatrol.utils;

import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapLoader;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;

/**
 * Every file the game loads, the only class that knows where they are.
 * Screens and renderers ask this for what they need so they don't have
 * to manage file paths or the AssetManager themselves.
 *
 * <p> To add an asset: add a path constant, queue it in {@link #queueGame()}
 * and add a getter. </p>
 * <p> To add a sprite: drop the PNG into the atlas. </p>
 */
public class Assets implements Disposable {
    private static final String SKIN = "ui/uiskin.json";
    private static final String LOGO = "ui/logo.png";
    private static final String SPRITES = "sprites/game.atlas";
    private static final String MAP = "tiled/campus.tmx";

    private final AssetManager manager = new AssetManager();
    private final Set<String> warned = new HashSet<>();
    private TextureRegion missing; // Only created if a sprite lookup fails

    /** Load things needed initially for the title and loading screen (fast). */
    public void loadBoot() {
        manager.load(SKIN, Skin.class);
        manager.load(LOGO, Texture.class);
        manager.finishLoading();
    }

    /** Queues the gameplay assets. The call {@link #update()} each frame until it returns true. */
    public void queueGame() {
        manager.setLoader(TiledMap.class, new TmxMapLoader());
        manager.load(SPRITES, TextureAtlas.class);
        manager.load(MAP, TiledMap.class);
    }

    /** Loads a bit more, returns true when everything queued has finished.
     * @param ms how many miliseconds of loading to do each frame.
     */
    public boolean update(int ms) {
        return manager.update(ms);
    }

    /** 0 to 1 for loading progress UI. */
    public float getProgress() {
        return manager.getProgress();
    }

    public Skin getSkin() {
        return  manager.get(SKIN, Skin.class);
    }

    public Texture getLogo() {
        return manager.get(LOGO, Texture.class);
    }

    public TiledMap getMap() {
        return manager.get(MAP, TiledMap.class);
    }

    /**
     * A sprite by its name in the atlas ("goose", "player", "bread"...).
     * A missing name shows a magenta square and logs once instead of crashing.
     */
    public TextureRegion getSprite(String name) {
        TextureRegion region = manager.get(SPRITES, TextureAtlas.class).findRegion(name);
        if (region != null) {
            return region;
        }
        if (warned.add(name)) {
            Gdx.app.error("Assets", "No sprite named '" + name + "' in the atlas");
        }
        return missingSprite();
    }

    /** Animation frames, e.g. goose_walk_0, goose_walk_1, ... */
    public Array<TextureAtlas.AtlasRegion> getFrames(String name) {
        return manager.get(SPRITES, TextureAtlas.class).findRegions(name);
    }

    /** Returns a magenta pixel that is used in place of missing sprites. Created only if required */
    private TextureRegion missingSprite() {
        if (missing == null) {
            Pixmap p = new Pixmap(16, 16, Pixmap.Format.RGBA8888);
            p.setColor(1f, 0f, 1f, 1f);
            p.fill();
            missing = new TextureRegion(new Texture(p));
            p.dispose();
        }

        return missing;
    }

    @Override
    public void dispose() {
        manager.dispose();
        if (missing != null) {
            missing.getTexture().dispose();
        }
    }
}
