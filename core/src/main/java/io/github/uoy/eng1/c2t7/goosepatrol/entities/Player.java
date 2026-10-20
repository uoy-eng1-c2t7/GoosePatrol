package io.github.uoy.eng1.c2t7.goosepatrol.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;

public class Player extends Entity {
    public Vector2 position;
    public Vector2 velocity;
    final int SPEED;
    TextureRegion texture;


    public Player(Vector2 startPosition, TextureRegion texture, int speed) {
        this.position = startPosition;
        this.texture = texture;
        this.velocity = Vector2.Zero;
        this.SPEED = speed;

    }

    public void updatePosition(float delta) {
        position.x += velocity.x * delta * SPEED;
        position.y += velocity.y * delta * SPEED;
    }

    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, texture.getRegionWidth(), texture.getRegionHeight());
    }
}
