package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

public class Player {
    private Body body;
    private float speed = 2f; 
    private final Texture texture;

    public Player(World world, float startX, float startY) {
        texture = new Texture("student.png");

        // Define physics body
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(startX / GameScreen.PPM, startY / GameScreen.PPM);
        body = world.createBody(bd);

        // Define circle hitbox
        CircleShape circle = new CircleShape();
        circle.setRadius(GameScreen.pixelToMeters(texture.getWidth() / 2f));
        FixtureDef fd = new FixtureDef();
        fd.shape = circle;
        body.createFixture(fd);
        circle.dispose();
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    public void update(float delta) {
        float velX = 0;
        float velY = 0;

        // Movement up (positive y)
        if (Gdx.input.isKeyPressed(Input.Keys.W) || Gdx.input.isKeyPressed(Input.Keys.UP)) velY += speed;
        // Movement down (negative y)
        if (Gdx.input.isKeyPressed(Input.Keys.S) || Gdx.input.isKeyPressed(Input.Keys.DOWN)) velY -= speed;
        // Movement left (negative x)
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) velX -= speed;
        // Movement right (positive x)
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) velX += speed;

        body.setLinearVelocity(velX, velY);
    }

    public void render(SpriteBatch batch) {
        float pixelX = GameScreen.metersToPixels(body.getPosition().x) - (texture.getWidth() / 2f);
        float pixelY = GameScreen.metersToPixels(body.getPosition().y) - (texture.getHeight() / 2f);

        batch.draw(texture, pixelX, pixelY);
    }

    public void dispose() {
        texture.dispose();
    }
}
