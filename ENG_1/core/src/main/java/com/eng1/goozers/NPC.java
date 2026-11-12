package com.eng1.goozers;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.*;

public class NPC {

    private static final float PPM = 32f;
    private static final float SPEED = 65f;
    private final Sprite sprite;
    private final Body body;

    public NPC(World world, Texture texture, float x, float y) {

        sprite = new Sprite(texture);
        sprite.setSize(16, 16);
        //sprite.setColor(1f, 0.5f, 0.5f, 1f);
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

    public void chase(float targetX, float targetY) {

        float dx = targetX / PPM - body.getPosition().x;
        float dy = targetY / PPM - body.getPosition().y;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < 8f / PPM) {body.setLinearVelocity(0, 0); return;}
        body.setLinearVelocity(dx / length * SPEED / PPM, dy / length * SPEED / PPM);
    }

    public void update() {
        sprite.setPosition(body.getPosition().x * PPM - 8f, body.getPosition().y * PPM - 8f);
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}
