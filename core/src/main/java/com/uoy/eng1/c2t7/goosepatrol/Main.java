package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    // Shared SpriteBatch
    public SpriteBatch batch;

    @Override
    public void create() {
        batch = new SpriteBatch();

        setScreen(new TitleScreen(this));
    }

    @Override 
    public void render() {
        super.render();
    }

    @Override
    public void dispose() {
        batch.dispose();
    }
}
