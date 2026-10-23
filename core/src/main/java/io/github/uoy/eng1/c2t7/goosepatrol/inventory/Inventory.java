package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;

public class Inventory {
    public final Array<InventorySlot> slots;
    public final int capacity;
    public boolean visible;

    public Inventory(int capacity, Texture slotSprite) {
        this.slots = new Array<>(capacity);
        this.capacity = capacity;
        for (int i = 0; i < capacity; i++) {
            slots.add(new InventorySlot(slotSprite, i + 1, slotSprite.getWidth() * i, 0));
        }
    }

    public void draw(SpriteBatch batch, float posX, float posY) {
        for (int i = 0; i < capacity; i++) {
            slots.get(i).draw(batch, posX, posY);
        }
    }

    public void toggleVisible() { visible = !visible; }

}
