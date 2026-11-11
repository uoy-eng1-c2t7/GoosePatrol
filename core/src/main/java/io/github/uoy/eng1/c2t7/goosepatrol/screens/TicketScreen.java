package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;

public class TicketScreen implements Screen {
    private final Stage stage;

    public TicketScreen(GoosePatrol game, boolean won, float timeTakenSeconds, int itemsUsed, int eventsTriggered) {
        this.stage = new Stage(new FitViewport(1280, 720), game.getBatch());
        Gdx.input.setInputProcessor(stage);
        Skin skin = game.getAssets().getSkin();

        // Dark background container
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // Bus ticket
        Table ticket = new Table();

        // Paper background
        ticket.setBackground(skin.newDrawable("white", new Color(0.95f, 0.94f, 0.88f, 1f)));
        ticket.pad(30);
        Label header = new Label("=== SECOND BUS ===", skin);
        header.setColor(Color.DARK_GRAY);
        Label title = new Label(won ? "BOARDED ON TIME!" : "BUS DEPARTED - MISSED!", skin);
        title.setColor(won ? new Color(0.1f, 0.6f, 0.1f, 1f) : Color.FIREBRICK);
        int mins = (int) (timeTakenSeconds / 60);
        int secs = (int) (timeTakenSeconds % 60);
        Label timeLabel = new Label("Trip Time: " + String.format("%02d:%02d", mins, secs), skin);
        Label itemLabel = new Label("Items used: " + itemsUsed, skin);
        Label eventLabel = new Label("Events triggered: " + eventsTriggered, skin);
        timeLabel.setColor(Color.BLACK);
        itemLabel.setColor(Color.BLACK);
        eventLabel.setColor(Color.BLACK);
        Label divider = new Label("---------------------------", skin);
        divider.setColor(Color.GRAY);
        ticket.add(header).padBottom(10).row();
        ticket.add(title).padBottom(15).row();
        if (won) {
            ticket.add(timeLabel).padBottom(10).row();
            ticket.add(itemLabel).padBottom(10).row();
            ticket.add(eventLabel).padBottom(10).row();
        }
        ticket.add(divider).padBottom(20).row();

        // Buttons
        TextButton restartBtn = new TextButton("Play Again", skin);
        TextButton menuBtn = new TextButton("Main Menu", skin);

        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game));
            }
        });

        menuBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        ticket.add(restartBtn).size(200, 45).padBottom(10).row();
        ticket.add(menuBtn).size(200, 45).row();
        root.add(ticket).center();
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.1f, 0.1f, 0.15f, 1f);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        if (width > 0 && height > 0)
            stage.getViewport().update(width, height, true);
    }

    @Override
    public void show() {
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
        stage.dispose();
    }
}
