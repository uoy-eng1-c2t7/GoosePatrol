package io.github.uoy.eng1.c2t7.goosepatrol.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;

public class Goose extends Entity {
    private enum State {
        IDLE, ANGRY, DISTRACTED
    }

    public Array<Vector2> currentPath;

    private final Entity target;
    private State state = State.ANGRY;
    private final Vector2 dir = new Vector2();

    public Goose(Vector2 startPosition, Entity target) {
        super(startPosition, new Texture(Constants.GOOSE_TEXTURE), Constants.GOOSE_SPEED);
        this.target = target;
    }

    @Override
    public void updateIntent(float delta) {
        velocity.set(0, 0);

        if (state != State.ANGRY) return;
        
        dir.set(target.position).sub(position);

        if (dir.len() > hitbox.radius + target.hitbox.radius) { // stop if touching
            velocity.set(dir).nor().scl(speed * delta);
            targetRotation = velocity.angleDeg();
        }
    }

    @Override 
    public void drawDebug(ShapeRenderer shapeRenderer) {
        // Draw green hitbox
        super.drawDebug(shapeRenderer); 

        // Draw intent - current velocity vector
        if (!velocity.isZero()) {
            shapeRenderer.setColor(Color.MAGENTA);
            // Scale up line by arbitary amount so it isnt too small
            shapeRenderer.line(position.x, position.y, position.x + velocity.x * 60, position.y + velocity.y * 60);
            
            // Draw A* path
            if (currentPath != null && !currentPath.isEmpty()) {
                shapeRenderer.setColor(Color.YELLOW);
                Vector2 prevPoint = position;

                for (Vector2 waypoint : currentPath) {
                    shapeRenderer.line(prevPoint.x, prevPoint.y, waypoint.x, waypoint.y);
                    shapeRenderer.circle(waypoint.x, waypoint.y, 4);
                    prevPoint = waypoint;
                }
            }
        }
    }

}
