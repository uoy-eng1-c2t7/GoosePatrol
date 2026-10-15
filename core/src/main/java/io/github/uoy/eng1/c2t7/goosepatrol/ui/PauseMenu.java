package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import org.w3c.dom.Text;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Overlay shown while the game is paused, dims the frozen game and offers
 * resume,
 * restart and main menu options. The class builds and draws the menu, what each
 * button does is decided by the caller.
 */
public class PauseMenu extends UiLayer {
    private static final float DIM_ALPHA = 0.6f;
    private static final float BUTTON_WIDTH = 250f;
    private static final float BUTTON_HEIGHT = 60f;

    private final Label title;
    private final TextButton resumeButton;

    /**
     * @param batch      the shared batch used for drawing 
     * @param skin       the loaded UI skin
     * @param onResume   run when Resume is clicked
     * @param onRestart  run when Restart is clicked
     * @param onMainMenu run when Main Menu is clicked
     */
    public PauseMenu(SpriteBatch batch, Skin skin, Runnable onResume, Runnable onRestart, Runnable onMainMenu) {
        super(batch);

        // Full window dark layer to dim the game, uses white pixel drawable from uiskin
        Image dim = new Image(skin.getDrawable("white"));
        dim.setColor(0f, 0f, 0f, DIM_ALPHA);  // tint the pixel by this, keep alpha low enough to see game behind
        dim.setFillParent(true);
        stage.addActor(dim);

        // Center title and buttons
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        title = new Label("PAUSED", skin);
        table.add(title).padBottom(40).row();

        resumeButton = addButton(table, skin, "Resume", onResume);
        addButton(table, skin, "Restart", onRestart);
        addButton(table, skin, "Main Menu", onMainMenu);
    }

    /** Changes the pause menu heading */
    public void setTitle(String text) {
        title.setText(text);
    }

    /** Changes the first button's text */
    public void setResumeText(String text)
    {
        resumeButton.setText(text);
    }

    private TextButton addButton(Table table, Skin skin, String text, final Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new ClickListener() {
            @Override 
            public void clicked(InputEvent event, float x, float y) {
                action.run();
            }
        });
        table.add(button).width(BUTTON_WIDTH).height(BUTTON_HEIGHT).padBottom(20).row();
        return button;
    }

}
