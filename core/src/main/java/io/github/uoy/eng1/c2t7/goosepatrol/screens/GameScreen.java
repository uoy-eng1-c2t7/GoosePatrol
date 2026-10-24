package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemStack;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.CameraController;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.DebugRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.WorldRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.HudUI;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.PauseMenuUI;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.PauseMenuUI.PauseListener;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.WorldListener;

public class GameScreen extends InputAdapter implements Screen, WorldListener, PauseListener {
    private final GoosePatrol game;
    private final GameWorld world;
    private final WorldRenderer renderer;
    private final DebugRenderer debugRenderer;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final CameraController cameraController;
    private final ExtendViewport viewport;
    private final Vector2 moveInput = new Vector2();

    // UI
    private final Stage uiStage;
    private final HudUI hudUI;
    private final PauseMenuUI pauseMenuUI;

    // State
    private boolean isPaused = false;
    private float timeRemaining;

    public GameScreen(GoosePatrol game) {
        this.game = game;
        GameConfig config = game.getConfig();
        this.timeRemaining = config.gameDurationSeconds;

        // World and Renderer setup
        world = new GameWorld(config, this, game.getAssets().getMap());
        renderer = new WorldRenderer(world, game.getAssets(), game.getBatch());
        debugRenderer = new DebugRenderer(world, game.getBatch());
        viewport = new ExtendViewport(config.camera.viewWidth, config.camera.viewHeight, camera);
        this.cameraController = new CameraController(camera, world.getMap().getWidth(), world.getMap().getHeight(),
                config.camera);

        // UI stage setup
        uiStage = new Stage(new FitViewport(1280, 720), game.getBatch());
        hudUI = new HudUI(world.getPlayer().getInventory(), game.getAssets());
        pauseMenuUI = new PauseMenuUI(game, game.getAssets().getSkin(), this);
        uiStage.addActor(hudUI);
        uiStage.addActor(pauseMenuUI);
    }

    @Override
    public void show() {
        cameraController.setTarget(world.getPlayer().getPosition());

        // Multiplex input - UI recieves clicks first, if not handled go to gamescreen
        InputMultiplexer multiplexer = new InputMultiplexer(uiStage, this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        // Update game - freeze when paused
        if (!isPaused) {
            timeRemaining = Math.max(0, timeRemaining - delta);
            if (timeRemaining <= 0) {
                game.setScreen(new TicketScreen(game, false, 0));
            }
            hudUI.updateTimer(timeRemaining);

            moveInput.set(
                    (Gdx.input.isKeyPressed(Input.Keys.D) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.A) ? 1 : 0),
                    (Gdx.input.isKeyPressed(Input.Keys.W) ? 1 : 0) - (Gdx.input.isKeyPressed(Input.Keys.S) ? 1 : 0));
            world.update(delta, moveInput);
            cameraController.update(delta);
        }

        boolean sprintPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        world.getPlayer().setSprinting(sprintPressed);

        // Render Game World
        ScreenUtils.clear(Color.BLACK);
        renderer.render(camera);
        debugRenderer.render(camera);

        // Render UI layer
        uiStage.act(delta);
        uiStage.draw();
    }

    // Input Handling
    @Override
    public boolean keyDown(int keycode) {
        // Toggle Pause
        if (keycode == Input.Keys.ESCAPE) {
            togglePause();
            return true;
        }

        if (isPaused)
            return false;

        // Number keys 1-9 for hotbar
        if (keycode >= Input.Keys.NUM_1 && keycode <= Input.Keys.NUM_9) {
            int slot = keycode - Input.Keys.NUM_1;
            world.getPlayer().getInventory().selectSlot(slot);
            hudUI.updateHotbar();
            return true;
        }

        // Use item
        if (keycode == Input.Keys.E) {
            useActiveItem();
            return true;
        }

        // Toggle debug
        if (keycode == Input.Keys.F3) {
            debugRenderer.toggle();
        }

        return false;
    }

    @Override
    public boolean scrolled(float amountX, float amountY) {
        if (isPaused)
            return false;
        // Scroll wheel cycles hotbar slots
        var inv = world.getPlayer().getInventory();
        int next = (inv.getSelectedSlot() + (int) Math.signum(amountY)) % inv.getSlotCount();
        if (next < 0)
            next += inv.getSlotCount();
        inv.selectSlot(next);
        hudUI.updateHotbar();
        return true;
    }

    private void useActiveItem() {
        ItemStack active = world.getPlayer().getInventory().getActiveStack();
        if (active != null) {
            boolean consumed = active.getItem().use(world, world.getPlayer());
            if (consumed) {
                world.getPlayer().getInventory().consumeActiveItem();
                hudUI.showToast("Used " + active.getItem().getDisplayName());
            }
            hudUI.updateHotbar();
        }
    }

    public void togglePause() {
        isPaused = !isPaused;
        pauseMenuUI.setVisible(isPaused);
    }

    @Override
    public void onResume() {
        togglePause();
    }

    @Override
    public void onRestart() {
        game.setScreen(new GameScreen(game));
    }

    @Override
    public void onExitReached(boolean isChased) {
        if (isChased) {
            hudUI.showToast("Driver: 'NO GEESE ON THE BUS! SHAKE THEM OFF'");
        } else {
            float timeTaken = game.getConfig().gameDurationSeconds - timeRemaining;
            game.setScreen(new TicketScreen(game, true, timeTaken));
        }
    }

    @Override
    public void onInventoryChanged() {
        hudUI.updateHotbar();
    }

    @Override
    public void onPlayerPecked() {
        game.getAssets().getSound("pain").play(game.getConfig().audio.sfxVolume);
        hudUI.showToast("OUCH! Pecked by a goose!");
    }

    @Override
    public void resize(int width, int height) {
        // If window is minimised it is 0, so don't do anything
        if (width <= 0 || height <= 0)
            return;
        uiStage.getViewport().update(width, height, true);
        viewport.update(width, height);
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
        renderer.dispose();
        uiStage.dispose();
    }
}
