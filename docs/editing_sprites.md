# Adding / editing sprites

This walkthrough shows you how to edit or create a sprite.

---

## Required Tools

- **Image Editor**: You can use whatever you like, i've been using [Photopea](https://www.photopea.com/) . 
- **Texture Packer**: Once again there are multiple options but I have been using [Texture Packer](https://libgdx.com/wiki/tools/texture-packer), specifically the GUI version as I find it easiest

## Files

- `raw-assets/sprites/` - Raw sprite PNGs live here, edit them or add new ones.
- `assets/tiled/campus.tmx` - optional if you want to place items on the map

## Step 1: Create the sprite and repack the atlas
Create the sprite as a PNG in your prefered program, I used Photopea. Export it as a PNG with a transparent background and drop it into `raw-assets/sprites`. 

## Step 2: Repack the Atlas
Create a new atlas called `game` , add the input files (all of the PNGs in the `raw-assets/sprites` folder). Set the output directory to `assets/sprites` then double click the atlas to pack it. It should output `game.atlas` and `game.png` containing all the packed sprites and their locations. 

## Step 3: Use the sprite
Now the sprite is ready to be used, you can access the sprite through the asset manager. 

``` java
public TextureRegion getSprite(String name) {
        TextureRegion region = manager.get(SPRITES, TextureAtlas.class).findRegion(name);
        if (region != null) {
            return region;
        }
        if (warned.add(name)) {
            Gdx.app.error("Assets", "No sprite named '" + name + "' in the atlas");
        }
        return missingSprite();
    }
```

This public method will get the texture region containing the sprite which you can then draw to the screen using the shared sprite batch's methods (.draw()).

The `asset.java` class is a wrapper for LibGDX's asset manager, it is stored in the main Game instance, `GoosePatrol.java`, so every class that has a reference to it (which should be every screen!) can access the asset mananger.

If you need to access the asset manager from the game world you are doing it wrong as these classes are decoupled from rendering and should only handle logic, you should instead look at using the WorldListener.