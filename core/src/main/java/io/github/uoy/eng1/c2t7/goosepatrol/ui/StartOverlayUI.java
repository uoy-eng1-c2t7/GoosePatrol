package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

/** Shown over the paused game at the start, explains the goal and controls. */
public class StartOverlayUI extends Table {
    public StartOverlayUI(Skin skin, int geeseToDisperse) {
        setFillParent(true);
        setBackground(skin.newDrawable("white", new Color(0f, 0f, 0f, 0.6f)));

        Table card = new Table();
        card.setBackground(skin.getDrawable("panel"));
        card.pad(30, 50, 30, 50);

        card.add(new Label("Catch the bus!", skin)).colspan(2).padBottom(10).row();
        card.add(new Label("Feed " + geeseToDisperse + " geese, then reach the bus", skin)).colspan(2).padBottom(24)
                .row();

        addControl(card, skin, "WASD", "Move");
        addControl(card, skin, "Shift", "Sprint");
        addControl(card, skin, "E", "Throw bread");
        addControl(card, skin, "1-9 / Scroll", "Choose item");
        addControl(card, skin, "Esc", "Pause");

        card.add(new Label("Press SPACE to start", skin)).colspan(2).padTop(24);
        add(card);
    }

    private void addControl(Table card, Skin skin, String key, String action) {
        card.add(new Label(key, skin)).right().padRight(30).padBottom(4);
        card.add(new Label(action, skin)).left().padBottom(4).row();
    }
}