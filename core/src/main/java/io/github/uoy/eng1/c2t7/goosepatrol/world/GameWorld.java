package io.github.uoy.eng1.c2t7.goosepatrol.world;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Item;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemRegistry;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.GameConfig;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Entity;
import io.github.uoy.eng1.c2t7.goosepatrol.world.entities.Player;

public class GameWorld {
    public enum Status {
        RUNNING,
        WON,
        LOST
    }

    private final WorldListener listener;
    private final GameConfig config;
    private final Player player;
    private final Array<Entity> entities = new Array<>();
    private final MapLoader map;

    public GameWorld(GameConfig config, WorldListener listener, TiledMap map) {
        this.config = config;
        this.listener = listener;
        this.player = new Player(new Vector2(100, 100), config.player);

        if (config.startingItems != null) {
            for (String itemId : config.startingItems) {
                Item item = ItemRegistry.get(itemId);
                if (item != null) {
                    player.getInventory().addItem(item, 1);
                }
            }
        }
        entities.add(player);

        this.map = new MapLoader(map);
    }

    public void update(float delta, Vector2 moveInput) {
        player.setMoveInput(moveInput);

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
