package com.eng1.goozers;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main extends ApplicationAdapter {

    private static final float PPM = 32f;
    private static final float TIME_STEP = 1f / 60f;

    private UI ui;
    private World world;
    private Body longBoiBody;
    private Player player;
    private Texture map;
    private Texture pauseScreen;
    private Box2DDebugRenderer debugRenderer;
    private FitViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;
    private boolean paused = false;

    private final float statueX = 227f;
    private final float statueY = 305f;
    private boolean nearDuck = false;
    private boolean inspectedDuck = false;
    private float accumulator = 0f;
    private static final float DEAD_ZONE_W_H = 32f;

    private final ArrayList<NPC> npcs = new ArrayList<>();
    private Texture npcTexture;
    private float spawnTimer = 0f;

    @Override
    public void create() {
        map = new Texture(Gdx.files.internal("map/test_map2.png"));
        map.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        batch = new SpriteBatch();
        font = new BitmapFont();

        Box2D.init();
        world = new World(new Vector2(0, 0), true); // No gravity: top-down game
        player = new Player(world, 100f, 100f);
        createLongBoi();

        npcTexture = new Texture(Gdx.files.internal("map/npcDuckRed.png"));
        npcTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pauseScreen = new Texture(Gdx.files.internal("map/PAUSED.png"));
        pauseScreen.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spawnNPC();

        debugRenderer = new Box2DDebugRenderer();
        viewport = new FitViewport(320f, 240f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        viewport.getCamera().position.set(player.getCenterX(), player.getCenterY(), 0);

        ui = new UI();
        ui.createHealthBar(player);
    }

    @Override
    public void render() {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            paused = !paused;}

        if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
            player.takeDamage(10);}

        if (!paused) {
            player.input();
            spawnTimer += Math.min(Gdx.graphics.getDeltaTime(), 0.25f);

            if (spawnTimer >= 3f && npcs.size() < 10) {spawnNPC();spawnTimer = 0f;}
            for (NPC npc : npcs) {npc.chase(player.getCenterX(), player.getCenterY() - 8f);}

            accumulator += Math.min(Gdx.graphics.getDeltaTime(), 0.25f);
            while (accumulator >= 1f / 60f) {world.step(1f / 60f, 6, 2);accumulator -= 1f / 60f;}

            player.update();

            for (NPC npc : npcs) {npc.update();}

            updateCamera();
            batch.setProjectionMatrix(viewport.getCamera().combined);}


        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.begin();
        batch.draw(map, 0, 0);
        for (NPC npc : npcs) {npc.draw(batch);}
        player.draw(batch);
        font.draw(batch, "(" + player.getX() + "," + player.getY() + ")",
            viewport.getCamera().position.x - viewport.getWorldWidth() / 2f + 10f,
            viewport.getCamera().position.y + viewport.getWorldHeight() / 2f - 10f);

        if (paused) {batch.draw(pauseScreen,
                viewport.getCamera().position.x - viewport.getWorldWidth() / 2f,
                viewport.getCamera().position.y - viewport.getWorldHeight() / 2f,
                viewport.getWorldWidth(),
                viewport.getWorldHeight());}
        if (!paused) {drawInteraction();}

        batch.end();
        debugRenderer.render(world, viewport.getCamera().combined.cpy().scl(PPM));
        ui.render();}

    private void spawnNPC() {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float x = player.getCenterX() + MathUtils.cos(angle) * 120f;
        float y = player.getCenterY() - 8f + MathUtils.sin(angle) * 120f;
        NPC npc = new NPC(world, npcTexture, x, y);

        npcs.add(npc);
    }

    private void updateCamera() {
        float playerX = player.getCenterX();
        float playerY = player.getCenterY();
        float cameraX = viewport.getCamera().position.x;
        float cameraY = viewport.getCamera().position.y;


        if (playerX > cameraX + DEAD_ZONE_W_H / 2f) {
            cameraX = playerX - DEAD_ZONE_W_H / 2f;}
        if (playerX < cameraX - DEAD_ZONE_W_H / 2f) {
            cameraX = playerX + DEAD_ZONE_W_H / 2f;}
        if (playerY > cameraY + DEAD_ZONE_W_H / 2f) {
            cameraY = playerY - DEAD_ZONE_W_H / 2f;}
        if (playerY < cameraY - DEAD_ZONE_W_H / 2f) {
            cameraY = playerY + DEAD_ZONE_W_H / 2f;}

        viewport.getCamera().position.set(cameraX, cameraY, 0);

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

    private void drawInteraction() {
        float dx = player.getCenterX() - statueX;
        float dy = player.getCenterY() - statueY;
        nearDuck = dx * dx + dy * dy <= 22f * 22f;

        if (nearDuck && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            inspectedDuck = !inspectedDuck;}
        if (nearDuck) {
            String message = inspectedDuck
                ? "Help me pass my exam Long Boi!"
                : "Press E to interact";
            font.draw(batch, message, 10, map.getHeight() - 10);}
        else {
            inspectedDuck = false;}
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        ui.resize(width, height);}

    @Override
    public void dispose() {
        map.dispose();
        player.dispose();
        font.dispose();
        batch.dispose();
        debugRenderer.dispose();
        world.dispose();
        npcTexture.dispose();
        ui.dispose();
        pauseScreen.dispose();
    }
}
