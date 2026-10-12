package io.github.uoy.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;

public class Player extends Entity {

    public Player(Vector2 startPosition, Texture texture) {
        super(startPosition, texture, Constants.PLAYER_SPEED);
    }

    @Override 
    public void updateIntent(float delta) {
        velocity.set(0, 0); 

        float moveDistance = speed * delta;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) velocity.y += moveDistance;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) velocity.y -= moveDistance;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) velocity.x -= moveDistance;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) velocity.x += moveDistance;
    }
}
