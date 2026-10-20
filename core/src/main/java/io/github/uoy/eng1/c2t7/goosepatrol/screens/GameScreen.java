package io.github.uoy.eng1.c2t7.goosepatrol.screens;

import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.ScreenUtils;

import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.WorldListener;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;

public class GameScreen implements Screen, WorldListener {
    private final GoosePatrol game;
    private final GameWorld world;
    private float acc = 0;
    private float wait = 5;

    public GameScreen(GoosePatrol game) {
        this.game = game;
        this.world = new GameWorld(game.getConfig(), this);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(Color.BLACK);
        acc += delta;
        if (acc >= wait) {
            game.getAssets().getSound("pain").play();
            acc = 0;
        }
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
