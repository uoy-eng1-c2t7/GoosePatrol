package io.github.uoy.eng1.c2t7.goosepatrol.events;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;

import java.util.HashMap;
import java.util.Map;

public class EventRegistry {
    private static final Map<String, Event> EVENTS = new HashMap<>();

    public static final Event TEST_EVENT = register(new Event(
        "test", "test", "im about to test", 1f, false,
        (GameWorld world) -> {
            System.out.println("i tested");
        }
        ));

    public static Event register(Event event) {
        EVENTS.put(event.getId(), event);
        return event;
    }

    public static Map<String, Event> getEvents() { return EVENTS; }

    public static Event get(String id) { return EVENTS.get(id); }

}
