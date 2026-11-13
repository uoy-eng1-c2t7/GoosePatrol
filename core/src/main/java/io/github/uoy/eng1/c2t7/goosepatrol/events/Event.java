package io.github.uoy.eng1.c2t7.goosepatrol.events;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;

public class Event {


    @FunctionalInterface
    public interface EventBehaviour {
        void onTrigger(GameWorld world);
    }

    private final String id;
    private final String name;
    private final String description;
    private final EventBehaviour behaviour;
    private final float probability; // chance of being triggered per second
    private final boolean repeatable;
    private boolean triggered;

    public Event(String id, String name, String description, float probability, boolean repeatable, EventBehaviour behaviour) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.probability = probability;
        this.repeatable = repeatable;
        this.behaviour = behaviour;
        this.triggered = false;
    }

    // returns true if triggered successfully, false otherwise
    public boolean trigger(GameWorld world) {
        if (behaviour == null || triggered) return false;
        if (!repeatable) triggered = true;
        behaviour.onTrigger(world);

        return true;
    }

    public String getId() { return id; }

    public float getProbability() { return probability; }

    public String getName() { return name; }

    public String getDescription() { return description; }

}
