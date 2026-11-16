package com.eng1.goozers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;

/** One duck: chase, peck, follow bread, eat, and flee. */
public class NPC {

    private static final float PPM = 32f;
    private static final float SPEED = 40f;       // pixels per second while chasing bread/player
    private static final float RUN_SPEED = 10f;   // pixels per second when fleeing
    private static final float PECK_DISTANCE = 12f;
    private static final float BREAD_DISTANCE = 12f;
    private static final float EATING_DURATION = 2f;

    private final Sprite sprite;
    private final Body body;

    private enum State {
        CHASING, DISTRACTED, EATING, FLEEING
    }

    private State state = State.CHASING;
    private final Vector2 breadTarget = new Vector2();
    private final Vector2 fleeDirection = new Vector2();
    private float eatingTimer = 0f;
    private boolean hasBeenVisible = false;

    public NPC(World world, Texture texture, float x, float y) {
        sprite = new Sprite(texture);
        sprite.setSize(16f, 16f);

        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        def.position.set(x / PPM, y / PPM);
        def.fixedRotation = true;

        body = world.createBody(def);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(3f / PPM, 3f / PPM);
        body.createFixture(shape, 1f);
        shape.dispose();

        update();
    }

    /** Only ducks that are chasing can be redirected by a newly dropped loaf. */
    public void distract(float breadX, float breadY) {
        if (state != State.CHASING) return;

        breadTarget.set(breadX / PPM, breadY / PPM);
        state = State.DISTRACTED;
    }

    /** Called by Main once per unpaused frame. delta is measured in seconds. */
    public void chase(Player player, float delta) {
        if (state == State.FLEEING) {
            body.setLinearVelocity(
                fleeDirection.x * RUN_SPEED / PPM,
                fleeDirection.y * RUN_SPEED / PPM
            );
            return;
        }

        if (state == State.EATING) {
            body.setLinearVelocity(0f, 0f);
            eatingTimer -= delta;
            if (eatingTimer <= 0f) {
                startFleeing(player);
            }
            return;
        }

        if (state == State.DISTRACTED) {
            if (body.getPosition().dst(breadTarget) <= BREAD_DISTANCE / PPM) {
                state = State.EATING;
                eatingTimer = EATING_DURATION;
                body.setLinearVelocity(0f, 0f);
            } else {
                moveTowards(breadTarget.x, breadTarget.y);
            }
            return;
        }

        // The player sprite is centred on their Box2D body after Player.update().
        float targetX = player.getCentreX() / PPM;
        float targetY = player.getCentreY() / PPM;

        if (body.getPosition().dst(targetX, targetY) <= PECK_DISTANCE / PPM) {
            peck(player);
            return;
        }

        moveTowards(targetX, targetY);
    }

    private void moveTowards(float targetX, float targetY) {
        float dx = targetX - body.getPosition().x;
        float dy = targetY - body.getPosition().y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length > 0.0001f) {
            body.setLinearVelocity(
                dx / length * SPEED / PPM,
                dy / length * SPEED / PPM
            );
        } else {
            body.setLinearVelocity(0f, 0f);
        }
    }

    private void peck(Player player) {
        player.takeDamage(10);
        startFleeing(player);
    }

    private void startFleeing(Player player) {
        state = State.FLEEING;
        fleeDirection.set(
            body.getPosition().x - player.getCentreX() / PPM,
            body.getPosition().y - player.getCentreY() / PPM
        );

        if (fleeDirection.isZero(0.001f)) {
            fleeDirection.set(1f, 0f);
        }
        fleeDirection.nor();

        sprite.setOriginCenter();
        sprite.setRotation(fleeDirection.angleDeg());
        body.setLinearVelocity(
            fleeDirection.x * RUN_SPEED / PPM,
            fleeDirection.y * RUN_SPEED / PPM
        );
    }

    public void update() {
        sprite.setPosition(
            body.getPosition().x * PPM - 8f,
            body.getPosition().y * PPM - 8f
        );
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }

    /** Remove a duck only after it has actually appeared in view. */
    public boolean Despawn(FitViewport viewport) {
        float cameraX = viewport.getCamera().position.x;
        float cameraY = viewport.getCamera().position.y;
        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;

        float spriteCenterX = sprite.getX() + sprite.getWidth() / 2f;
        float spriteCenterY = sprite.getY() + sprite.getHeight() / 2f;
        // Half the diagonal covers the corners of a rotated 16x16 duck sprite.
        float radius = (float) Math.sqrt(
            sprite.getWidth() * sprite.getWidth() +
            sprite.getHeight() * sprite.getHeight()
        ) / 2f;

        boolean outside =
            spriteCenterX + radius < cameraX - halfWidth ||
            spriteCenterX - radius > cameraX + halfWidth ||
            spriteCenterY + radius < cameraY - halfHeight ||
            spriteCenterY - radius > cameraY + halfHeight;

        if (!outside) hasBeenVisible = true;
        return hasBeenVisible && outside;
    }

    public Body getBody() {
        return body;
    }
}
