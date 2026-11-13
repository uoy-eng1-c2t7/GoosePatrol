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

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.events.Event;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Item;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemStack;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.CameraController;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.DebugRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.rendering.WorldRenderer;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.HudUI;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.PauseMenuUI;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.StartOverlayUI;
import io.github.uoy.eng1.c2t7.goosepatrol.ui.PauseMenuUI.PauseListener;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Assets;
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
    private final StartOverlayUI startOverlay;

    // State
    private boolean isPaused = true;
    private boolean waitingToStart = true;

    private static final float BUS_DEPART_SECONDS = 1.2f;
    private float departTimer = 0f;

    public GameScreen(GoosePatrol game) {
        this.game = game;
        GameConfig config = game.getConfig();

        Assets assets = game.getAssets();

        // World and Renderer setup
        world = new GameWorld(config, this, assets.getMap());
        renderer = new WorldRenderer(world, assets, game.getBatch());
        debugRenderer = new DebugRenderer(world, game.getBatch());
        viewport = new ExtendViewport(config.camera.viewWidth, config.camera.viewHeight, camera);
        this.cameraController = new CameraController(camera, world.getMap().getWidth(), world.getMap().getHeight(),
                config.camera);

        // UI stage setup
        uiStage = new Stage(new ExtendViewport(1280, 720), game.getBatch());
        hudUI = new HudUI(world.getPlayer().getInventory(), assets, game.getConfig().player.maxHearts);
        pauseMenuUI = new PauseMenuUI(game, assets.getSkin(), this);
        hudUI.updateGeese(0, world.getGeeseToDisperse());
        uiStage.addActor(hudUI);
        uiStage.addActor(pauseMenuUI);
        startOverlay = new StartOverlayUI(assets.getSkin(), world.getGeeseToDisperse());
        uiStage.addActor(startOverlay);
    }

    @Override
    public void show() {
        cameraController.setTarget(world.getPlayer().getPosition());

        if (isPaused) {
            cameraController.snapToTarget(); // The camera doesn't update itself while paused
        }

        // Multiplex input - UI recieves clicks first, if not handled go to gamescreen
        InputMultiplexer multiplexer = new InputMultiplexer(uiStage, this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void render(float delta) {
        delta = Math.min(delta, 0.05f); // A long frame must not skip through walls
        // Update game - freeze when paused
        if (departTimer > 0f) {
            departTimer -= delta;
            if (departTimer <= 0f) {
                showResults(true);
                return;
            }
        } else if (!isPaused) {
            readMoveInput();
            world.update(delta, moveInput);
            if (world.getStatus() == GameWorld.Status.LOST) {
                showResults(false);
                return; // This screen has been disposed, so stop drawing it
            }
            hudUI.updateTimer(world.getTimeRemaining());
            hudUI.updateStamina(world.getPlayer().getStaminaPercent());
            cameraController.update(delta);
        }

        boolean sprintPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT);
        world.getPlayer().setSprinting(sprintPressed);

        // Render Game World
        ScreenUtils.clear(Color.BLACK);
        renderer.render(camera, isPaused ? 0f : delta); // Pause animation if paused
        debugRenderer.render(camera);

        // Render UI layer
        uiStage.act(delta);
        uiStage.draw();
    }

    // Input Handling
    @Override
    public boolean keyDown(int keycode) {
        if (waitingToStart) {
            if (keycode == Input.Keys.SPACE) {
                startGame();
            }
            return true;
        }

        // Toggle Pause
        if (keycode == Input.Keys.ESCAPE || keycode == Input.Keys.SPACE) {
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
        Item used = world.useActiveItem();
        if (used != null) {
            hudUI.showToast("Used " + used.getDisplayName());
            hudUI.updateCounters(world.getItemsUsed(), world.getEventsTriggered());
        }
        hudUI.updateHotbar();
    }

    /** WASD or arrow keys (FR-02). */
    private void readMoveInput() {
        moveInput.set(
                (isDown(Input.Keys.D, Input.Keys.RIGHT) ? 1 : 0) - (isDown(Input.Keys.A, Input.Keys.LEFT) ? 1 : 0),
                (isDown(Input.Keys.W, Input.Keys.UP) ? 1 : 0) - (isDown(Input.Keys.S, Input.Keys.DOWN) ? 1 : 0));
    }

    private static boolean isDown(int key, int altKey) {
        return Gdx.input.isKeyPressed(key) || Gdx.input.isKeyPressed(altKey);
    }

    /** Switches to the results ticket (FR-01, FR-12, FR-13). */
    private void showResults(boolean won) {
        game.setScreen(new TicketScreen(game, won, world.getTimeTaken(),
                world.getItemsUsed(), world.getEventsTriggered()));
    }

    private void startGame() {
        waitingToStart = false;
        isPaused = false;
        startOverlay.setVisible(false);
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
    public void onExitReached() {
        departTimer = BUS_DEPART_SECONDS;
        renderer.startBusLeaving();
    }

    @Override
    public void onInventoryChanged() {
        hudUI.updateHotbar();
    }

    @Override
    public void onPlayerPecked() {
        game.getAssets().getSound("pain").play(game.getConfig().audio.sfxVolume);
        int hearts = world.getPlayer().getHearts();
        hudUI.updateHearts(hearts);
        if (hearts > 0) {
            hudUI.showToast("OUCH! " + hearts + " hearts left");
        }
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
    public void onGooseDispersed() {
        hudUI.updateGeese(world.getGeeseDispersed(), world.getGeeseToDisperse());
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

    @Override
    public void onEventTriggered(Event event) {
        hudUI.getEventUI().announceEvent(event);
        hudUI.updateCounters(world.getItemsUsed(), world.getEventsTriggered());
    }
}
