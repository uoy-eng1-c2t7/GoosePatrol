package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.ai.PathFinder;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

/**
 * A hungry goose. Behaviour is a small state machine:
 * <pre>
 *   IDLE (wander near home) --notices player--> CHASE --player LOST--> RETURN_HOME --> IDLE
 *   any state --lure item--> DISTRACTED --lure ends--> CHASE (if it can still see you) or RETURN_HOME
 * </pre>
 *
 * <h3>When does a goose give up?</h3>
 * Only when the player has been <b>lost</b>: out of line of sight for
 * {@code loseSightSeconds}, or further away than {@code leashRadius}. There is no chase
 * timer. A lure alone isn't enough: when it runs out the goose looks for you again.
 *
 * <h3>Moving</h3>
 * If the goose can walk straight to its goal it does. Otherwise it follows a route from
 * {@link PathFinder}. If no route exists it pushes toward the goal anyway (walls slide it)
 * rather than standing still. A stuck detector forces a fresh route if it stops making progress.
 */
public class Goose extends Entity {
    private enum State {
        IDLE, CHASE, DISTRACTED, RETURN_HOME
    }

    private static final float ARRIVE_DISTANCE = 4f;
    private static final float STUCK_PROGRESS = 0.25f;  // moved less than this fraction of expected = no progress
    private static final float STUCK_SECONDS = 0.5f;    // for this long = stuck

    private final GameConfig.GooseConfig config;
    private final Entity target;
    private final PathFinder pathfinder;
    private final Vector2 home;
    private final Vector2 toGoal = new Vector2();

    private State state = State.IDLE;
    private Array<Vector2> path = new Array<>();
    private float repathTimer;

    // What the goose is currently heading for (debug view reads this)
    private final Vector2 currentGoal = new Vector2();
    private boolean hasGoal;

    // Stuck detection
    private final Vector2 lastPosition = new Vector2();
    private float stuckSeconds;
    private int unstickCount;

