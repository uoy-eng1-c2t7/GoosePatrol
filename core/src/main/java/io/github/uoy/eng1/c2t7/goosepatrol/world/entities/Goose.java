package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.AStar;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

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

    private final AStar pathfinder;
    public Array<Vector2> path;
    private float pathfindCooldown;
    private float pathfindConfigCooldown;

    public Goose(Vector2 startPosition, GameConfig.GooseConfig config, Player player, AStar pathfinder) {
        super(startPosition, config.radius, config.speed, config.turnSpeed);
        this.player = player;
        sightRange = config.sightRange;
        loseSightRange = config.loseSightRange;
        territoryRadius = config.territoryRadius;
        pathfindConfigCooldown = config.pathfindCooldown;
        pathfindCooldown = pathfindConfigCooldown;
        homePosition = new Vector2(startPosition);

        this.pathfinder = pathfinder;
        path = new Array<>();
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
                pathfindCooldown -= delta;
                if (pathfindCooldown <= 0) {
                    path = pathfinder.findPath(getPosition(), player.getPosition());
                    pathfindCooldown = pathfindConfigCooldown;
                }
                if (Math.abs(getPosition().len() - player.getPosition().len()) < 16 || path.size < 3) { steerTowards(player.getPosition(), getSpeed() * 0.9f, delta); }
                else { steerTowards(path.get(1), getSpeed() * 0.9f, delta); }

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
                    pathfindCooldown -= delta;
                    if (pathfindCooldown <= 0) {
                        path = pathfinder.findPath(getPosition(), homePosition);
                        pathfindCooldown = pathfindConfigCooldown;
                    }
                    if (path.size < 3) return;
                    steerTowards(path.get(1), getSpeed() * 0.6f, delta);
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
        Vector2 direct = target.cpy().sub(getPosition()).nor();

        // Corner unstuck feeler
        if (getVelocity().len2() < 10f) {
            stuckTimer += delta;
            if (stuckTimer > 0.3f) {
                if (detourDir.isZero())
                    detourDir.set(-direct.y, direct.x);
                getVelocity().set(detourDir).scl(speed);
                return;
            }
        } else {
            stuckTimer = 0f;
            detourDir.setZero();
        }

        getVelocity().set(direct).scl(speed);
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
}
