package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.ai.NavGraph;
import io.github.uoy.eng1.c2t7.goosepatrol.ai.PathFinder;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.Physics.MapCollision;
import io.github.uoy.eng1.c2t7.goosepatrol.world.Physics.PhysicsEngine;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.GroundItem;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.Inventory;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.ItemContext;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.ItemDefinition;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.ItemRegistry;
import io.github.uoy.eng1.c2t7.goosepatrol.world.map.MapLoader;

/**
 * The game's rules and state: where everything is, how much time is left, and
 * whether the player has won or lost.
 * <p>This package draws nothing. Rendering lives in {@code rendering/}, input is read
 * by {@code GameScreen} and passed in, and the world reports what happened through
 * a {@link WorldListener}. That also means the world can be unit tested without a window.</p>
 *
 * <p>Each frame while the game is running, {@code GameScreen} calls
 * {@link #update(float, Vector2)}, which does, in order:</p>
 * <ol>
 *   <li>count the clock down (time out = lost)</li>
 *   <li>every entity decides what it wants ({@code updateIntent})</li>
 *   <li>physics moves them and keeps them out of walls</li>
 *   <li>every entity updates its timers/rotation ({@code update})</li>
 *   <li>pickups, hidden events, lures and the bus stop are checked</li>
 * </ol>
 * Items are used with {@link #useItem(int)}.
 */
public class GameWorld {
    /** How the run is going. */
    public enum Status {
        RUNNING, WON, LOST
    }

    /** A thrown lure, kept only so the renderer can draw it while geese are busy with it. */
    public static final class Lure {
        private final Vector2 position;
        private float secondsLeft;

        private Lure(Vector2 position, float secondsLeft) {
            this.position = position;
            this.secondsLeft = secondsLeft;
        }

        public Vector2 getPosition() {
            return position;
        }
    }

    private static final float PICKUP_RANGE = 10f;
    private static final float GOOSE_ALERT_RADIUS = 300f;
    private static final float BUS_REFUSAL_COOLDOWN = 3f;
    private static final float EVENT_TIME_CHANGE = 15f;
    private static final float EVENT_TIME_LOSS = 10f;

    private final GameConfig config;
    private final WorldListener listener;
    private final MapLoader map;
    private final MapCollision collision;
    private final PhysicsEngine physics;
    private final Player player;
    private final Array<Goose> geese = new Array<>();
    private final Array<Entity> movers = new Array<>();
    private final Array<GroundItem> groundItems = new Array<>();
    private final Array<HiddenEvent> events;
    private final Array<Lure> lures = new Array<>();
    private final Inventory inventory = new Inventory();
    private final ItemContext itemContext = new WorldItemContext();

    private Status status = Status.RUNNING;
    private float timeLeft;
    private float elapsed;
    private float busRefusalCooldown;
    private int itemsUsed;
    private int eventsTriggered;

    /**
     * @param config    tunable numbers
     * @param tiledMap  the loaded campus map
     * @param listener  told about notable things happening (may be a UI, a test, ...)
     */
    public GameWorld(GameConfig config, TiledMap tiledMap, WorldListener listener) {
        this.config = config;
        this.listener = listener;
        this.timeLeft = config.gameDurationSeconds;

        map = new MapLoader(tiledMap);
        collision = new MapCollision(map.getWalls(), map.getWidth(), map.getHeight());
        physics = new PhysicsEngine(collision);

        float clearance = Math.max(config.player.radius, config.goose.radius);
        NavGraph graph = new NavGraph(collision, map.getWidth(), map.getHeight(), map.getTileSize(), clearance);
        PathFinder pathFinder = new PathFinder(graph, collision, clearance);

        player = new Player(map.getPlayerSpawn(), config.player);
        movers.add(player);
        for (Vector2 spawn : map.getGooseSpawns()) {
            Goose goose = new Goose(spawn, config.goose, player, pathFinder);
            geese.add(goose);
            movers.add(goose);
        }

        for (MapLoader.ItemSpawn spawn : map.getItemSpawns()) {
            groundItems.add(new GroundItem(spawn.position(), ItemRegistry.require(spawn.itemType())));
        }
        events = map.getEvents();

        if (config.startingItems != null) {
            for (String id : config.startingItems) {
                inventory.add(ItemRegistry.require(id), 1);
            }
        }
    }

    /**
     * Advances the game by {@code delta} seconds. Does nothing once the game is over.
     *
     * @param moveInput direction the player wants to move (length ignored)
     */
    public void update(float delta, Vector2 moveInput) {
        if (status != Status.RUNNING) {
            return;
        }

        elapsed += delta;
        timeLeft -= delta;
        if (timeLeft <= 0) {
            timeLeft = 0;
            status = Status.LOST;
            return;
        }

        player.setMoveInput(moveInput);
        for (Entity e : movers) {
            e.updateIntent(delta);
        }
        physics.step(movers, delta);
        for (Entity e : movers) {
            e.update(delta);
        }

        checkPickups();
        checkEvents();
        updateLures(delta);
        checkBusStop(delta);
    }

