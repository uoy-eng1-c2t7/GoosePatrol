# Editing the Map

This walkthrough shows you how open up the map in Tiled and edit it.

---

## Required Tools

- **Image Editor**: Optional, needed if you want to edit or add new tiles, you can use any but I've been using [Photopea](https://www.photopea.com/) . 
- **Map Editor** We are using [Tiled](https://www.mapeditor.org/) .

## Files

- `assets/tiled/` - The map and tilemap is stored here.


## Opening the Map in Tiled
Launch Tiled, click 'Open File or Project', navigate to `assets/tiled/campus.tmx` and open it up. You should see something like this:
![Tiled Example 1](/docs/images/tiled_example_1.png)
You can see two types of layers, the purple shape layers known as 'object layers' and the blue grid layers known as 'tile layers'. The game only cares about the object layers, the tile grid layers are purely decorative. For example, if you draw a wall on a tile layer, it won't have any collision, you need to actually draw a rectangle over the wall in the object layer called 'collision' for the game to know about it. 

## Adding Entities
There is an 'object layer' called entites, to add a new entitiy (an item, player, goose ...) add a 'point object' 
![Adding point object](/docs/images/tiled_example_point.png)
The name property is all the game looks for and it determines what entity it spawns there, 'player' and 'goose' are hardcoded. The game will only spawn one player, but you can have as many geese as you'd like. If you want to spawn an item, use the items ID from the item registry. 

## Adding Collision
Draw the wall on a 'tile layer', then go on the 'object layer' called collision and draw a rectangle shape over the wall. You must ONLY USE RECTANGLES - the game does not support any other shapes as the math gets complicated. We could've used a physics engine like Box2D - but getting this all setup and running smoothly is even more complicated - so we didn't.

## Special Layers
Currently the only other layer is called exit, the game looks for a rectangle on this layer and checks if the player enters it, if they do (and aren't being chased by any geese) the game ends.