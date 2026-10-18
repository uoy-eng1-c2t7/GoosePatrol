package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import io.github.uoy.eng1.c2t7.goosepatrol.world.GameWorld;
import io.github.uoy.eng1.c2t7.goosepatrol.world.items.Inventory;

/**
 * A heads up display drawn over the world, displays the bus countdown, geese chasing, counters,
 * A pop up message line and the inventory bar.
 * Call {@link #update(GameWorld))} every frame.
 * Hud
 */
public class Hud extends UiLayer {
    private static final float TOAST_SECONDS = 2.5f;
    private static final float TOAST_FADE = 0.5f;

    private final Label timerLabel;
    private final Label geeseLabel;
    private final Label statsLabel;
    private final Label toastLabel;
    private final Label[] slotLabels = new Label[Inventory.MAX_SLOTS];

    public Hud(SpriteBatch batch, Skin skin) {
        super(batch);

        timerLabel = new Label("", skin);
        geeseLabel = new Label("", skin);
        statsLabel = new Label("", skin);
        toastLabel = new Label("", skin);
        toastLabel.getColor().a = 0f;

        Table slots = new Table();
        for (int i = 0; i < slotLabels.length; i++) {
            slotLabels[i] = new Label("", skin);
            slots.add(slotLabels[i]).padLeft(12).padRight(12);
        }

        Table root = new Table();
        root.setFillParent(true);
        root.pad(12);
        root.add(timerLabel).left().expandX();
        root.add(geeseLabel).center().expandX();
        root.add(statsLabel).right().expandX();
        root.row();
        root.add(toastLabel).colspan(3).padTop(24);
        root.row();
        root.add().colspan(3).expand();
        root.row();
        root.add(slots).colspan(3);
        stage.addActor(root);
    }

    /** Refreshes every label from the current world state. */
    public void update(GameWorld world) {
        int secs = (int) Math.ceil(world.getTimeLeft());
        timerLabel.setText(String.format("Bus departs in %d:%02d", secs / 60, secs % 60));
        geeseLabel.setText("Geese chasing: " + world.getChasingCount());
        statsLabel.setText("Items used: " + world.getItemsUsed() + "   Events: " + world.getEventsTriggered());

        Inventory inv = world.getInventory();
        for (int i = 0; i < slotLabels.length; i++) {
            Inventory.Stack stack = inv.get(i);
            if (stack == null) {
                slotLabels[i].setText("");
            } else {
                slotLabels[i].setText("[" + (i + 1) + "] " + stack.getDefinition().getName()
                        + " x" + stack.getCount());
            }
        }
    }

    /** Shows a short message under the top bar that fades out by itself. */
    public void showToast(String message) {
        toastLabel.setText(message);
        toastLabel.clearActions();
        toastLabel.getColor().a = 1f;
        toastLabel.setColor(Color.WHITE);
        toastLabel.addAction(Actions.sequence(Actions.delay(TOAST_SECONDS), Actions.fadeOut(TOAST_FADE)));
    }
}
