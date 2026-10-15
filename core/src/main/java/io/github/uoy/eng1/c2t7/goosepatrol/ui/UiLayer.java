package io.github.uoy.eng1.c2t7.goosepatrol.ui;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;

/**
 * Self contained layer of UI to be drawn on top of the world
 */
public abstract class UiLayer implements Disposable {
    protected final Stage stage;

    protected UiLayer(SpriteBatch batch) {
        stage = new Stage(new ExtendViewport(1280, 720), batch);
    }
    

    public void act(float delta) {
        stage.act(delta);
    }

    public void draw() {
        stage.draw();
    }

    public void resize(int w, int h) {
        stage.getViewport().update(w, h, true);
    }

    public Stage getStage() {
        return stage;
    }

    @Override 
    public void dispose() {
        stage.dispose();
    }

}
