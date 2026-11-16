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

/**
 * runs the game and draws the map and draws the player and the ducks on top of
 * the map
 * Lwjgl3Launcher opens the game window
 * Main checks GameEngine to see whether the game is paused
 * UI draws the health bar and inventory slots
 */
public class Main extends ApplicationAdapter {

    /*
     * Box2D measures positions in metres one metre here equals 32 drawing pixels
     * for example: 2 metres = 64 pixels on the map
     */
    private static final float PPM = 32f;
    private static final float TIME_STEP = 1f / 60f;

    private UI ui;
    private final GameEngine gameEngine = new GameEngine();
    private World world;
    private Body longBoiBody;
    private Player player;
    private Texture map;
    private Texture pauseScreen;
    private Box2DDebugRenderer debugRenderer;
    private FitViewport viewport;
    private SpriteBatch batch;
    private BitmapFont font;

    private final float statueX = 227f;
    private final float statueY = 305f;
    private boolean nearDuck = false;
    private boolean inspectedDuck = false;
    private float accumulator = 0f;
    private static final float DEAD_ZONE_W_H = 32f;

    private final ArrayList<NPC> npcs = new ArrayList<>();
    private Texture npcTexture;
    private Texture breadTexture;
    private static final float BREAD_SIZE = 16f;
    private final ArrayList<Vector2> groundBreads = new ArrayList<>();
    private float spawnTimer = 0f;

    /**
     * create() runs at startup it loads the images and creates:
     * - the player, first duck, the collision system, the camera, and the UI
     */
    @Override
    public void create() {
        map = new Texture(Gdx.files.internal("map/test_map2.png"));
        map.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);

        batch = new SpriteBatch();
        font = new BitmapFont();

        Box2D.init();
        world = new World(new Vector2(0, 0), true); // both values 0 = no gravity: top-down game
        player = new Player(world, 100f, 100f);
        createLongBoi();

        npcTexture = new Texture(Gdx.files.internal("map/npcDuckRed.png"));
        npcTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pauseScreen = new Texture(Gdx.files.internal("map/PAUSED.png"));
        pauseScreen.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        breadTexture = new Texture(Gdx.files.internal("ui/bread.png"));
        breadTexture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        spawnNPC();

        debugRenderer = new Box2DDebugRenderer();
        viewport = new FitViewport(320f, 240f);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        viewport.getCamera().position.set(player.getCentreX(), player.getCentreY(), 0);

        viewport.getCamera().update();

