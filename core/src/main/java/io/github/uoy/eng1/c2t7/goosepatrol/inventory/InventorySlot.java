package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class InventorySlot {
    private final Texture texture;
    private InventoryItem item;
    int id;

    public InventorySlot(Texture texture, int id) {
        this.texture = texture;
        this.id = id;
        this.item = null;
    }

    public InventoryItem getItem() {
        return this.item;
    }

    public void setItem(InventoryItem item) {
        this.item = item;
    }

    public void draw(SpriteBatch batch, int posX, int posY) {
        batch.draw(texture, posX, posY, texture.getHeight(), texture.getWidth());
        if (item != null) drawItem(batch, posX, posY);
    }

    private void drawItem(SpriteBatch batch, int posX, int posY) {
        Texture itemTexture = item.getTexture();
        batch.draw(itemTexture, posX, posY, itemTexture.getHeight(), itemTexture.getWidth());
    }


}
