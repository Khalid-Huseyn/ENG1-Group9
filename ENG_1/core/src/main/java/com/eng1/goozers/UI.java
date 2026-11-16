package com.eng1.goozers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import java.util.Locale;

/** The inventory, selection highlight and player health bar. */
public class UI {

    private Stage uiStage;
    private Texture invTexture;
    private Texture hlTexture;
    private Image hl;
    private Group inventory;
    private int currentSlot = 0;
    private BitmapFont timerFont;
    private Label timerLabel;
    private int lastDisplayedSecond = -1;

    private static final int BAR_W = 160;
    private static final int BAR_H = 48;
    private static final int SLOT_COUNT = 8;
    private static final int SLOT_SIZE = 16;
    private static final float SCALE = 1.5f;

    private Texture hpBarTexture;
    private Texture fullBar;
    private Actor healthBar;

    // Three individual bread items, all using Main's shared texture.
    private final Image[] breadIcons = new Image[3];

    public UI() {
        createUI();
    }

    private void createUI() {
        uiStage = new Stage(new ScreenViewport());
        invTexture = new Texture(Gdx.files.internal("ui/inventBar.png"));
        invTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        inventory = new Group();
        inventory.setSize(BAR_W * SCALE, BAR_H * SCALE);

        Image background = new Image(invTexture);
        background.setSize(BAR_W * SCALE, BAR_H * SCALE);
        inventory.addActor(background);

        hlTexture = new Texture(Gdx.files.internal("ui/hlSprite.png"));
        hlTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        hl = new Image(hlTexture);
        hl.setSize(SLOT_SIZE * SCALE, SLOT_SIZE * SCALE);
        hl.setTouchable(Touchable.disabled);
        inventory.addActor(hl);

        Table hud = new Table();
        hud.setFillParent(true);
        hud.bottom();
        hud.padBottom(12);
        hud.add(inventory).size(BAR_W * SCALE, BAR_H * SCALE);

        uiStage.addActor(hud);

        timerFont = new BitmapFont();

        timerLabel = new Label("00:00", new Label.LabelStyle(timerFont, Color.WHITE));
        Table timerHud = new Table();
        timerHud.setFillParent(true);
        timerHud.top().right();
        timerHud.padTop(10f);
        timerHud.padRight(10f);
        timerHud.add(timerLabel);

        uiStage.addActor(timerHud);
        uiStage.getViewport().update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        selectSlot(0);
    }

    private void selectSlot(int index) {
        currentSlot = index;
        hl.setPosition(
            (SLOT_SIZE + index * SLOT_SIZE) * SCALE,
            1f + SLOT_SIZE * SCALE
        );
    }

    /** Call before checking F in Main so 1+F on the same frame uses the right slot. */
    public void handleInput() {
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1 + i)) {
                selectSlot(i);
            }
        }
    }

    /** Add one bread icon to each of slots 1, 2 and 3. Does not own the texture. */
    public void addBreadItems(Texture breadTexture) {
        for (int i = 0; i < breadIcons.length; i++) {
            Image bread = new Image(breadTexture);
            bread.setSize(SLOT_SIZE * SCALE, SLOT_SIZE * SCALE);
            bread.setPosition(
                (SLOT_SIZE + i * SLOT_SIZE) * SCALE,
                1f + SLOT_SIZE * SCALE
            );
            bread.setTouchable(Touchable.disabled);
            inventory.addActor(bread);
            breadIcons[i] = bread;
        }
        hl.toFront();
    }

    /** Consume the currently selected loaf; false means empty/non-bread slot. */
    public boolean useSelectedBread() {
        if (currentSlot < 0 || currentSlot >= breadIcons.length) return false;

        Image bread = breadIcons[currentSlot];
        if (bread == null || !bread.isVisible()) return false;

        bread.setVisible(false);
        return true;
    }

    public int getCurrentSlot() {
        return currentSlot;
    }

    public void render() {
        uiStage.getViewport().apply();
        uiStage.act(Gdx.graphics.getDeltaTime());
        uiStage.draw();
    }

    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
    }

    public void createHealthBar(Player player) {
        hpBarTexture = new Texture(Gdx.files.internal("ui/healthBar.png"));
        fullBar = new Texture(Gdx.files.internal("ui/fullBar.png"));
        hpBarTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        fullBar.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        healthBar = new Actor() {
            @Override
            public void draw(Batch batch, float parentAlpha) {
                float percentage = player.getHpPercentage();
                batch.draw(hpBarTexture, getX(), getY(), getWidth(), getHeight());

                int sourceWidth = Math.round(fullBar.getWidth() * percentage);
                float visibleWidth = getWidth() * percentage;
                if (sourceWidth > 0) {
                    batch.draw(
                        fullBar,
                        getX(), getY(),
                        visibleWidth, getHeight(),
                        0, 0,
                        sourceWidth, fullBar.getHeight(),
                        false, false
                    );
                }
            }
        };

        healthBar.setSize(hpBarTexture.getWidth(), hpBarTexture.getHeight());
        healthBar.setPosition(10f, 10f);
        uiStage.addActor(healthBar);}

    public void updateTimer(float elapsedTime) {

        int totalSeconds = (int) elapsedTime;
        if (totalSeconds == lastDisplayedSecond) return;
        lastDisplayedSecond = totalSeconds;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;

        timerLabel.setText(String.format(Locale.ROOT, "%02d:%02d", minutes, seconds));
    }

    public void dispose() {
        uiStage.dispose();
        invTexture.dispose();
        hlTexture.dispose();
        timerFont.dispose();
        if (hpBarTexture != null) hpBarTexture.dispose();
        if (fullBar != null) fullBar.dispose();
        // The bread texture is owned and disposed by Main, not by UI.
    }
}
