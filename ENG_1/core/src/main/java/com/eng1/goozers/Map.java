package com.eng1.goozers;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

/** class for map and static bodies from BOX2D
 */
public class Map {
    private static final float PPM = 32f;

    private final World world;
    private Body longBoiBody;

    private final float statueX = 374f;
    private final float statueY = 95f;

    public Map(World world) {
        this.world = world;
        createLongBoi();
        addStaticBody(200f, 15f, 90f, 18f); /** left part bottom lake */
        addStaticBody(456f, 30f, 180f, 32f); /** right part bottom lake */
        addStaticBody(220f, 110f, 55f, 75f); /** left bottom building  */
        addStaticBody(45f, 253f, 8f, 30f); /** bus  */
        addStaticBody(81f, 256f, 6f, 17f); /** bus stop  */
        addStaticBody(384f, 265f, 58f, 55f); /** top right lecture building */
        addStaticBody(530f, 330f, 15f, 15f); /** top right lake, left rectangle */
        addStaticBody(575f, 315f, 30f, 30f); /** top right lake, right rectangle */


        addStaticBody(300f, 346f, 700f, 1f); /** top map edge */
        addStaticBody(607f, 200f, 1f, 400f); /** right map edge */
        addStaticBody(300f, 0f, 700f, 1f); /** bottom map edge */
        addStaticBody(0f, 200f, 1f, 400f); /** left map edge */
    }

    private void createLongBoi() {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(statueX / PPM, statueY / PPM);

        longBoiBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(10f / PPM, 10f / PPM);
        longBoiBody.createFixture(shape, 0f);
        shape.dispose();
    }

    public Body addStaticBody(float x, float y, float halfWidth, float halfHeight) {
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(x / PPM, y / PPM);

        Body staticBody = world.createBody(bodyDef);
        PolygonShape shape = new PolygonShape();
        shape.setAsBox(halfWidth / PPM, halfHeight / PPM);
        staticBody.createFixture(shape, 0f);
        shape.dispose();

        return staticBody;
    }

    public float getStatueX() {
        return statueX;
    }

    public float getStatueY() {
        return statueY;
    }
}
