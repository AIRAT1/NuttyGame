package de.android.ayrathairullin.nuttygame.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.android.ayrathairullin.nuttygame.NuttyGame;

public class GameScreen extends ScreenAdapter{
    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;
    private static final float UNITS_PER_METER = 16;

    private World world;
    private Box2DDebugRenderer dDebugRenderer;
    private Body body;

    private Camera camera;
    private Viewport viewport;
    private ShapeRenderer shapeRenderer;

    private final NuttyGame nuttyGame;

    public GameScreen(NuttyGame nuttyGame) {
        this.nuttyGame = nuttyGame;
    }

    @Override
    public void show() {
        world = new World(new Vector2(0, - 10), true);
        dDebugRenderer = new Box2DDebugRenderer();
        body = createBody();
        body.setTransform(100, 120, 0);
        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);
        shapeRenderer = new ShapeRenderer();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        drawDebug();
    }

    private Body createBody() {
        BodyDef def = new BodyDef();
        def.type = BodyDef.BodyType.DynamicBody;
        Body box = world.createBody(def);
        PolygonShape poly = new PolygonShape();
        poly.setAsBox(60 / UNITS_PER_METER, 60 / UNITS_PER_METER);
        box.createFixture(poly, 1);
        poly.dispose();
        return box;
    }

    private void update(float delta) {
        world.step(delta, 6, 2);
        body.setAwake(true);
    }

    private void clearScreen() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void drawDebug() {
        dDebugRenderer.render(world, camera.combined);
    }
}
