package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.events.Event;
import io.github.uoy.eng1.c2t7.goosepatrol.events.EventRegistry;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Item;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemRegistry;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemStack;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.DroppedItem;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;
import io.github.uoy.eng1.c2t7.goosepatrol.world.pathfinding.NavGrid;

public class GameWorld {
    public enum Status {
        RUNNING,
        WON,
        LOST
    }

    private final WorldListener listener;
    private final Player player;
    private final Array<Entity> entities = new Array<>();
    private final MapLoader map;
    private final NavGrid navGrid;
    private final GameConfig config;
    private static final float THROW_SPEED = 500f;
    private static final Array<Rectangle> NO_WALLS = new Array<>();
    private static final float DRIVER_TALK_RANGE = 320f;
    private int geeseDispersed = 0;
    private int itemsUsed = 0;
    private int eventsTriggered = 0;
    private Status status = Status.RUNNING;
    private float timeRemaining;
    private float timeTaken;

    private static final float EVENT_ROLL_INTERVAL = 1f;
    private float eventTimer = EVENT_ROLL_INTERVAL;

    public GameWorld(GameConfig config, WorldListener listener, TiledMap tiledMap) {
        EventRegistry.resetAll();
        timeRemaining = config.gameDurationSeconds;
        this.listener = listener;
        this.config = config;
        this.map = new MapLoader(tiledMap);
        navGrid = new NavGrid(map.getWidth(), map.getHeight(), map.getWalls(), config.goose.radius);
        this.player = new Player(map.getPlayerSpawn(), config.player);
        entities.add(player);

        for (Vector2 spawnPos : map.getGooseSpawns()) {
            Goose goose = new Goose(spawnPos, config.goose, player, navGrid);
            entities.add(goose);
        }

        for (MapLoader.ItemSpawn spawn : map.getItemSpawns()) {
            entities.add(new DroppedItem(spawn.position, spawn.item, 1));
        }

        if (config.startingItems != null) {
            for (String itemId : config.startingItems) {
                Item item = ItemRegistry.get(itemId);
                if (item != null) {
                    player.getInventory().addItem(item, 1);
                }
            }
        }
    }

    /**
     * Advances the game by one frame. Does nothing once the game is won or lost.
     *
     * @param delta     seconds since the last frame
     * @param moveInput movement direction from the keyboard, each axis -1, 0 or 1
     */
    public void update(float delta, Vector2 moveInput) {
        if (status != Status.RUNNING) {
            return;
        }
        timeRemaining = Math.max(0f, timeRemaining - delta); // FR-14
        if (timeRemaining <= 0f) {
            status = Status.LOST; // UR_PLY_LOSE
            return;
        }
        player.setMoveInput(moveInput);
        updateEntities(entities, delta);
        alertNearbyGeese();
        checkPecks();
        checkExit();
        updateEventTimer(delta);
    }

    private void updateEventTimer(float delta) {
        eventTimer -= delta;
        if (eventTimer <= 0) {
            eventTimer = EVENT_ROLL_INTERVAL;
            runEvents();
        }
    }

    /**
     * Rolls each hidden event's chance once; triggered events are announced and
     * counted (FR-06, FR-13).
     */
    private void runEvents() {
        for (Event event : EventRegistry.getEvents().values()) {
            if (MathUtils.random() <= event.getProbability() && event.trigger(this)) {
                eventsTriggered++;
                listener.onEventTriggered(event);
            }
        }
    }

    private void updateEntities(Array<Entity> entities, float delta) {
        // Check if player walks over any item
        for (Entity entity : entities) {
            if (entity instanceof DroppedItem) {
                DroppedItem drop = (DroppedItem) entity;
                if (drop.canPickup() && player.getBounds().overlaps(drop.getBounds())) {
                    if (player.getInventory().addItem(drop.getItem(), drop.getCount())) {
                        drop.destroy(); // Mark for removal
                        listener.onInventoryChanged(); // Refresh hotbar UI
                    }
                }
            }
        }

        // Update entity positions and check for collisions
        for (Entity entity : entities) {
            entity.updateIntent(delta);

            Vector2 pos = entity.getPosition();
            Vector2 vel = entity.getVelocity();

            // Move X and check collision
            pos.x += vel.x * delta;
            entity.syncBounds();
            for (Rectangle wall : wallsFor(entity)) {
                if (Intersector.overlaps(entity.getBounds(), wall)) {
                    pos.x -= vel.x * delta;
                    entity.syncBounds();
                    break;
                }
            }
            // Move Y and check collision
            pos.y += vel.y * delta;
            entity.syncBounds();
            for (Rectangle wall : wallsFor(entity)) {
                if (Intersector.overlaps(entity.getBounds(), wall)) {
                    pos.y -= vel.y * delta;
                    entity.syncBounds();
                    break;
                }
            }
            entity.update(delta);
        }

        // Clean up destroyed items
        for (int i = entities.size - 1; i >= 0; i--) {
            Entity e = entities.get(i);
            if (e instanceof DroppedItem && ((DroppedItem) e).isDestroyed()) {
                entities.removeIndex(i);
            } else if (e instanceof Goose && ((Goose) e).isGone()) {
                entities.removeIndex(i);
                geeseDispersed++;
                listener.onGooseDispersed();
            }
        }
    }

