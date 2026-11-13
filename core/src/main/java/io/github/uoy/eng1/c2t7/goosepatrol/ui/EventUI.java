package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import io.github.uoy.eng1.c2t7.goosepatrol.events.Event;

/** Shows a short-lived panel each time a hidden event fires (FR-06). */
public class EventUI extends Table {
    private final Skin skin;

    public EventUI(Skin skin) {
        this.skin = skin;
        center();
    }

    /** Adds a panel naming the event, which fades out after a few seconds. */
    public void announceEvent(Event event) {
        Table panel = panel();
        panel.add(new Label("New event!", skin)).row();
        panel.add(new Label(event.getName() + ": ", skin));
        panel.add(new Label(event.getDescription(), skin));

        panel.addAction(Actions.sequence(
                Actions.fadeIn(0.15f),
                Actions.delay(3f),
                Actions.fadeOut(0.4f),
                Actions.removeActor()));

        this.add(panel).row();
    }

    private Table panel() {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable("panel"));
        panel.pad(5, 8, 5, 8);
        return panel;
    }
}
