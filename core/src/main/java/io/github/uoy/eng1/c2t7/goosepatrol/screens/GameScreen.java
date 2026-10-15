package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.WorldRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.PauseMenu;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.AssetPaths;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;

/**
 * One run of the game.
 */
public class GameScreen implements Screen {
    /**
     * READY = intro overlay (start paused), RUNNING = playing, PAUSED = pause
     * overlay
     */
    private enum GameState {
        READY, RUNNING, PAUSED
    }

    // Force the game to start in READY state
    private GameState state = GameState.READY;

    private final AssetManager assets;
    private final GoosePatrol game;
    private final PauseMenu pauseMenu;

    private final WorldRenderer worldRenderer;
    private final GameWorld world;

    public GameScreen(GoosePatrol game) {
        this.game = game;
        this.assets = game.getAssetManager();
        Skin skin = assets.get(AssetPaths.UI_SKIN, Skin.class);

        this.world = null;
        this.worldRenderer = null;

        pauseMenu = new PauseMenu(
            game.getBatch(),  
            skin,
            () -> resumeGame(),
            () -> restartGame(),
            () -> game.setScreen(new MainMenuScreen(game)),
            "Are you Ready?"
        );
    }

    private void resumeGame() {
        state = GameState.RUNNING;

        // Reset the pause menu text back to normal 
        pauseMenu.setTitle("PAUSED");
        pauseMenu.setResumeText("Resume");

        // Give input back to hud
    }

    private void pauseGame() {
        state = GameState.PAUSED;
        Gdx.input.setInputProcessor(pauseMenu.getStage()); // Route clicks to pause menu
    }

    private void restartGame() {
        resume(); // need to reset game entities here
    }

    @Override
    public void show() {
        // State is READY so immediatley give input to pause menu
        Gdx.input.setInputProcessor(pauseMenu.getStage());
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.PINK);

        // Update logic 
        if (state == GameState.RUNNING) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            pauseGame();
        }
            // update world state
        } else if (state == GameState.PAUSED || state == GameState.READY) {
            pauseMenu.act(delta);
        } 

        // Draw World
        
        // Draw UI overlays
        if (state == GameState.PAUSED || state == GameState.READY) {
            pauseMenu.draw();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        pauseMenu.resize(width, height);
    }

    @Override
    public void pause() {
        // If player tabs out, pause the game
        if (state == GameState.RUNNING) {
            pauseGame();
        }
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
        pauseMenu.dispose();
    }

}
