package com.eng1.goozers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

/**
 * controls a duck that chases the player
 * Main gives each duck its image and tells it where to move
 * it calculates a target near the player and moves there
 */
public class NPC {

    // Box2D measures positions in metres one metre here equals 32 drawing pixels
    private static final float PPM = 32f;
    private static final float SPEED = 65f;
    private final Sprite sprite;
    private final Body body;

    /** creates a duck and its collision box using the image and position supplied by Main */
    public NPC(World world, Texture texture, float x, float y) {

        sprite = new Sprite(texture);
        sprite.setSize(16, 16);
        // sprite.setColor(1f, 0.5f, 0.5f, 1f) gives the duck a tint colour
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x / PPM, y / PPM);
        def.fixedRotation = true;

        body = world.createBody(def);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(2f / PPM, 2f / PPM);
        body.createFixture(shape, 1f);
        shape.dispose();

        update();
    }

    /**
     * moves towards the target and stops when it is less than eight pixels away
     * it stops by setting the method speed to zero
     */
    public void chase(float targetX, float targetY) {

        float dx = targetX / PPM - body.getPosition().x;
        float dy = targetY / PPM - body.getPosition().y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < 8f / PPM) {
            body.setLinearVelocity(0, 0);
            return;
        }
        body.setLinearVelocity(dx / length * SPEED / PPM, dy / length * SPEED / PPM);
    }

    /**
     * moves the duck image to match the position calculated by Box2D
     * puts the centre of the duck image at that position so you can see it move
     */
    public void update() {
        sprite.setPosition(body.getPosition().x * PPM - 8f, body.getPosition().y * PPM - 8f);
    }

    /** Main calls draw() for each duck and puts its image on screen at the position set by update() */
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
