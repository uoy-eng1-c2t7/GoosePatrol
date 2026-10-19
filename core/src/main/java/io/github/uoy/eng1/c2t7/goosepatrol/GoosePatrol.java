package io.github.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class GoosePatrol extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen());
    }
}