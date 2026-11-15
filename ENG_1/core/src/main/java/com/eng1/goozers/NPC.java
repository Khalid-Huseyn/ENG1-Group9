package com.eng1.goozers;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;

/**
 * controls a duck that chases the player, pecks once and then moves away
 * Main updates and draws each duck and removes fleeing ducks outside the camera view
 */
public class NPC {

    // Box2D measures positions in metres one metre here equals 32 drawing pixels
    private static final float PPM = 32f;
    private static final float SPEED = 65f;
    private final Sprite sprite;
    private final Body body;
    private static final float runSpeed = 10f;
    private static final float peckDist  = 8f;
    private boolean fleeing = false;
    private final Vector2 fleeDirection = new Vector2();

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
     * moves towards the player until close enough to peck
     * ducks that have already pecked keep moving away instead of chasing again
     */
    public void chase(Player player) {

        if (fleeing) {
            body.setLinearVelocity(fleeDirection.x * runSpeed / PPM, fleeDirection.y * runSpeed / PPM);
            return;
        }

        float distanceToPlayer = (float) Math.sqrt(
            Math.pow(player.getCentreX() / PPM - body.getPosition().x, 2) + Math.pow((player.getCentreY() - 8f) / PPM - body.getPosition().y, 2));

        if (distanceToPlayer <= peckDist / PPM) {
            peck(player);
            return;
        }

        body.setLinearVelocity(player.getCentreX() / PPM - body.getPosition().x, (player.getCentreY() - 8f) / PPM - body.getPosition().y);
    }

    /** moves the duck image to match the position calculated by Box2D */
    public void update() {
        sprite.setPosition(body.getPosition().x * PPM - 8f, body.getPosition().y * PPM - 8f);
    }

    /** Main calls this to draw the duck at its updated position */
    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    /** asks Player to take 10 damage and turns the duck to move away at 10 pixels per second */
    private void peck(Player player){
        player.takeDamage(10);
        fleeing = true;
        fleeDirection.set(body.getPosition().x - player.getCentreX() / PPM, body.getPosition().y - (player.getCentreY() - 8f) / PPM);
        fleeDirection.nor();

        sprite.setOriginCenter();
        sprite.setRotation(fleeDirection.angleDeg());
        body.setLinearVelocity(fleeDirection.x * runSpeed / PPM, fleeDirection.y * runSpeed / PPM);
    }

    /** tells Main whether a fleeing duck is fully outside the camera view and can be removed */
    public boolean Despawn(FitViewport viewport) {
        if (!fleeing) {
            return false;
        }

        float cameraX = viewport.getCamera().position.x;
        float cameraY = viewport.getCamera().position.y;
        float camCentreX = viewport.getWorldWidth() / 2f;
        float camCentreY = viewport.getWorldHeight() / 2f;

        return (
            sprite.getX() + sprite.getWidth() < cameraX-camCentreX ||
            sprite.getX() > cameraX+camCentreX ||
            sprite.getY() + sprite.getHeight() < cameraY-camCentreY ||
            sprite.getY() > cameraY + camCentreY);
    }

    /** gives Main the duck collision body so it can remove it from the physics world */
    public Body getBody() {
        return body;
    }
}
