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

/**
 * handles the player movement, draws character image and tracks health
 *
 * Main tells the player when to move and draw -
 * - uses input() to read WASD uses update() after the physics runs,
 * - and draw() to show the player even when game is paused
 *
 * UI reads the player health to draw the health bar
 * - health value lives in Player UI reads it works,
 * - and works out how mcuh of the health bar to fill
 */
public class Player {

    // Box2D measures positions in metres one metre here equals 32 drawing pixels
    private static final float PPM = 32f;
    private static final float SPEED = 50f;

    private final Texture texture;
    private final Sprite character;
    private final Body body;
    private final int maxHP = 100;
    private int currentHP = maxHP;

    /**
     * loads the player image and adds a small collision box near the feet
     * - allows the player to collide with objects
     */
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
        shape.setAsBox(4f / PPM, 3f / PPM); // 8 x 6 pixel hitbox at the feet,the small size allows upper body to overlap scenery
        body.createFixture(shape, 1f);
        shape.dispose();
    }

    /**
     * reads WASD to choose the movement direction
     * main calls this while the game is running
     */
    public void input() {
        float dx = 0f;
        float dy = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) dy++;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) dy--;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) dx++;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) dx--;

        /**
         * keep diagonal movement at the same speed as moving straight
         * - otherwise holding W+D would move faster than holding W
         */

        float length = (float) Math.sqrt(dx * dx + dy * dy);
        if (length > 0f) {
            dx /= length;
            dy /= length;
        }

        body.setLinearVelocity(dx * SPEED / PPM, dy * SPEED / PPM);
    }

    /**
     * moves the player image to match the position calculated by Box2D
     * - update() puts the character image to that position
     */
    public void update() {
        character.setPosition(
            body.getPosition().x * PPM - 16f,
            body.getPosition().y * PPM - 16f
        );
    }

    /**
     * draws the player image as part of the scene drawn by Main
     * - set by update()
     */
    public void draw(SpriteBatch batch) {
        character.draw(batch);
    }

    /** returns the left edge of the player image in pixels */
    public float getX() {
        return character.getX();
    }

    /** returns the bottom edge of the player image in pixels */
    public float getY() {
        return character.getY();
    }

    /** returns the middle of the player image from left to right for the ducks and camera */
    public float getCentreX() {
        return character.getX() + character.getWidth() / 2f;
    }

    /** returns the middle of the player image from bottom to top for the ducks and camera */
    public float getCentreY() {
        return character.getY() + character.getHeight() / 2f;
    }

    /**
     * frees the player image from memory
     * Main clears the Box2D world seperately which also cleans up the player's physic body
     */
    public void dispose() {
        texture.dispose();
    }

    /**
     * reduces health without letting it fall below zero
     * negative damage is ignored
     */
    public void takeDamage(int damage) {
        currentHP = Math.max(0, currentHP - Math.max(0, damage));
        if (currentHP == 0) {
            // TODO: launch lose menu
        }
    }

    /** tells UI how full the health bar should be, such as 0.5 for half full */
    public float getHpPercentage() {
        return (float) currentHP / maxHP;
    }

    /** returns how much health the player has left */
    public int getCurrentHP() {
        return currentHP;}
}
