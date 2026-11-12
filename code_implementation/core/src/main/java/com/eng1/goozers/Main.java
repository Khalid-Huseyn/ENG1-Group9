package com.eng1.goozers;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private float playerX;
    private float playerY;
    private float statueX;
    private float statueY;
    private boolean nearDuck = false;
    private boolean inspectedDuck = false;
    private Sprite character;
    private float speed = 50f;
    private Texture map;
    private FitViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;

    @Override
    public void create() {
        map = new Texture(Gdx.files.internal("map/test_map.png"));
        character = new Sprite(new Texture(Gdx.files.internal("map/character.png")));
        batch = new SpriteBatch();

        character.setSize(32, 32);
        character.setPosition(100, 100);

        character.getTexture().setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        map.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        viewport = new FitViewport(map.getWidth(), map.getHeight());
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        font = new BitmapFont();
    }

    @Override
    public void render() {
        input();

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);

        batch.begin();
        batch.draw(map, 0, 0);

        interact();
        font.draw(batch, "(" + character.getX() + "," + character.getY() + ")", 200, map.getHeight() - 10);
        // line above is for testing if you need coordinates of character
        character.draw(batch);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        map.dispose();
        character.getTexture().dispose();
        batch.dispose();
    }
    private void input() {
        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            character.translateY(speed * Gdx.graphics.getDeltaTime());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            character.translateY(-speed * Gdx.graphics.getDeltaTime());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            character.translateX(speed * Gdx.graphics.getDeltaTime());
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            character.translateX(-speed * Gdx.graphics.getDeltaTime());
        }
    }

    private void interact() {

        float playerX = character.getX() + character.getWidth() / 2f;
        float playerY = character.getY() + character.getHeight() / 2f;
        
        statueX = 145;
        statueY = 145; // CHANGE THIS LATER!   TOO NASTY AT THE MOMENT!

        float dx = playerX - statueX;
        float dy = playerY - statueY;
        nearDuck = dx * dx + dy * dy <= 24f * 24f;

        if (nearDuck && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            inspectedDuck = !inspectedDuck;
        }

        if (nearDuck) {
            if (inspectedDuck) {
                font.draw(batch, "Help me pass my exam Long Boi!", 10, map.getHeight() - 10);
            } else {
                font.draw(batch, "press E to interact", 10, map.getHeight() - 10);
            }
        } else {
            inspectedDuck = false;
        }
    }
}

