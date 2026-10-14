package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.AssetPaths;

/**
 * The main menu screen displayed when the game launches.
 * Handles UI layout and navigation to the main game loop.
 */
public class MainMenuScreen implements Screen {
    private final GoosePatrol game;
    private final Stage stage;

    /**
     * Constructs a new MainMenuScreen and initialises it with the main game
     * instance.
     * 
     * @param game the main GoosePatrol game instance
     */
    public MainMenuScreen(GoosePatrol game) {
        this.game = game;

        // Setup a Stage with a FitViewport so the UI scales on any monitor size
        stage = new Stage(new FitViewport(1280, 720)); // This is a virtual size

        // Tell libGDX to route all mouse clicks to this stage
        Gdx.input.setInputProcessor(stage);

        // Get loaded UI assets from AssetManager
        Skin skin = game.getAssetManager().get(AssetPaths.UI_SKIN, Skin.class);
        Texture logoTex = game.getAssetManager().get(AssetPaths.UI_LOGO, Texture.class);

        // Table to allign UI
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Build UI 
        createLogo(table, logoTex);
        createButtons(table, skin);
    };

    /**
     * Creates the animated logo and adds it to the layout table.
     * 
     * @param table The Scene2D layout table.
     * @param logoTex The loaded logo texture.
     */
    private void createLogo(Table table, Texture logoTex) {
        Image logoImage = new Image(logoTex);
        logoImage.setOrigin(Align.center);

        // Add a floating animation
        logoImage.addAction(Actions.forever(
            Actions.sequence(
                Actions.moveBy(0, 25f, 1.5f),
                Actions.moveBy(0, -25f, 1.5f)
            )
        ));

        table.add(logoImage).padBottom(50).row();
    }

    /**
     * Creates the Start and Exit buttons, adds their listeners, and packs them into the table.
     * 
     * @param table The Scene2D layout table.
     * @param skin  The loaded UI skin containing fonts and graphics.
     */
    private void createButtons(Table table, Skin skin) {
        TextButton startButton = new TextButton("Start Game", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        addHoverEffect(startButton);
        addHoverEffect(exitButton);

        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        table.add(startButton).width(250).height(60).padBottom(20).row();
        table.add(exitButton).width(250).height(60);
    }

    /**
     * Helper method to add reactive scaling to buttons when the mouse hovers over
     * them.
     */
    private void addHoverEffect(final TextButton button) {
        button.setTransform(true); // To scale a button it needs a transform

        button.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                button.setOrigin(Align.center);     
                button.addAction(Actions.scaleTo(1.1f, 1.1f, 0.1f));
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                button.setOrigin(Align.center);     
                button.addAction(Actions.scaleTo(1.0f, 1, 0.1f));
            }
        });
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.WHITE);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        // If window is minimised it is 0, so don't do anything
        if (width <= 0 || height <= 0)
            return;
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void show() {
    }

    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}