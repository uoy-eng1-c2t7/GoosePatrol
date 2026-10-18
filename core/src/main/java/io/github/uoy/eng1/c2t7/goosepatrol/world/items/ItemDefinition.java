package io.github.uoy.eng1.c2t7.goosepatrol.world.items;

/**
 * Description of one kind of item, create new items in {@link ItemRegistry}.
 */
public final class ItemDefinition {
    private final String id;
    private final String name;
    private final String description;
    private ItemEffect effect;

    ItemDefinition(String id, String name, String description, ItemEffect effect) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.effect = effect;
    }

    /** Unique key.What config.json and tiled 'itemType' use. */
    public String getId() {
        return id;
    }

    /** Name shown in the HUD. */
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ItemEffect getEffect() {
        return effect;
    }

}
