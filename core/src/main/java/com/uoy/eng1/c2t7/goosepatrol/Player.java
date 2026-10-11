package com.uoy.eng1.c2t7.goosepatrol;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

/**
 * The player, a dynamic Box2D body.
 */
public class Player {
    private final Body body;
    private final Vector2 direction = new Vector2();
    private final Vector2 velocity = new Vector2();
    private float facing;

    public Player(World world, float startX, float startY) {
        BodyDef bd = new BodyDef();
        bd.type = BodyDef.BodyType.DynamicBody;
        bd.position.set(startX, startY);
        bd.fixedRotation = true;
        body = world.createBody(bd);

        CircleShape circle = new CircleShape();
        circle.setRadius(Constants.PLAYER_RADIUS);
        FixtureDef fd = new FixtureDef();
        fd.shape = circle;
        fd.friction = 0f;
        body.createFixture(fd);
        circle.dispose();
    }

    /**
     * Set the movement direction.
     */
    public void setDirection(float x, float y) {
        direction.set(x, y);
    }

    public Vector2 getPosition() {
        return body.getPosition();
    }

    /**
     * Applies direction as velocity, normalised so diaganols aren't faster.
     */
    public void update() {
        velocity.set(direction).nor().scl(Constants.PLAYER_SPEED);
        body.setLinearVelocity(velocity);
    }

    public void faceToward(Vector2 target, float delta) {
        Vector2 pos = body.getPosition();
        float dx = target.x - pos.x;
        float dy = target.y - pos.y;
        if (dx * dx + dy * dy < 0.0001f) {
            return;
        }
        float targetAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        float alpha = 1f - (float) Math.exp(-Constants.PLAYER_TURN_SPEED * delta);
        facing = MathUtils.lerpAngleDeg(facing, targetAngle, alpha);
    }

    /**
     * Draw the sprite centered on the body
     */
    public void render(SpriteBatch batch, Texture texture) {
        float width = texture.getWidth() / Constants.PPM;
        float height = texture.getHeight() / Constants.PPM;
        batch.draw(texture,
                body.getPosition().x - width / 2f, body.getPosition().y - height / 2f,
                width / 2f, height / 2f,
                width, height,
                1f, 1f,
                facing,
                0, 0, texture.getWidth(), texture.getHeight(),
                false, false);
    }

}