    private void checkPickups() {
        for (GroundItem item : groundItems) {
            if (item.isCollected()) {
                continue;
            }
            float reach = player.getRadius() + PICKUP_RANGE;
            if (item.getPosition().dst2(player.getPosition()) <= reach * reach
                    && inventory.add(item.getDefinition(), 1)) {
                item.collect();
                listener.onItemPickedUp(item.getDefinition().getName());
            }
        }
    }

    private void checkEvents() {
        for (HiddenEvent event : events) {
            if (!event.isTriggered() && event.getArea().contains(player.getPosition())) {
                event.markTriggered();
                eventsTriggered++;
                applyEffect(event.getEffect());
                listener.onEventTriggered(event.getName(), event.getEffect().getMessage());
            }
        }
    }

    /** What each hidden event actually does. Add a case here when adding an Effect. */
    private void applyEffect(HiddenEvent.Effect effect) {
        switch (effect) {
            case GOOSE_ALERT -> {
                for (Goose g : geese) {
                    if (g.getPosition().dst2(player.getPosition()) <= GOOSE_ALERT_RADIUS * GOOSE_ALERT_RADIUS) {
                        g.alert();
                    }
                }
            }
            case FREE_BREAD -> inventory.add(ItemRegistry.require("bread"), 2);
            case BONUS_TIME -> timeLeft += EVENT_TIME_CHANGE;
            case LOSE_TIME -> timeLeft = Math.max(1f, timeLeft - EVENT_TIME_LOSS);
            case SLIPPERY -> player.applySpeedModifier(0.5f, 4f);
        }
    }

    private void updateLures(float delta) {
        for (int i = lures.size - 1; i >= 0; i--) {
            lures.get(i).secondsLeft -= delta;
            if (lures.get(i).secondsLeft <= 0) {
                lures.removeIndex(i);
            }
        }
    }

    /** Reaching the bus stop wins, unless a goose is still on your tail. */
    private void checkBusStop(float delta) {
        busRefusalCooldown -= delta;
        if (!map.getExitArea().contains(player.getPosition())) {
            return;
        }
        if (getChasingCount() == 0) {
            status = Status.WON;
        } else if (busRefusalCooldown <= 0) {
            busRefusalCooldown = BUS_REFUSAL_COOLDOWN;
            listener.onBusRefused();
        }
    }

    /**
     * Uses the item in inventory {@code slot} (0-based). The item is consumed and its
     * effect runs.
     *
     * @return true if something was used
     */
    public boolean useItem(int slot) {
        if (status != Status.RUNNING) {
            return false;
        }
        Inventory.Stack stack = inventory.get(slot);
        if (stack == null) {
            return false;
        }
        ItemDefinition definition = stack.getDefinition();
        inventory.removeOne(slot);
        definition.getEffect().apply(itemContext);
        itemsUsed++;
        listener.onItemUsed(definition.getName());
        return true;
    }

    /** The only door through which item effects can touch the world. */
    private final class WorldItemContext implements ItemContext {
        @Override
        public Vector2 getPlayerPosition() {
            return new Vector2(player.getPosition());
        }

        @Override
        public Vector2 throwTarget(float distance) {
            Vector2 origin = player.getPosition();
            float angle = player.getRotation();
            Vector2 target = new Vector2();
            for (float d = distance; d > 0; d -= 8f) {
                target.set(origin).add(MathUtils.cosDeg(angle) * d, MathUtils.sinDeg(angle) * d);
                target.x = MathUtils.clamp(target.x, 0, map.getWidth());
                target.y = MathUtils.clamp(target.y, 0, map.getHeight());
                if (!collision.isBlocked(target.x, target.y, 4f) && !collision.segmentBlocked(origin, target, 0f)) {
                    return target;
                }
            }
            return new Vector2(origin);
        }

        @Override
        public void speedBoost(float multiplier, float seconds) {
            player.applySpeedModifier(multiplier, seconds);
        }

        @Override
        public void lure(Vector2 point, float radius, float seconds) {
            for (Goose g : geese) {
                if (g.getPosition().dst2(point) <= radius * radius) {
                    g.distractWith(point, seconds);
                }
            }
            lures.add(new Lure(new Vector2(point), seconds));
        }

        @Override
        public void addTime(float seconds) {
            timeLeft = Math.max(1f, timeLeft + seconds);
        }
    }

    /** Geese currently chasing the player. Must be 0 to board the bus. */
    public int getChasingCount() {
        int count = 0;
        for (Goose g : geese) {
            if (g.isChasing()) {
                count++;
            }
        }
        return count;
    }

    /** Summary for the end-of-game ticket. */
    public GameResult getResult() {
        return new GameResult(status == Status.WON, elapsed, itemsUsed, eventsTriggered);
    }

    public Status getStatus() {
        return status;
    }

    public float getTimeLeft() {
        return timeLeft;
    }

    public int getItemsUsed() {
        return itemsUsed;
    }

    public int getEventsTriggered() {
        return eventsTriggered;
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Goose> getGeese() {
        return geese;
    }

    public Array<GroundItem> getGroundItems() {
        return groundItems;
    }

    public Array<Lure> getLures() {
        return lures;
    }

    public Array<HiddenEvent> getEvents() {
        return events;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public MapLoader getMap() {
        return map;
    }

    public float getWidth() {
        return map.getWidth();
    }

    public float getHeight() {
        return map.getHeight();
    }
}
