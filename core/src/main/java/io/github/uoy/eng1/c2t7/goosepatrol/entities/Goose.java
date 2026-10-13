package io.github.uoy.eng1.c2t7.goosepatrol.entities;

import java.util.Vector;

import org.w3c.dom.Text;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.ai.Pathfinder;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Constants;

/**
 * A hungry goose, it's behaviour is a small state machine.
 * <p>
 * Movement: if the goose can see where it wants to go, it walks straight there.
 * If a wall is in the way it will ask the {@link Pathfinder} for a route.
 * 
 */
public class Goose extends Entity {
    private enum State {
        IDLE, CHASE, DISTRACTED, RETURN_HOME
    }

    private final Entity target;
    private final Pathfinder pathfinder;
    private final Vector2 home;

    private State state = State.IDLE;

    // Array of points for the current path
    private Array<Vector2> currentPath = new Array<>();
    // Random repathing time so geese dont all repath at the same time (would be
    // laggy :( )
    private float repathTimer = MathUtils.random(0f, Constants.REPATH_TIME);

    // CHASE
    private float chaseTimer; // How long the goose has been chasing
    private float timeSinceSeen; // How long since the goose last saw the player
    private final Vector2 lastSeen = new Vector2(); // where the goose last saw the player
    private float reaggroCooldown; // Ignore the player briefly before giving up

    // DISTRACTED
    private final Vector2 distractPoint = new Vector2(); //
    private float distractTimer;

    // IDLE wandering
    private final Vector2 wanderPoint = new Vector2();
    private boolean hasWanderPoint;
    private float wanderPause;
    private float wanderTimer;

    private final Vector2 dir = new Vector2();

    public Goose(Vector2 startPosition, Texture texture, Entity target, Pathfinder pathfinder) {
        super(startPosition, texture, Constants.GOOSE_SPEED, Constants.GOOSE_HITBOX_RADIUS);
        this.home = new Vector2(startPosition);
        this.target = target;
        this.pathfinder = pathfinder;
    }

    /**
     * Makes the goose drop everything and go to {@code point} for {@code seconds}
     */
    public void distractWith(Vector2 point, float seconds) {
        state = State.DISTRACTED;
        distractPoint.set(point);
        distractTimer = seconds;
        currentPath.clear();
        repathTimer = 0f;
    }

    public State getState() {
        return state;
    }

    @Override
    public void updateIntent(float delta) {
        velocity.set(0, 0);

        switch (state) {
            case IDLE -> updateIdle(delta);
            case CHASE -> updateChase(delta);
            case DISTRACTED -> updateDistracted(delta);
            case RETURN_HOME -> updateReturnHome(delta);
        }
    }

    private void updateIdle(float delta) {
        if (canNoticePlayer()) {
            startChase();
            return;
        }
        wander(delta);
    }

    /**
     * True if the player is inside the wake-up radius and in clear view
     */
    private boolean canNoticePlayer() {
        return position.dst2(target.position) < Constants.AGGRO_RADIUS * Constants.AGGRO_RADIUS
                && pathfinder.canSee(position, target.position);
    }

    private void startChase() {
        state = State.CHASE;
        chaseTimer = Constants.MAX_CHASE_SECONDS;
        timeSinceSeen = 0f;
        lastSeen.set(target.position);
        currentPath.clear();
    }

    private void updateChase(float delta) {
        chaseTimer -= delta;

        if (pathfinder.canSee(position, target.position)) {
            timeSinceSeen = 0f;
            lastSeen.set(target.position);
        } else {
            timeSinceSeen += delta;
        }

        boolean tooFar = position.dst2(target.position) > Constants.LEASH_RADIUS * Constants.LEASH_RADIUS;
        if (chaseTimer <= 0 || tooFar || timeSinceSeen > Constants.LOSE_SIGHT_SECONDS) {
            startReturnHome();
            return;
        }

        // Just lost sight: keep following the real position for a moment, then fall
        // back to last seen spot
        Vector2 goal = timeSinceSeen < Constants.TRACK_SECONDS ? target.position : lastSeen;
        moveToward(goal, speed, delta);
    }

    private void updateDistracted(float delta) {
        distractTimer -= delta;
        if (distractTimer <= 0) {
            startReturnHome();
            return;
        }

        // Walk to lure, then wait there until timer runs out
        if (position.dst2(distractPoint) > Constants.ARRIVE_DISTANCE * Constants.ARRIVE_DISTANCE) {
            moveToward(distractPoint, speed, delta);
        }
    }

