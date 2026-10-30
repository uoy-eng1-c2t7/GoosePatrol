# Adding a new Item

This walkthrough shows you how to create a new collectible item, give it a custom behavior and spawn it into the world.

---

## Required Tools

- **Image Editor**: You can use whatever you like, i've been using [Photopea](https://www.photopea.com/) . 
- **Texture Packer**: Once again there are multiple options but I have been using [Free Texture Packer](https://free-tex-packer.org/) .
- **Map Editor** needed if you are spawning the item on the map, we are using [Tiled](https://www.mapeditor.org/) .

## Files

- `assets/sprites/` - sprite PNG and atlas will need to be replaced with updated version containing your new item's sprite.
- `assets/config.json` - optional if you want to add the item to the players starting inventory
- `assets/tiled/campus.tmx` - optional if you want to place items on the map

## Step 1: Create the sprite and repack the atlas
1. draw your item sprite and save it as `[itemname].png`, use an appropiate canvas size, bearing in mind a 32x32 tile is meant to represent around 1m - maybe draw it relative to the player size.  
2. Repack the atlas with all the existing sprites + your new sprite, make sure 'allow rotation' is turned off, 'format' is set to Spine, 'Texture name' is game and 'Texture format' is png. Replace the old atlas and png with your new one.

## Step 2: Register the Item in 'ItemRegistry.java'
Open `ItemRegistry.java` abd add your new item constant:

```java
public class ItemRegistry {
    private static final Map<String, Item> ITEMS = new HashMap<>();

    public static final Item BREAD = register(new Item(
            "bread", "Bread", "bread.png", 16,
            (world, player) -> {
                world.throwFood(player.getPosition(), player.getRotation(), ItemRegistry.get("bread"), 8f, 180f, 3.0f);
                return true;
            }));

    public static final Item NEW_ITEM = register(new Item(
            "new_item", "New Item", "new_item.png", 1,
            (world, player) -> {
                // What should your new item do?
                return true;
            }));

    public static Item register(Item item) {
        ITEMS.put(item.getId(), item);
        return item;
    }

    public static Item get(String id) {
        return ITEMS.get(id);
    }
}
```
The strings, in order are, `ID` (for referencing), `displayName` (for ui), `spriteName` (same as the atlas, must end in .png) and `maxStack` (how many of your item can be stacked together).

As well as all of these arguments, you must also pass a 'lambda function' which defines what your item will do. In this function the item has access to the world and the player, so you can do lots. In world I defined a public throwFood() function and use this in the bread items lambda. The lambda returns a boolean, `true` if the item should be consumed, `false` if not. Most consumables should return true.

## Step 3: Spawn the Item in Game
You have two ways to give the player access to this item:
- Add your item to the starting Inventory in `assets/config.json` by its ID which you just defined.
- As a map pickup in Tiled, open up the entities layer and add a 'point object', the objects name determined which item will spawn there, as always use the item's ID. 