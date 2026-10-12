package io.github.uoy.entities;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    public Vector2 position;
    public Vector2 velocity;
    public Circle hitbox;
    protected Texture texture;
    protected float speed;

    public Entity(Vector2 startPosition, Texture texture, float speed) {
        this.position = new Vector2(startPosition.x, startPosition.y);
        this.texture = texture;
        this.speed = speed;
        float radius = texture.getWidth() / 2f;
        // Draw a circle to act as a hitbox in the middle of the texture
        this.hitbox = new Circle(position.x + radius, position.y + radius, radius);
    }

    // Entities updates where it wants to go
    public abstract void updateIntent(float delta);

    // Visuals
    public void draw(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y);
    }

    // Reallign hitbox to center of the texture
    public void syncHithox() {
        hitbox.setPosition(position.x + hitbox.radius, position.y + hitbox.radius);
    }
}