    private void updateReturnHome(float delta) {
        // Notice the player again, but only after a short cooldown so a goose that just
        // gave up doesnt flip back immediately
        reaggroCooldown -= delta;
        if (reaggroCooldown <= 0 && canNoticePlayer()) {
            startChase();
            return;
        }

        if (position.dst2(home) <= Constants.HOME_ARRIVE_DISTANCE * Constants.HOME_ARRIVE_DISTANCE) {
            state = State.IDLE;
            hasWanderPoint = false;
            wanderPause = MathUtils.random(Constants.WANDER_PAUSE_MIN, Constants.WANDER_PAUSE_MAX);
            return;
        }
        moveToward(home, speed, delta);
    }

    private void startReturnHome() {
        state = State.RETURN_HOME;
        currentPath.clear();
        repathTimer = 0f;
        reaggroCooldown = Constants.REAGGRO_COOLDOWN;
    }

    private void moveToward(Vector2 goal, float moveSpeed, float delta) {
        if (pathfinder.canSee(position, goal)) {
            currentPath.clear();
            steerStraight(goal, moveSpeed, delta);
            return;
        }

        // Wall is in way, use pathfinding
        repathTimer -= delta;
        if (repathTimer <= 0) {
            currentPath = pathfinder.findPath(position, goal);
            repathTimer = Constants.REPATH_TIME + MathUtils.random(0f, 0.1f);
        }
        followPath(moveSpeed, delta);
    }

    private void followPath(float moveSpeed, float delta) {
        // Drop every waypoint goose has already reached
        while (currentPath.size > 0
                && position.dst2(currentPath.first()) <= Constants.ARRIVE_DISTANCE * Constants.ARRIVE_DISTANCE) {
            currentPath.removeIndex(0);
        }
        if (currentPath.size > 0) {
            steerStraight(currentPath.first(), moveSpeed, delta);
        }
    }

    /**
     * Set velocity to head at a point without overshooting the point
     */
    private void steerStraight(Vector2 goal, float moveSpeed, float delta) {
        dir.set(goal).sub(position);
        float step = moveSpeed * delta;

        if (dir.len2() <= step * step) {
            velocity.set(dir); // close enough to land on it
        } else {
            velocity.set(dir).nor().scl(step);
        }
        if (!velocity.isZero()) {
            targetRotation = velocity.angleDeg();
        }
    }

    /**
     * Walk to random spot near home, pause, repeat
     */
    private void wander(float delta) {
        if (wanderPause > 0) {
            wanderPause -= delta;
            return;
        }
        if (!hasWanderPoint) {
            pickWanderPoint();
            return;
        }

        wanderTimer -= delta;
        boolean arrived = position.dst2(wanderPoint) <= Constants.ARRIVE_DISTANCE * Constants.ARRIVE_DISTANCE;
        if (arrived || wanderTimer <= 0) {
            hasWanderPoint = false;
            wanderPause = MathUtils.random(Constants.WANDER_PAUSE_MIN, Constants.WANDER_PAUSE_MAX);
            return;
        }
        steerStraight(wanderPoint, speed * Constants.WANDER_SPEED_FACTOR, delta);
    }

    private void pickWanderPoint() {
        for (int attempts = 0; attempts < 5; attempts++) {
            float angle = MathUtils.random(360f);
            float distance = MathUtils.random(Constants.WANDER_RADIUS);
            wanderPoint.set(home).add(MathUtils.cosDeg(angle) * distance, MathUtils.sinDeg(angle) * distance);

            if (pathfinder.canSee(position, wanderPoint)) {
                hasWanderPoint = true;
                wanderTimer = Constants.WANDER_TIMEOUT;
                return;
            }
        }
        wanderPause = MathUtils.random(Constants.WANDER_PAUSE_MIN, Constants.WANDER_PAUSE_MAX);
    }

    @Override
    public void drawDebug(ShapeRenderer shapeRenderer) {
        super.drawDebug(shapeRenderer);

        // Home marker
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.circle(home.x, home.y, 4);

        // Draw right debug visuals depending on state
        switch (state) {
            case IDLE -> {
                shapeRenderer.setColor(Color.YELLOW);
                shapeRenderer.circle(position.x, position.y, Constants.AGGRO_RADIUS);
            }
            case CHASE -> {
                shapeRenderer.setColor(pathfinder.canSee(position, target.position) ? Color.RED : Color.MAROON);
                shapeRenderer.line(position, target.position);
            }
            case DISTRACTED -> {
                shapeRenderer.setColor(Color.ORANGE);
                shapeRenderer.line(position, distractPoint);
            }
            case RETURN_HOME -> {
                shapeRenderer.setColor(Color.CYAN);
                shapeRenderer.line(position, home);
            }
        }

        // Pathfinding
        if (!currentPath.isEmpty()) {
            shapeRenderer.setColor(Color.BLUE);
            Vector2 prev = position;
            for (Vector2 waypoint : currentPath) {
                shapeRenderer.line(prev.x, prev.y, waypoint.x, waypoint.y);
                shapeRenderer.circle(waypoint.x, waypoint.y, 4);
                prev = waypoint;
            }
        }
    }
}