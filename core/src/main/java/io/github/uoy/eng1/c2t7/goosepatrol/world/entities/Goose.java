package io.github.uoy.eng1.c2t7.goosepatrol.world.entities;

import com.badlogic.gdx.math.Vector2;

public class Goose extends Entity {
    public Goose(Vector2 startPosition, float radius) {
        super(startPosition, radius);
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
