package io.github.uoy.eng1.c2t7.goosepatrol.rendering;

import com.badlogic.gdx.utils.Disposable;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;

/** Handles rendering the GameWorld to the screen. 
 *  Does not handle logic, just reads and renders.
 */
public class WorldRenderer implements Disposable {
    private final GameWorld world;

    public WorldRenderer(GameWorld world) {
        this.world = world;
    }

    public void update(float delta) {

    }
    
    public void dispose() {
        
    }
}
