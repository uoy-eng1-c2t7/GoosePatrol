package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.Array;
import io.github.uoy.eng1.c2t7.goosepatrol.GoosePatrol;

public class Inventory {
    public final Array<InventorySlot> slots;
    public final int capacity;
    public boolean visible;
    private final GoosePatrol game;

    private final Texture slotSprite;
    private final Texture selectedSlotSprite;

    public Inventory(int capacity, GoosePatrol game) {
        this.slots = new Array<>(capacity);
        this.capacity = capacity;
        this.game = game;

        this.slotSprite = game.getAssets().getInventorySlot();
        this.selectedSlotSprite = game.getAssets().getSelectedSlot();

        for (int i = 0; i < capacity; i++) {
            slots.add(new InventorySlot(i, slotSprite, selectedSlotSprite, slotSprite.getWidth() * i, 0, this));
        }

    }

    public void update(OrthographicCamera camera) {
        for (InventorySlot slot: slots) {
            if (slot.checkIfClicked(camera)) {
                slot.toggleSelected();
                deselectPreviouslySelected(slot.id);
            }
        }
    }

    public void deselectPreviouslySelected(int id) {
        // cant use inventory slots iterator twice. since the above function will already be using it need to do this instead
        for (int i = 0; i < capacity; i++) {
            if (i != id) slots.get(i).deselect();
        }
    }

    public void draw(SpriteBatch batch, float posX, float posY) {
        for (InventorySlot slot: slots) {
            slot.draw(batch, posX, posY);
        }
    }

    public void toggleVisible() { visible = !visible; }

}