        ui = new UI();
        ui.createHealthBar(player);
        ui.addBreadItems(breadTexture);
    }

    /** checks Esc, moves the characters during play and draws the game frame by frame (each frame) */
    @Override
    public void render() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            if (gameEngine.isPaused()) {
                gameEngine.resumeGame();
            } else {
                gameEngine.pauseGame();
            }
        }

        // Select the inventory slot before checking whether to use bread.
        ui.handleInput();
        boolean paused = gameEngine.isPaused();
        if (!paused) {
            if (Gdx.input.isKeyJustPressed(Input.Keys.H)) {
                // press H during play to test losing 10 health -Dev Feature-
                player.takeDamage(10);
            }
            if (Gdx.input.isKeyJustPressed(Input.Keys.F) && ui.useSelectedBread()) {
                dropBread();
            }
            updateGame();
        }

        ScreenUtils.clear(0, 0, 0, 1);
        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        batch.begin();
        batch.draw(map, 0, 0);
        for (Vector2 bread : groundBreads) {
            batch.draw(breadTexture, bread.x - BREAD_SIZE / 2f,
                    bread.y - BREAD_SIZE / 2f, BREAD_SIZE, BREAD_SIZE);
        }
        for (NPC npc : npcs) {
            npc.draw(batch);
        }
        player.draw(batch);

        float left = viewport.getCamera().position.x - viewport.getWorldWidth() / 2f;
        float top = viewport.getCamera().position.y + viewport.getWorldHeight() / 2f;
        if (paused) {
            batch.draw(pauseScreen, left, top - viewport.getWorldHeight(),
                    viewport.getWorldWidth(), viewport.getWorldHeight());
            font.draw(batch, "Paused - Press Esc to resume", left + 10f, top - 30f);
        } else {
            font.draw(batch, "(" + player.getX() + "," + player.getY() + ")", left + 10f, top - 10f);
            // only check E in the method and show the Long Boi message while playing
            drawInteraction();
        }

        batch.end();
        debugRenderer.render(world, viewport.getCamera().combined.cpy().scl(PPM));
        ui.render();
    }

    /**
     * reads movement keys and lets the ducks chase, peck or move away from the player
     * - Box2D does the work to find where everyone ends up, and then the images and camera move to match it
     */
    private void updateGame() {
        float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.25f);
        player.input();
        spawnTimer += delta;

        if (spawnTimer >= 3f && npcs.size() < 20) {
            spawnNPC();
            spawnTimer = 0f;
        }
        for (NPC npc : npcs) {
            npc.chase(player, delta);
        }

        /**
         * limit the time added after a slow frame, then calculate movement in 1/60-second steps
         * - we do this because BOX2D works time in chunks of 1/60 a second so if a frame is slow,
         * - the game will cap how much time is needed to avoid trying to catchup
         */
        accumulator += delta;
        while (accumulator >= TIME_STEP) {
            world.step(TIME_STEP, 6, 2);
            accumulator -= TIME_STEP;
        }

        player.update();

        for (NPC npc : npcs) {
            npc.update();
        }

        updateCamera();
        removeOffscreenBread();

        // remove ducks once they have been visible and then leave the camera view
        // work backwards so removing a duck does not make us skip the next one
        for (int i = npcs.size() - 1; i >= 0; i--) {
            NPC npc = npcs.get(i);
            if (npc.Despawn(viewport)) {
                // remove the collision body as well as the duck from the list
                world.destroyBody(npc.getBody());
                npcs.remove(i);
            }
        }
    }

    private void dropBread() {
        // A dropped loaf appears at the player's feet.
        float breadX = player.getCentreX();
        float breadY = player.getCentreY() - 8f;
        groundBreads.add(new Vector2(breadX, breadY));

        // Ducks that are already eating or fleeing do not switch targets.
        for (NPC npc : npcs) {
            npc.distract(breadX, breadY);
        }
    }

    private void removeOffscreenBread() {
        float cameraX = viewport.getCamera().position.x;
        float cameraY = viewport.getCamera().position.y;
        float halfWidth = viewport.getWorldWidth() / 2f;
        float halfHeight = viewport.getWorldHeight() / 2f;
        float halfBread = BREAD_SIZE / 2f;

        for (int i = groundBreads.size() - 1; i >= 0; i--) {
            Vector2 bread = groundBreads.get(i);
            boolean outside =
                bread.x + halfBread < cameraX - halfWidth ||
                bread.x - halfBread > cameraX + halfWidth ||
                bread.y + halfBread < cameraY - halfHeight ||
                bread.y - halfBread > cameraY + halfHeight;
            if (outside) {
                groundBreads.remove(i);
            }
        }
    }

    /** creates a duck 120 pixels from the player in a random direction the ducks can also appear outside the map */
    private void spawnNPC() {
        float angle = MathUtils.random(0f, MathUtils.PI2);
        float x = player.getCentreX() + MathUtils.cos(angle) * 120f;
        float y = player.getCentreY() - 8f + MathUtils.sin(angle) * 120f;
        NPC npc = new NPC(world, npcTexture, x, y);

        npcs.add(npc);
    }

    /**
     * lets the player move within a small central area before the camera follows
     * - traditional practice in 2D games
     */
    private void updateCamera() {
        float playerX = player.getCentreX();
        float playerY = player.getCentreY();
        float cameraX = viewport.getCamera().position.x;
        float cameraY = viewport.getCamera().position.y;

        if (playerX > cameraX + DEAD_ZONE_W_H / 2f) {
            cameraX = playerX - DEAD_ZONE_W_H / 2f;
        }
        if (playerX < cameraX - DEAD_ZONE_W_H / 2f) {
            cameraX = playerX + DEAD_ZONE_W_H / 2f;
        }
        if (playerY > cameraY + DEAD_ZONE_W_H / 2f) {
            cameraY = playerY - DEAD_ZONE_W_H / 2f;
        }
        if (playerY < cameraY - DEAD_ZONE_W_H / 2f) {
            cameraY = playerY + DEAD_ZONE_W_H / 2f;
        }

        viewport.getCamera().position.set(cameraX, cameraY, 0);
        viewport.getCamera().update();

    }

    /**
     * to avoid the player from walking through the statue since it's a picture from the map,
     * we add an invisible 20-by-20-pixel box that blocks movement through the statue
     */
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

    /** checks whether the player is near Long Boi and presses E, then shows the message */
    private void drawInteraction() {
        float dx = player.getCentreX() - statueX;
        float dy = player.getCentreY() - statueY;
        nearDuck = dx * dx + dy * dy <= 22f * 22f;

        if (nearDuck && Gdx.input.isKeyJustPressed(Input.Keys.E)) {
            inspectedDuck = !inspectedDuck;
        }
        if (nearDuck) {
            String message = inspectedDuck
                    ? "Help me pass my exam Long Boi!"
                    : "Press E to interact";
            font.draw(batch, message, 10, map.getHeight() - 10);
        } else {
            inspectedDuck = false;
        }
    }

    /** adjusts the game view and UI when the window changes size */
    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        ui.resize(width, height);
    }

    /** frees the images, text and physics resources when the game is done using them and closes */
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
        breadTexture.dispose();
    }
}
