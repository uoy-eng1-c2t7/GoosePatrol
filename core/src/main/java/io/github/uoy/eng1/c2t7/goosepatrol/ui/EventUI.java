package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import io.github.uoy.eng1.c2t7.goosepatrol.events.Event;

public class EventUI extends Table {

    public static class NewEvent {
        // time to show new event announcement
        public float timeRemaining;
        public Event event;


        public NewEvent(float timeRemaining, Event event) {
            this.event = event;
            this.timeRemaining = timeRemaining;
        }

    }


    private Array<NewEvent> newEvents;

    private final Skin skin;

    public EventUI(Skin skin) {
        setVisible(true);
        Label nameLabel = new Label("New Event", skin);
        this.skin = skin;

        this.center();

        this.setDebug(true);
    }

    public void announceEvent(Event event) {
        Table panel = panel();
        panel.add(new Label("New event!", skin)).row();
        panel.add(new Label(event.getName() + ": ", skin));
        panel.add(new Label(event.getDescription(), skin));

        panel.addAction(Actions.sequence(
            Actions.fadeIn(0.15f),
            Actions.delay(3f),
            Actions.fadeOut(0.4f),
            Actions.removeActor()
        ));

        this.add(panel).row();

    }

    public void renderAnnouncements() {
    }

    private Table panel() {
        Table panel = new Table();
        panel.setBackground(skin.getDrawable("panel"));
        panel.pad(5, 8, 5, 8);
        return panel;
    }
}
