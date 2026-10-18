package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.GameCamera;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.WorldRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.Hud;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.PauseMenu;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.AssetPaths;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameResult;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.WorldListener;

/**
 * One run of the game. Owns the run's state ( ready / running / paused / finished),
 * reads input and connects the world, renderer and UI together.
 * Also listens to the world so it can show pop up messages.
 * 
 * <p> WASD to move, 1-9 for inventory, ESC to pause, 
 * F3 for world debug, F4 for nav debug, F5 for AI debug. </p>
 */
public class GameScreen implements Screen, WorldListener {
    private enum GameState {
        READY, RUNNING, PAUSED, FINISHED
    }

    private final GoosePatrol game;
    private final PauseMenu pauseMenu;
    private final Hud hud;
    private final GameWorld world;
    private final GameCamera camera;
    private final WorldRenderer worldRenderer;
    private final Vector2 moveInput = new Vector2();

    private GameState state = GameState.READY;

    public GameScreen(GoosePatrol game) {
        this.game = game;
        Skin skin = game.getAssetManager().get(AssetPaths.UI_SKIN, Skin.class);
        TiledMap map = game.getAssetManager().get(AssetPaths.CAMPUS_MAP, TiledMap.class);

        world = new GameWorld(game.getConfig(), map, this);
        camera = new GameCamera(game.getConfig().camera, world.getWidth(), world.getHeight());
        camera.snapTo(world.getPlayer().getPosition());
        worldRenderer = new WorldRenderer(world, map, camera, game.getBatch(), game.getAssetManager());
        hud = new Hud(game.getBatch(), skin);

        pauseMenu = new PauseMenu(
            game.getBatch(), 
            skin, 
            () -> enterState(GameState.RUNNING), 
            () -> game.setScreen(new GameScreen(game)), 
            () -> game.setScreen(new MainMenuScreen(game)),
            "");  
    }

    /** Only change state here to keep text and input routing in sync. */
    private void enterState(GameState newState) {
        state = newState;
        switch (state) {
            case READY -> {
                pauseMenu.setTitle("Don't miss the bus!");
                pauseMenu.setResumeText("Start");
                pauseMenu.setResumeVisible(true);
                Gdx.input.setInputProcessor(pauseMenu.getStage());
            }
            case PAUSED -> {
                pauseMenu.setTitle("PAUSED");
                pauseMenu.setResumeText("Resume");
                pauseMenu.setResumeVisible(true);
                Gdx.input.setInputProcessor(pauseMenu.getStage());
            }
            case RUNNING -> Gdx.input.setInputProcessor(null);
            case FINISHED -> {
                pauseMenu.setTitle(buildTicket());
                pauseMenu.setResumeVisible(false);
                Gdx.input.setInputProcessor(pauseMenu.getStage());
            }
        }
    }

    private String buildTicket() {
        GameResult r = world.getResult();
        int secs = (int) r.secondsUsed();
        return (r.won() ? "You caught the bus!" : "You missed the bus...")
                + "\n\n -- BUS TICKET" 
                + String.format("%nTime taken: $d:%02d", secs / 60, secs % 60)
                + "\nItems used: " + r.itemsUsed()
                + "\nEvents triggered: " + r.eventsTriggered();
    }

    @Override
    public void show() {
        enterState(state);
    }

    @Override
    public void render(float delta) {
        handleHotKeys();

        if (state == GameState.RUNNING) {
            readMoveInput();
            readItemKeys();
            world.update(delta, moveInput);
            camera.follow(world.getPlayer().getPosition(), delta);
            if (world.getStatus() != GameWorld.Status.RUNNING) {
                enterState(GameState.FINISHED);
            }
        } else {
            pauseMenu.act(delta);
        }
        hud.update(world);
        hud.act(delta);

        ScreenUtils.clear(Color.BLACK);
        worldRenderer.render();
        hud.draw();
        if (state != GameState.RUNNING) {
            pauseMenu.draw();
        }
    }

    /** ESC toggles between running and paused, Ignored on the ready and finished screens state. F3 toggles debug. */
    private void handleHotKeys() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.F3)) {
            worldRenderer.toggleWorldDebug();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F4)) {
            worldRenderer.toggleNavDebug();
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.F5)) {
            worldRenderer.toggleAiDebug();
        }
        if (!Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            return;
        }
        if (state == GameState.RUNNING) {
            enterState(GameState.PAUSED);
        } else if (state == GameState.PAUSED) {
            enterState(GameState.RUNNING);
        }
    }

    private void readMoveInput() {
        moveInput.set(
                (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0),
                (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0));
    }

    /** Keys 1-9 use inventory slots 0-8. */
    private void readItemKeys() {
        for (int slot = 0; slot < 9; slot++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + slot)) {
                world.useItem(slot);
            }
        }
    }

    @Override 
    public void onBusRefused() {
        hud.showToast("\"No geese on the bus\" Lose them first.");
    }

    @Override 
    public void onEventTriggered(String eventName, String message) {
        hud.showToast(message);
    }

    @Override 
    public void onItemUsed(String itemName) {
        hud.showToast("Used " + itemName);
    }

    public void onItemPickedUp(String itemName) {
        hud.showToast("Picked up " + itemName);
    }

 
    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;
        camera.resize(width, height);
        hud.resize(width, height);
        pauseMenu.resize(width, height);
    }

    @Override
    public void pause() {
        if (state == GameState.RUNNING)
            enterState(GameState.PAUSED);
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
        worldRenderer.dispose();
    }

}
