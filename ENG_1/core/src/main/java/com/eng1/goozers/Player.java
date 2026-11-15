package com.eng1.goozers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Player {

    private static final float PPM = 32f;
    private static final float SPEED = 50f;

    private final Texture texture;
    private final Sprite character;
    private final Body body;
    private final int maxHP = 100;
    private int currentHP = maxHP;

    public Player(World world, float startX, float startY) {
        texture = new Texture(Gdx.files.internal("map/character.png"));
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        character = new Sprite(texture);
        character.setSize(32f, 32f);
        character.setPosition(startX, startY);

        BodyDef playerDef = new BodyDef();
        playerDef.type = BodyDef.BodyType.DynamicBody;
        playerDef.fixedRotation = true;
        playerDef.position.set((startX + 16f) / PPM, (startY + 8f) / PPM);

        body = world.createBody(playerDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(4f / PPM, 3f / PPM); // 8 x 6 pixel hitbox at the feet
        body.createFixture(shape, 1f);
        shape.dispose();
    }

    public void input() {
        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy++;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy--;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx++;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx--;

        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0f) {
            dx /= length;
            dy /= length;
        }

        body.setLinearVelocity(dx * SPEED / PPM, dy * SPEED / PPM);
    }

    public void update() {
        character.setPosition(
            body.getPosition().x * PPM - 16f,
            body.getPosition().y * PPM - 16f
        );
    }

    public void draw(SpriteBatch batch) {
        character.draw(batch);
    }

    public float getX() {
        return character.getX();
    }

    public float getY() {
        return character.getY();
    }

    public float getCenterX() {
        return character.getX() + character.getWidth() / 2f;
    }

    public float getCenterY() {
        return character.getY() + character.getHeight() / 2f;
    }

    public void dispose() {
        texture.dispose();
    }

    public void takeDamage(int damage) {
        if ((currentHP - damage) > 0) {
            currentHP = currentHP - damage;
        }
        // else -> launch #lose menu
    }

    public float getHpPercentage() {
        return (float) currentHP / maxHP;
    }

    public int getCurrentHP() {
        return currentHP;}
}
