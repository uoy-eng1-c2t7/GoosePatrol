package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.pathfinding.NavGrid;
import io.github.uoy.eng1.c2t7.goosepatrol.world.pathfinding.PathFollower;

public class Goose extends Entity {
    public enum State {
        WANDERING,
        CHASING,
        RETURNING,
        DISTRACTED,
        PECKING
    }

    private final Player player;
    private final Vector2 homePosition;
    private final PathFollower pathFollower;
    private State state = State.WANDERING;

    private final float sightRange;
    private final float loseSightRange;
    private final float territoryRadius;

    private float wanderTimer = 0f;
    private float peckTimer = 0f;
    private float stuckTimer = 0f;
    private float distractDuration;
    private final Vector2 detourDir = new Vector2();
    private Vector2 distractionPos = null;

    public Goose(Vector2 startPosition, GameConfig.GooseConfig config, Player player, NavGrid navGrid) {
        super(startPosition, config.radius, config.speed, config.turnSpeed);
        this.player = player;
        sightRange = config.sightRange;
        loseSightRange = config.loseSightRange;
        territoryRadius = config.territoryRadius;
        homePosition = new Vector2(startPosition);
        pathFollower = new PathFollower(navGrid);
    }

    @Override
    public void updateIntent(float delta) {
        float distToPlayer = getPosition().dst(player.getPosition());

        switch (state) {
            case WANDERING:
                if (distToPlayer < sightRange) {
                    state = State.CHASING;
                    break;
                }
                wanderAroundHome(delta);
                break;

            case CHASING:
                if (distToPlayer > loseSightRange) {
                    state = State.RETURNING;
                    break;
                }
                steerTowards(player.getPosition(), getSpeed(), delta);
                break;

            case RETURNING:
                if (distToPlayer < sightRange) {
                    state = State.CHASING;
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
                        state = State.PECKING;
                        peckTimer = distractDuration;
                        getVelocity().setZero();
                    } else {
                        steerTowards(distractionPos, getSpeed() * 0.9f, delta);
                    }
                } else {
                    state = State.WANDERING;
                }
                break;

            case PECKING:
                getVelocity().setZero();
                peckTimer -= delta;
                if (peckTimer <= 0) {
                    distractionPos = null;
                    state = State.RETURNING;
                }
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

    public void distract(Vector2 foodPos, float duration) {
        this.distractionPos = new Vector2(foodPos);
        this.distractDuration = duration;
        this.state = State.DISTRACTED;
    }

    public boolean isChasing() {
        return state == State.CHASING;
    }

    public State getState() {
        return state;
    }

    public PathFollower getPathFollower() {
        return pathFollower;
    }

    public float getSightRange() {
        return sightRange;
    }
}