    // CHASE
    private float timeSinceSeen;
    private boolean hasEverSeenPlayer; // only start the lose-sight timer once LOS is first established
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
        this.lastPosition.set(startPosition);
        // Stagger so geese don't all repath on the same frame
        this.repathTimer = MathUtils.random(0f, config.repathSeconds);
    }

    // ------------------------------------------------------------ public API

    /** True while the goose is actively following the player. The bus check uses this. */
    public boolean isChasing() {
        return state == State.CHASE;
    }

    /** True while the goose is busy with a lure. */
    public boolean isDistracted() {
        return state == State.DISTRACTED;
    }

    /** Makes the goose start chasing the player immediately. */
    public void alert() {
        startChase();
    }

    /** Makes the goose drop everything and go to {@code point} for {@code seconds}. */
    public void distractWith(Vector2 point, float seconds) {
        state = State.DISTRACTED;
        distractPoint.set(point);
        distractTimer = seconds;
        path.clear();
        repathTimer = 0f;
    }

    // ------------------------------------------------------- debug read-outs

    public String getStateName() {
        return state.name();
    }

    /** Remaining waypoints of the current route (empty when walking straight). Read only. */
    public Array<Vector2> getPath() {
        return path;
    }

    public boolean hasGoal() {
        return hasGoal;
    }

    /** Where the goose is trying to get to this frame. Only valid if {@link #hasGoal()}. */
    public Vector2 getGoal() {
        return currentGoal;
    }

    public Vector2 getHome() {
        return home;
    }

    public float getAggroRadius() {
        return config.aggroRadius;
    }

    public float getLeashRadius() {
        return config.leashRadius;
    }

    /** Seconds spent not making progress while trying to move. */
    public float getStuckSeconds() {
        return stuckSeconds;
    }

    /** How many times the stuck detector has had to step in. */
    public int getUnstickCount() {
        return unstickCount;
    }

    // ------------------------------------------------------------- behaviour

    @Override
    public void updateIntent(float delta) {
        stop(); // stand still unless the current state says otherwise
        hasGoal = false;
        switch (state) {
            case IDLE -> updateIdle(delta);
            case CHASE -> updateChase(delta);
            case DISTRACTED -> updateDistracted(delta);
            case RETURN_HOME -> updateReturnHome(delta);
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        trackProgress(delta);
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
            hasEverSeenPlayer = true;
            timeSinceSeen = 0f;
            lastSeen.set(target.getPosition());
        } else if (hasEverSeenPlayer) {
            // Only start losing track once we've actually spotted them at least once
            timeSinceSeen += delta;
        }

        float leash = config.leashRadius;
        boolean tooFar = getPosition().dst2(target.getPosition()) > leash * leash;
        if (tooFar || timeSinceSeen > config.loseSightSeconds) {
            startReturnHome(); // the player has been lost - the ONLY way a chase ends
            return;
        }
        moveToward(lastSeen, getSpeed(), delta); // lastSeen is the live position while visible
    }

    private void updateDistracted(float delta) {
        distractTimer -= delta;
        if (distractTimer <= 0) {
            // Lure finished. Still near the player and can see them? Back to the chase.
            if (canNoticePlayer()) {
                startChase();
            } else {
                startReturnHome();
            }
            return;
        }
        // Walk to the lure then wait there until the timer runs out
        if (getPosition().dst2(distractPoint) > ARRIVE_DISTANCE * ARRIVE_DISTANCE) {
            moveToward(distractPoint, getSpeed(), delta);
        }
    }

    private void updateReturnHome(float delta) {
        // Notice the player again, but only after a cooldown so a goose that just
        // lost them doesn't flip straight back
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

    /** Close enough AND a wall-free line of sight. */
    private boolean canNoticePlayer() {
        float aggro = config.aggroRadius;
        return getPosition().dst2(target.getPosition()) < aggro * aggro
                && pathfinder.canSee(getPosition(), target.getPosition());
    }

    private void startChase() {
        state = State.CHASE;
        timeSinceSeen = 0f;
        hasEverSeenPlayer = false;
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

    // ---------------------------------------------------------------- moving

    /**
     * Walk straight if the body fits along the line, otherwise follow a pathfinder
     * route. With no route available, push toward the goal rather than freeze.
     */
    private void moveToward(Vector2 goal, float moveSpeed, float delta) {
        currentGoal.set(goal);
        hasGoal = true;

        if (pathfinder.canWalk(getPosition(), goal)) {
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
        if (path.isEmpty()) {
            head(goal, moveSpeed);
        }
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

    /** If the goose wants to move but barely does for a while, throw away its plan and start again. */
    private void trackProgress(float delta) {
        float wanted = getVelocity().len() * delta;
        float moved = getPosition().dst(lastPosition);
        lastPosition.set(getPosition());

        if (wanted > 0 && moved < wanted * STUCK_PROGRESS) {
            stuckSeconds += delta;
        } else {
            stuckSeconds = 0f;
        }
        if (stuckSeconds >= STUCK_SECONDS) {
            stuckSeconds = 0f;
            unstickCount++;
            path.clear();
            repathTimer = 0f;       // new route next frame
            hasWanderPoint = false; // new wander target if idle
        }
    }

    // ------------------------------------------------------------- wandering

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
        currentGoal.set(wanderPoint);
        hasGoal = true;
        moveToward(wanderPoint, getSpeed() * config.wanderSpeedFactor, delta);
    }

    private void pickWanderPoint() {
        for (int attempt = 0; attempt < 5; attempt++) {
            float angle = MathUtils.random(360f);
            float distance = MathUtils.random(config.wanderRadius);
            wanderPoint.set(home).add(MathUtils.cosDeg(angle) * distance, MathUtils.sinDeg(angle) * distance);
            if (pathfinder.canWalk(getPosition(), wanderPoint)) {
                hasWanderPoint = true;
                return;
            }
        }
        wanderPause = randomPause(); // nothing reachable, try again in a bit
    }

    private float randomPause() {
        return MathUtils.random(config.wanderPauseMin, config.wanderPauseMax);
    }
}