    /** A goose that spots the player sets off the geese around it. */
    private void alertNearbyGeese() {
        for (int i = 0; i < entities.size; i++) {
            Entity spotter = entities.get(i);
            if (spotter instanceof Goose && ((Goose) spotter).consumeSpottedPlayer()) {
                for (Entity entity : entities) {
                    if (entity instanceof Goose && ((Goose) entity).canBeLured()
                            && entity.getPosition().dst(spotter.getPosition()) < config.goose.alertRange) {
                        ((Goose) entity).startChasing();
                    }
                }
            }
        }
    }

    private void checkPecks() {
        for (Entity entity : entities) {
            if (entity instanceof Goose && ((Goose) entity).isChasing()
                    && Intersector.overlaps(entity.getBounds(), player.getBounds())) {
                if (player.takePeck()) {
                    listener.onPlayerPecked();
                    if (player.getHearts() == 0) {
                        status = Status.LOST;
                    }
                }
                return;
            }
        }
    }

    private void checkExit() {
        if (map.getExitZone() != null && Intersector.overlaps(player.getBounds(), map.getExitZone())
                && hasDispersedEnough() && !isPlayerBeingChased()) {
            status = Status.WON;
            timeTaken = config.gameDurationSeconds - timeRemaining;
            listener.onExitReached();
        }
    }

    public boolean hasDispersedEnough() {
        return geeseDispersed >= config.geeseToDisperse;
    }

    /**
     * What the bus driver is saying, or null if the player is too far away to hear.
     */
    public String getDriverMessage() {
        Rectangle exit = map.getExitZone();
        if (exit == null) {
            return null;
        }
        float distance = player.getPosition().dst(exit.x + exit.width / 2f, exit.y + exit.height / 2f);
        if (distance > DRIVER_TALK_RANGE) {
            return null;
        }
        if (!hasDispersedEnough()) {
            return "Feed " + (config.geeseToDisperse - geeseDispersed) + " more geese first!";
        }
        return isPlayerBeingChased() ? "No geese on the bus!" : "Hop on!";
    }

    private boolean isPlayerBeingChased() {
        for (Entity entity : entities) {
            if (entity instanceof Goose) {
                if (((Goose) entity).isChasing())
                    return true;
            }
        }
        return false;
    }

    private Array<Rectangle> wallsFor(Entity entity) {
        return entity.ignoresWalls() ? NO_WALLS : map.getWalls();
    }

    public void throwFood(Vector2 fromPos, float rotDeg, Item item, float lifetime, float attractRadius,
            float distractTime) {
        Vector2 throwDir = new Vector2(MathUtils.cosDeg(rotDeg), MathUtils.sinDeg(rotDeg));
        Vector2 spawnPos = fromPos.cpy().add(throwDir.cpy().scl(20f));
        Vector2 throwVel = throwDir.scl(THROW_SPEED);

        DroppedItem drop = new DroppedItem(spawnPos, item, 1, throwVel, true, 0.6f);
        drop.setLifetime(lifetime);
        entities.add(drop);

        int lured = 0;
        for (Entity entity : entities) {
            if (entity instanceof Goose && lured < config.goose.geesePerFood) {
                Goose goose = (Goose) entity;
                if (goose.canBeLured() && goose.getPosition().dst(spawnPos) < attractRadius) {
                    goose.distract(drop.getPosition(), distractTime);
                    lured++;
                }
            }
        }
    }

    /**
     * Uses the item in the selected hotbar slot (UR_PLY_INVENTORY, FR-12).
     *
     * @return the item that was used, or null if the slot was empty or the item
     *         refused
     */
    public Item useActiveItem() {
        ItemStack active = player.getInventory().getActiveStack();
        if (active == null) {
            return null;
        }
        Item item = active.getItem();
        if (!item.use(this, player)) {
            return null;
        }
        player.getInventory().consumeActiveItem();
        itemsUsed++;
        listener.onInventoryChanged();
        return item;
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Entity> getEntities() {
        return entities;
    }

    public Status getStatus() {
        return status;
    }

    public float getTimeRemaining() {
        return timeRemaining;
    }

    public float getTimeTaken() {
        return timeTaken;
    }

    public NavGrid getNavGrid() {
        return navGrid;
    }

    public MapLoader getMap() {
        return map;
    }

    public int getGeeseDispersed() {
        return geeseDispersed;
    }

    public int getGeeseToDisperse() {
        return config.geeseToDisperse;
    }

    public int getItemsUsed() {
        return itemsUsed;
    }

    public int getEventsTriggered() {
        return eventsTriggered;
    }
}
