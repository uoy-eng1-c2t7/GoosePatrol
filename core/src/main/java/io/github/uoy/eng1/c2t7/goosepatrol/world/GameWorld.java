package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Item;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemRegistry;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.DroppedItem;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Goose;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;

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
    private float exitCooldown = 0f;

    public GameWorld(GameConfig config, WorldListener listener, TiledMap tiledMap) {
        this.listener = listener;
        this.map = new MapLoader(tiledMap);

        this.player = new Player(map.getPlayerSpawn(), config.player);
        entities.add(player);

        for (Vector2 spawnPos : map.getGooseSpawns()) {
            Goose goose = new Goose(spawnPos, config.goose, player);
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

    public void update(float delta, Vector2 moveInput) {
        player.setMoveInput(moveInput);
        updateEntities(entities, delta);
        checkExit(delta);
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
            for (Rectangle wall : map.getWalls()) {
                if (Intersector.overlaps(entity.getBounds(), wall)) {
                    pos.x -= vel.x * delta;
                    entity.syncBounds();
                    break;
                }
            }
            // Move Y and check collision
            pos.y += vel.y * delta;
            entity.syncBounds();
            for (Rectangle wall : map.getWalls()) {
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
            }
        }
    }

    private void checkExit(float delta) {
        if (exitCooldown > 0) {
            exitCooldown -= delta;
        }

        if (map.getExitZone() != null && Intersector.overlaps(player.getBounds(), map.getExitZone())
                && exitCooldown <= 0) {
            listener.onExitReached(isPlayerBeingChased());
            exitCooldown = 2.0f; // wait 2 seconds before trying again
        }
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

    public void throwFood(Vector2 fromPos, float rotDeg, Item item, float lifetime, float attractRadius,
            float distractTime) {
        Vector2 throwDir = new Vector2(MathUtils.cosDeg(rotDeg), MathUtils.sinDeg(rotDeg));
        Vector2 spawnPos = fromPos.cpy().add(throwDir.cpy().scl(20f));
        Vector2 throwVel = throwDir.scl(200f);

        DroppedItem drop = new DroppedItem(spawnPos, item, 1, throwVel, true, 0.6f);
        entities.add(drop);

        // Alert nearby geese with this item's specific duration and radius
        for (Entity entity : entities) {
            if (entity instanceof Goose) {
                Goose goose = (Goose) entity;
                if (goose.isChasing() && goose.getPosition().dst(spawnPos) < attractRadius) {
                    goose.distract(spawnPos, distractTime);
                }
            }
        }
    }

    public Player getPlayer() {
        return player;
    }

    public Array<Entity> getEntities() {
        return entities;
    }

    public MapLoader getMap() {
        return map;
    }
}
