package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import io.github.uoy.eng1.c2t7.goosepatrol.events.Event;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.Inventory;
import io.github.uoy.eng1.c2t7.goosepatrol.inventory.ItemStack;
import io.github.uoy.eng1.c2t7.goosepatrol.utils.Assets;

public class HudUI extends Table {
    private final Inventory inventory;
    private final Assets assets;
    private final Skin skin;

    private final Label timerLabel;
    private final Label toastLabel;
    private final Label geeseLabel;
    private final Table hotbarTable;
    private final Stack[] slotStacks;
    private final Image[] heartImages;
    private final ProgressBar staminaBar;
    private final Table toastPanel;

    private final EventUI eventUI;

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

        geeseLabel = new Label("", skin);
        Table topBar = new Table();
        staminaBar = new ProgressBar(0f, 1f, 0.01f, false, skin);
        staminaBar.setValue(1f);
        Table status = panel();
        status.add(hearts).left().row();
        status.add(staminaBar).width(150).left().padTop(8);

        Table timerPanel = panel();
        timerPanel.add(timerLabel);

        Table geesePanel = panel();
        geesePanel.add(geeseLabel);

        Table left = new Table();
        left.add(status).left().top();
        Table right = new Table();
        right.add(geesePanel).right().top();

        topBar.add(left).expandX().uniformX().left().top();
        topBar.add(timerPanel).top();
        topBar.add(right).expandX().uniformX().right().top();
        add(topBar).growX().top().pad(20, 20, 0, 20).row();

        //EVENTS
        eventUI = new EventUI(skin);
        add(eventUI).right().top().row();

        // TOAST
        toastLabel = new Label("", skin);
        toastPanel = panel();
        toastPanel.add(toastLabel);
        toastPanel.getColor().a = 0f;
        add(toastPanel).expand().bottom().padBottom(15).row();

        // HOTBAR
        hotbarTable = new Table();
        buildHotbar();
        add(hotbarTable).bottom().padBottom(20);

        updateHotbar();

    }

    private Table panel() {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable("panel"));
        panel.pad(10, 16, 10, 16);
        return panel;
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

    public void updateGeese(int dispersed, int target) {
        geeseLabel.setText(dispersed >= target ? "Get to the bus!" : "Geese fed: " + dispersed + " / " + target);
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
        staminaBar.setValue(percent);
    }

    public void showToast(String message) {
        toastLabel.setText(message);
        toastPanel.clearActions();
        toastPanel.getColor().a = 0f;
        toastPanel.addAction(Actions.sequence(
                Actions.fadeIn(0.15f),
                Actions.delay(1.0f),
                Actions.fadeOut(0.4f)));
    }

    public EventUI getEventUI() { return eventUI; }
}
