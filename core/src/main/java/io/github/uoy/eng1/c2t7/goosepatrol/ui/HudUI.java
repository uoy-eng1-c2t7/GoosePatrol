package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Inventory;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemStack;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Assets;

public class HudUI extends Table {
    private final Inventory inventory;
    private final Assets assets;
    private final Skin skin;

    private final Label timerLabel;
    private final Label toastLabel;
    private final Table hotbarTable;
    private final Stack[] slotStacks;
    private final Image[] heartImages;
    private final Image staminaFill;

    public HudUI(Inventory inventory, Assets assets, int maxHearts) {
        this.inventory = inventory;
        this.assets = assets;
        skin = assets.getSkin();

        slotStacks = new Stack[inventory.getSlotCount()];
        heartImages = new Image[maxHearts];

        setFillParent(true);

        // Hearts and Timer
        timerLabel = new Label("05:00", skin);
        Table hearts = new Table();
        for (int i = 0; i < heartImages.length; i++) {
            heartImages[i] = new Image(skin.getDrawable("heart_full"));
            hearts.add(heartImages[i]).size(32).padRight(4);
        }

        Table topBar = new Table();
        staminaFill = new Image(skin.newDrawable("white", new Color(0.35f, 0.8f, 0.4f, 1f)));
        Table status = new Table();
        status.add(hearts).left().row();
        status.add(staminaFill).size(180, 8).left().padTop(6);

        topBar.add(status).expandX().uniformX().left().top();
        topBar.add(timerLabel).top();
        topBar.add().expandX().uniformX();
        add(topBar).growX().top().pad(20, 20, 0, 20).row();

        // TOAST
        toastLabel = new Label("", skin);
        toastLabel.getColor().a = 0f;
        add(toastLabel).expand().bottom().padBottom(15).row();

        // HOTBAR
        hotbarTable = new Table();
        buildHotbar();
        add(hotbarTable).bottom().padBottom(20);

        updateHotbar();
    }

    private void buildHotbar() {
        hotbarTable.clear();
        for (int i = 0; i < inventory.getSlotCount(); i++) {
            Stack slotStack = new Stack();
            slotStacks[i] = slotStack;
            hotbarTable.add(slotStack).size(48, 48).pad(4);
        }
    }

    public void updateHotbar() {
        ItemStack[] slots = inventory.getSlots();
        int selected = inventory.getSelectedSlot();
        for (int i = 0; i < slots.length; i++) {
            Stack stack = slotStacks[i];
            stack.clearChildren();
            // Background (selected highlight vs normal)
            String bgName = (i == selected) ? "slot_selected" : "slot";
            Image bg = new Image(skin.getDrawable(bgName));
            stack.add(bg);
            // Item Icon (from TextureAtlas)
            ItemStack itemStack = slots[i];
            if (itemStack != null) {
                Image itemImg = new Image(
                        new TextureRegionDrawable(assets.getSprite(itemStack.getItem().getSpriteName())));
                stack.add(itemImg);
                // Stack count label in bottom right corner
                if (itemStack.getCount() > 1) {
                    Label count = new Label(String.valueOf(itemStack.getCount()), skin);
                    count.setAlignment(Align.bottomRight);
                    stack.add(count);
                }
            }
        }
    }

    public void updateTimer(float remainingSeconds) {
        int minutes = (int) (remainingSeconds / 60);
        int seconds = (int) (remainingSeconds % 60);
        timerLabel.setText(String.format("%02d:%02d", minutes, seconds));
    }

    public void updateHearts(int hearts) {
        for (int i = 0; i < heartImages.length; i++) {
            heartImages[i].setDrawable(skin.getDrawable(i < hearts ? "heart_full" : "heart_empty"));
        }
    }

    public void updateStamina(float percent) {
        staminaFill.setScaleX(percent);
    }

    public void showToast(String message) {
        toastLabel.setText(message);
        toastLabel.clearActions();
        toastLabel.getColor().a = 0f;
        toastLabel.addAction(Actions.sequence(
                Actions.fadeIn(0.15f),
                Actions.delay(1.0f),
                Actions.fadeOut(0.4f)));
    }
}
