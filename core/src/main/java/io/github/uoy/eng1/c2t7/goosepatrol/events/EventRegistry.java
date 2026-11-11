package io.github.uoy.eng1.c2t7.goosepatrol.events;

import com.badlogic.gdx.utils.Array;
import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;

import java.util.HashMap;
import java.util.Map;

public class EventRegistry {
    private static final Map<String, Event> EVENTS = new HashMap<>();


    public static final Event PLAYER_SPEED_EVENT = register(new Event(
        "player_speed", "Usain Bolt", "Player is now twice as fast", 0.01f, false,
        (GameWorld world) -> {
            Player player = world.getPlayer();
            player.setSpeed(player.getSpeed() * 2);
        }
    ));

    public static final Event GEESE_SPEED_EVENT = register(new Event(
        "geese_speed", "Speedy Geese", "The geese are now 50% faster", 0.01f, true,
        (GameWorld world) -> {
            Array<Entity> entities = world.getEntities();
            for (Entity entity: entities) {
                if (entity instanceof Goose) {
                    entity.setSpeed((float) (entity.getSpeed() * 1.5));
                }
            }
        }
    ));

    public static Event register(Event event) {
        EVENTS.put(event.getId(), event);
        return event;
    }

    public static Map<String, Event> getEvents() { return EVENTS; }

    public static Event get(String id) { return EVENTS.get(id); }

}
