package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.screens.MainMenuScreen;

public class PauseMenuUI extends Table {
    public interface PauseListener {
        void onResume();
        void onRestart();
    }

    public PauseMenuUI(GoosePatrol game, Skin skin, PauseListener listener) {
        setFillParent(true);
        setVisible(false); // Initially hidden

        // Semi-transparent black background overlay
        setBackground(skin.newDrawable("white", new Color(0, 0, 0, 0.75f)));

        Label title = new Label("PAUSED", skin);
        add(title).padBottom(40).row();

        TextButton resumeBtn = new TextButton("Resume", skin);
        TextButton restartBtn = new TextButton("Restart", skin);
        TextButton exitBtn = new TextButton("Main Menu", skin);

        resumeBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onResume();
            }
        });

        restartBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                listener.onRestart();
            }
        });

        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new MainMenuScreen(game));
            }
        });

        add(resumeBtn).size(220, 50).padBottom(15).row();
        add(restartBtn).size(220, 50).padBottom(15).row();
        add(exitBtn).size(220, 50).row();
    }
}