package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    // Shared resources.
    public SpriteBatch batch;
    public BitmapFont font;

    @Override 
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);
        Gdx.app.log("Main", "Game started");
        
        batch = new SpriteBatch();
        font = new BitmapFont();
        setScreen(new TitleScreen(this));
    }

    @Override
    public void dispose() {
        super.dispose();
        batch.dispose();
        font.dispose();
    }
}
