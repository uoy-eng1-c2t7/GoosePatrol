package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import com.badlogic.gdx.graphics.Texture;

public abstract class InventoryItem {
    private Texture texture;
    String name;
    String description;

    public InventoryItem(Texture texture, String name, String description) {
        this.texture = texture;
        this.name = name;
        this.description = description;
    }

    public Texture getTexture() { return this.texture; }

    public abstract void onUse();
}
