package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.PauseMenu;

/**
 * One run of the game.
 */
public class GameScreen implements Screen{
    /** READY = intro overlay (start paused), RUNNING = playing, PAUSED = pause overlay */
    private enum GameState { READY, RUNNING, PAUSED }
    
    private final GoosePatrol game;
    private final PauseMenu pauseMenu;


    public GameScreen(GoosePatrol game) {
        this.game = game;

        pauseMenu = new PauseMenu(game.getBatch(),  game.getAssetManager().get(null), null, null, null)
    }

    @Override
    public void show() {
        
    }

    @Override
    public void render(float delta) {
        
    }

    @Override
    public void resize(int width, int height) {
        
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
        
    }
    
}
