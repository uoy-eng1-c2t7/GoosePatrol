package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.pathfinding.NavGrid;
import io.github.uoy.eng1.c2t7.goosepatrol.world.pathfinding.PathFollower;

/**
 * A goose driven by a state machine: WANDERING near home → CHASING once it sees
 * the player → DISTRACTED by thrown food → EATING → LEAVING (dispersed). Gives
 * up and RETURNS home if it loses the player. Implements FR-08, FR-09, FR-10,
 * UR_PLY_DISPERSE.
 */
public class Goose extends Entity {
    public enum State {
        WANDERING,
        CHASING,
        RETURNING,
        DISTRACTED,
        EATING,
        LEAVING
    }

    private final Player player;
    private final Vector2 homePosition;
    private final PathFollower pathFollower;
    private State state = State.WANDERING;

    private final float sightRange;
    private final float loseSightRange;
    private final float territoryRadius;
    private final float giveUpSeconds;
    private final float leaveSeconds;

    private boolean spottedPlayer = false;

    private float lostTimer = 0f;
    private float wanderTimer = 0f;
    private float eatTimer = 0f;
    private float leaveTimer = 0f;
    private float distractDuration;
    private Vector2 distractionPos = null;

    public Goose(Vector2 startPosition, GameConfig.GooseConfig config, Player player, NavGrid navGrid) {
        super(startPosition, config.radius, config.speed, config.turnSpeed);
        this.player = player;
        leaveSeconds = config.leaveSeconds;
        sightRange = config.sightRange;
        loseSightRange = config.loseSightRange;
        territoryRadius = config.territoryRadius;
        giveUpSeconds = config.giveUpSeconds;
        homePosition = new Vector2(startPosition);
        pathFollower = new PathFollower(navGrid);
    }

    @Override
    public void updateIntent(float delta) {
        float distToPlayer = getPosition().dst(player.getPosition());

        switch (state) {
            case WANDERING:
                if (distToPlayer < sightRange) {
                    spotPlayer();
                    break;
                }
                wanderAroundHome(delta);
                break;

            case CHASING:
                if (distToPlayer > loseSightRange) {
                    lostTimer += delta;
                    if (lostTimer >= giveUpSeconds) {
                        state = State.RETURNING;
                        break;
                    }
                } else {
                    lostTimer = 0f;
                }
                if (getBounds().overlaps(player.getBounds())) {
                    getVelocity().setZero(); // Already touching, no need to get closer
                    break;
                }
                steerTowards(player.getPosition(), getSpeed(), delta);
                break;

            case RETURNING:
                if (distToPlayer < sightRange) {
                    spotPlayer();
                    break;
                }
                float distToHome = getPosition().dst(homePosition);
                if (distToHome < 20f) {
                    state = State.WANDERING;
                } else {
                    steerTowards(homePosition, getSpeed() * 0.6f, delta);
                }
                break;

            case DISTRACTED:
                if (distractionPos != null) {
                    float distToFood = getPosition().dst(distractionPos);
                    if (distToFood < 16f) {
                        state = State.EATING;
                        eatTimer = distractDuration;
                        getVelocity().setZero();
                    } else {
                        steerTowards(distractionPos, getSpeed() * 0.9f, delta);
                    }
                } else {
                    state = State.WANDERING;
                }
                break;

            case EATING:
                getVelocity().setZero();
                eatTimer -= delta;
                if (eatTimer <= 0) {
                    flyAway();
                }
                break;

            case LEAVING:
                leaveTimer -= delta;
                break;
        }
    }

    private void wanderAroundHome(float delta) {
        wanderTimer -= delta;
        if (wanderTimer <= 0) {
            wanderTimer = MathUtils.random(2f, 4f);
            if (MathUtils.randomBoolean(0.35f)) {
                getVelocity().setZero();
            } else {
                if (getPosition().dst(homePosition) > territoryRadius) {
                    Vector2 toHome = homePosition.cpy().sub(getPosition()).nor();
                    getVelocity().set(toHome).scl(getSpeed() * 0.4f);
                } else {
                    getVelocity().setToRandomDirection().scl(getSpeed() * 0.4f);
                }
            }
        }
    }

    private void steerTowards(Vector2 target, float speed, float delta) {
        Vector2 next = pathFollower.nextPoint(getPosition(), target, delta);
        getVelocity().set(next).sub(getPosition()).nor().scl(speed);
    }

    private void flyAway() {
        distractionPos = null;
        state = State.LEAVING;
        leaveTimer = leaveSeconds;
        getVelocity().set(getPosition()).sub(player.getPosition()).nor().scl(getSpeed() * 2f);
    }

    public boolean isGone() {
        return state == State.LEAVING && leaveTimer <= 0f;
    }

    @Override
    public boolean ignoresWalls() {
        return state == State.LEAVING;
    }

    public void distract(Vector2 foodPos, float duration) {
        this.distractionPos = foodPos;
        this.distractDuration = duration;
        this.state = State.DISTRACTED;
    }

    public boolean isChasing() {
        return state == State.CHASING;
    }

    public State getState() {
        return state;
    }

    private void spotPlayer() {
        startChasing();
        spottedPlayer = true;
    }

    public void startChasing() {
        state = State.CHASING;
        lostTimer = 0f;
    }

    /** True once, on the frame after this goose notices the player by itself. */
    public boolean consumeSpottedPlayer() {
        boolean spotted = spottedPlayer;
        spottedPlayer = false;
        return spotted;
    }

    public boolean canBeLured() {
        return state == State.WANDERING || state == State.CHASING || state == State.RETURNING;
    }

    public PathFollower getPathFollower() {
        return pathFollower;
    }

    public float getSightRange() {
        return sightRange;
    }
}