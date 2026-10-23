package io.github.uoy.eng1.c2t7.goosepatrol.inventory;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

public class InventorySlot {
    final int id;
    private final Texture texture;
    private final Texture selectedTexture;
    private Texture currentTexture;
    private InventoryItem item;
    private final Vector2 cameraOffset;

    boolean isSelected = false;
    private final Rectangle hitbox;
    private final Inventory inventory;

    public InventorySlot(int id, Texture texture, Texture selectedTexture, float cameraOffsetX, float cameraOffsetY, Inventory inventory) {
        this.id = id;
        this.texture = texture;
        this.currentTexture = texture;
        this.selectedTexture = selectedTexture;
        this.item = null;

        this.cameraOffset = new Vector2(cameraOffsetX, cameraOffsetY);
        this.hitbox = new Rectangle(cameraOffsetX, cameraOffsetY, texture.getWidth(), texture.getHeight());
        this.inventory = inventory;
    }

    public InventoryItem getItem() {
        return this.item;
    }

    public void setItem(InventoryItem item) {
        this.item = item;
    }

    public boolean checkIfClicked(OrthographicCamera camera) {
        if (Gdx.input.justTouched()) {
            Vector3 touch = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touch);
            return hitbox.contains(touch.x, touch.y);
        }

        return false;
    }

    public void toggleSelected() {
        if (currentTexture == texture) { currentTexture = selectedTexture; }
        else if (currentTexture == selectedTexture) { currentTexture = texture; }
        isSelected = !isSelected;
    }

    public void deselect() {
        currentTexture = texture;
        isSelected = false;
    }

    public void draw(SpriteBatch batch, float posX, float posY) {
        batch.draw(currentTexture, posX + cameraOffset.x, posY + cameraOffset.y, texture.getHeight(), texture.getWidth());
        if (item != null) drawItem(batch, posX + cameraOffset.x, posY + cameraOffset.y);
    }

    private void drawItem(SpriteBatch batch, float posX, float posY) {
        Texture itemTexture = item.getTexture();
        batch.draw(itemTexture, posX, posY, itemTexture.getHeight(), itemTexture.getWidth());
    }


}
