package io.github.uoy.eng1.c2t7.goosepatrol.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public abstract class Entity {
    public Vector2 position;
    public Vector2 velocity = new Vector2(0, 0);
    public Circle hitbox;
    protected Texture texture;
    protected float speed;
    protected float turnSpeed = 10f;
    protected float rotation = 0;
    protected float targetRotation = 0;

    public Entity(Vector2 startPosition, Texture texture, float speed, float hitBoxRadius) {
        position = new Vector2(startPosition.x, startPosition.y);
        this.texture = texture;
        this.speed = speed;
        this.hitbox = new Circle(position.x, position.y, hitBoxRadius);
    }

    // Entities updates where it wants to go
    public abstract void updateIntent(float delta);

    // Visuals
    public void draw(SpriteBatch batch) {
        float w = texture.getWidth(), h = texture.getHeight();
        // draw needs the bottom left corner, so offset position - long call :(
        batch.draw(texture, position.x - w / 2f, position.y - h / 2f, w / 2f, h / 2f, w, h, 1f, 1f, rotation, 0, 0, (int) w, (int) h, false, false);
    }

    // Keep hitbox on the entities position
    public void syncHithox() {
        hitbox.setPosition(position.x, position.y);
    }

    public void update(float delta) {
        // Smoothly update rotation to targetRotation
        rotation = MathUtils.lerpAngleDeg(rotation, targetRotation, 1f - (float) Math.exp(-turnSpeed * delta));
    }

    public void drawDebug(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.GREEN);
        shapeRenderer.circle(hitbox.x, hitbox.y, hitbox.radius);
    }
}