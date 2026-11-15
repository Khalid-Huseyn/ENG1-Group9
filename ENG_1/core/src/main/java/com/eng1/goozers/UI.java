package com.eng1.goozers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class UI {

    private Stage uiStage;

    private Texture invTexture;
    private Texture hlTexture;
    private Image hl;
    private int currentSlot = 0;
    private static final int barW = 160;
    private static final int barH = 48;
    private static final int slotCount = 8;
    private static final int slotSize = 16;
    private static final float scaleF = 1.5f;

    public UI() {
        createUI();}

    private void createUI() {
        uiStage = new Stage(new ScreenViewport());
        invTexture = new Texture(Gdx.files.internal("ui/inventBar.png"));
        invTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        Group inventory = new Group();
        inventory.setSize(barW * scaleF, barH * scaleF);
        Image background = new Image(invTexture);
        background.setSize(barW * scaleF, barH * scaleF);
        inventory.addActor(background);

        createHighlight();
        hl = new Image(hlTexture);
        hl.setSize(slotSize * scaleF, slotSize * scaleF);
        hl.setTouchable(Touchable.disabled);
        inventory.addActor(hl);

        Table hud = new Table();
        hud.setFillParent(true);
        hud.bottom();
        hud.padBottom(12);
        hud.add(inventory).size(barW * scaleF, barH * scaleF);

        uiStage.addActor(hud);
        uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        selectSlot(0);
    }

    private void createHighlight() {
        hlTexture = new Texture(Gdx.files.internal("ui/hlSprite.png"));
        hlTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
    }

    private void selectSlot(int index) {
        currentSlot = index;
        hl.setPosition((slotSize + index * slotSize) * scaleF, 1 + slotSize * scaleF);
    }

    public void render() {
        for (int i = 0; i < slotCount; i++) {

            if (Gdx.input.isKeyJustPressed(
                Input.Keys.NUM_1 + i
            )) {
                selectSlot(i);
            }}
        uiStage.getViewport().apply();
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
    }

    public void dispose() {
        uiStage.dispose();
        invTexture.dispose();
        hlTexture.dispose();
    }
}
