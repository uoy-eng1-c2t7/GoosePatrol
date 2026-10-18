package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.ai.PathFinder;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

/**
 * A hungry goose. Behaviour is a small state machine:
 * IDLE (wander near home) -> CHASE (follow the player) -> RETURN_HOME -> IDLE,
 * with DISTRACTED (walk to a lure) available from any state.
 * <p>
 * If the goose can see where it wants to go, it goes there. Otherwise it asks
 * {@link PathFinder}
 * for a route around walls.
 */
public class Goose extends Entity {
    private enum State {
        IDLE, CHASE, DISTRACTED, RETURN_HOME
    }

    private static final float ARRIVE_DISTANCE = 4f;

    private final GameConfig.GooseConfig config;
    private final Entity target;
    private final PathFinder pathfinder;
    private final Vector2 home;
    private final Vector2 toGoal = new Vector2();

    private State state = State.IDLE;
    private Array<Vector2> path = new Array<>();
    private float repathTimer;

    // CHASE
    private float timeSinceSeen;
    private final Vector2 lastSeen = new Vector2();
    private float reaggroCooldown;

    // DISTRACTED
    private final Vector2 distractPoint = new Vector2();
    private float distractTimer;

    // IDLE wandering
    private final Vector2 wanderPoint = new Vector2();
    private boolean hasWanderPoint;
    private float wanderPause;

    public Goose(Vector2 startPosition, GameConfig.GooseConfig config, Entity target, PathFinder pathfinder) {
        super(startPosition, config.speed, config.radius);
        this.config = config;
        this.target = target;
        this.pathfinder = pathfinder;
        this.home = new Vector2(startPosition);
        // stagger so geese dont all repath on one frame (laggy :( )
        this.repathTimer = MathUtils.random(0f, config.repathSeconds); 
    }

    /** True while goose is actively following the player. The bus uses this. */
    public boolean isChasing() {
        return state == State.CHASE;
    }

    /** True while the goose is busy with a lure. */
    public boolean isDistracted() {
        return state == State.DISTRACTED;
    }

    /** Makes the goose start chasing the player immediately */
    public void alert() {
        startChase();
    }

    /** Makes goose drop everything and go to {@code point} for {@code seconds}. */
    public void distractWith(Vector2 point, float seconds) {
        state = State.DISTRACTED;
        distractPoint.set(point);
        distractTimer = seconds;
        path.clear();
        repathTimer = 0f;
    }

    @Override
    public void updateIntent(float delta) {
        stop(); // stand still unless current state says otherwise
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

    private void updateChase(float delta) {
        if (pathfinder.canSee(getPosition(), target.getPosition())) {
            timeSinceSeen = 0f;
            lastSeen.set(target.getPosition());
        } else {
            timeSinceSeen += delta;
        }

        float leash = config.leashRadius;
        boolean tooFar = getPosition().dst(target.getPosition()) > leash * leash;
        if (tooFar || timeSinceSeen > config.loseSightSeconds) {
            startReturnHome(); // Player lost - give up!
            return;
        }
        moveToward(lastSeen, getSpeed(), delta); // last seen is the live position while visible
    }

    private void updateDistracted(float delta) {
        distractTimer -= delta;
        if (distractTimer <= 0) {
            // Lure finished. If still near player CHASE THEM.
            if (canNoticePlayer()) {
                startChase();
            } else {
                startReturnHome();
            }
        }
        // Walk to lure then wait there until the timer runs out
        if (getPosition().dst2(distractPoint) > ARRIVE_DISTANCE * ARRIVE_DISTANCE) {
            moveToward(distractPoint, getSpeed(), delta);
        }
    }

    private void updateReturnHome(float delta) {
        // Notice player again, but only after a cooldown so a goose that just gave up
        // doesn't flip immediatley back
        reaggroCooldown -= delta;
        if (reaggroCooldown <= 0 && canNoticePlayer()) {
            startChase();
            return;
        }
        if (getPosition().dst2(home) <= ARRIVE_DISTANCE * ARRIVE_DISTANCE) {
            state = State.IDLE;
            hasWanderPoint = false;
            wanderPause = randomPause();
            return;
        }
        moveToward(home, getSpeed(), delta);
    }

    /** Close enough and with a clear line of sight? */
    private boolean canNoticePlayer() {
        float aggro = config.aggroRadius;
        return getPosition().dst2(target.getPosition()) < aggro * aggro
                && pathfinder.canSee(getPosition(), target.getPosition());
    }

    private void startChase() {
        state = State.CHASE;
        timeSinceSeen = 0f;
        lastSeen.set(target.getPosition());
        path.clear();
        repathTimer = 0f;
    }

    private void startReturnHome() {
        state = State.RETURN_HOME;
        path.clear();
        repathTimer = 0f;
        reaggroCooldown = config.reaggroCooldown;
    }

    /** Walk straight if the way is clear, otherwise follow a pathfinder route. */
    private void moveToward(Vector2 goal, float moveSpeed, float delta) {
        if (pathfinder.canSee(getPosition(), goal)) {
            path.clear();
            head(goal, moveSpeed);
            return;
        }
        repathTimer -= delta;
        if (repathTimer <= 0) {
            path = pathfinder.findPath(getPosition(), goal);
            repathTimer = config.repathSeconds + MathUtils.random(0f, 0.1f);
        }
        followPath(moveSpeed);
    }

    private void followPath(float moveSpeed) {
        // Drop every waypoint already reached
        while (path.size > 0
                && getPosition().dst2(path.first()) <= ARRIVE_DISTANCE * ARRIVE_DISTANCE) {
            path.removeIndex(0);
        }
        if (path.size > 0) {
            head(path.first(), moveSpeed);
        }
    }

    private void head(Vector2 goal, float moveSpeed) {
        toGoal.set(goal).sub(getPosition());
        steer(toGoal, moveSpeed);
    }

    /** Walk to a random spot near home, pause, repeat. */
    private void wander(float delta) {
        if (wanderPause > 0) {
            wanderPause -= delta;
            return;
        }
        if (!hasWanderPoint) {
            pickWanderPoint();
            return;
        }
        if (getPosition().dst2(wanderPoint) <= ARRIVE_DISTANCE * ARRIVE_DISTANCE) {
            hasWanderPoint = false;
            wanderPause = randomPause();
            return;
        }
        head(wanderPoint, getSpeed() * config.wanderSpeedFactor);
    }

    private void pickWanderPoint() {
        for (int attempt = 0; attempt < 5; attempt++) {
            float angle = MathUtils.random(360f);
            float distance = MathUtils.random(config.wanderRadius);
            wanderPoint.set(home).add(MathUtils.cosDeg(angle) * distance, MathUtils.sinDeg(angle) * distance);
            if (pathfinder.canSee(getPosition(), wanderPoint)) {
                hasWanderPoint = true;
                return;
            }
        }
        wanderPause = randomPause(); // Nothing was reachable, try again in a bit
    }

    private float randomPause() {
        return MathUtils.random(config.wanderPauseMin, config.wanderPauseMax);
    }
}
