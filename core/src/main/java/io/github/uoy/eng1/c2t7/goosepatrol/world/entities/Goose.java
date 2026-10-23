package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;

public class Goose extends Entity {
    public Goose(Vector2 startPosition, GameConfig.GooseConfig config) {
        super(startPosition, config.radius, config.turnSpeed);
    }

    public enum State {
        WANDERING,
        CHASING, 
        PECKING,
        DISTRACTED
    }

    private State state = State.WANDERING;

    @Override
    public void updateIntent(float delta) {
        
    }
    
    
}
