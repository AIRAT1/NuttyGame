package de.android.ayrathairullin.nuttygame.screens;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.android.ayrathairullin.nuttygame.NuttyGame;

public class GameScreen extends ScreenAdapter{
    private static final float WORLD_WIDTH = 960;
    private static final float WORLD_HEIGHT = 544;

    private static final float UNITS_PER_METER = 32;
    private static final float UNIT_WIDTH = WORLD_WIDTH / UNITS_PER_METER;
    private static final float UNIT_HEIGHT = WORLD_HEIGHT / UNITS_PER_METER;

    private static final float MAX_STRENGTH = 15;
    private static final float MAX_DISTANCE = 100;
    private static final float UPPER_ANGLE = 3 * MathUtils.PI / 2f;
    private static final float LOWER_ANGLE = MathUtils.PI / 2f;

    private static final Vector2 anchor = new Vector2(convertMetresToUnits(6.125f), convertMetresToUnits(5.75f));
    private static final Vector2 firingPosition = anchor.cpy();

    private final NuttyGame nuttyGame;

    private ShapeRenderer shapeRenderer;
    private Viewport viewport;
    private OrthographicCamera camera;

    private SpriteBatch batch;

    private World world;
    private OrthographicCamera box2dCam;
    private Box2DDebugRenderer debugRenderer;

    private ObjectMap<Body, Sprite> sprites = new ObjectMap<Body, Sprite>();
    private Array<Body> toRemove = new Array<Body>();

    private TiledMap tiledMap;
    private OrthogonalTiledMapRenderer orthogonalTiledMapRenderer;

    private float distance, angle;

    private Sprite slingshot, squirrel, staticAcorn;

    public GameScreen(NuttyGame nuttyGame) {
        this.nuttyGame = nuttyGame;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
    }

    @Override
    public void show() {
        world = new World(new Vector2(0, - 9.81f), true);
        debugRenderer = new Box2DDebugRenderer();
        box2dCam = new OrthographicCamera(UNIT_WIDTH, UNIT_HEIGHT);

        camera = new OrthographicCamera();
        viewport = new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        viewport.apply(true);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();

        tiledMap = nuttyGame.getAssetManager().get("nuttybirds.tmx");
        orthogonalTiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap, batch);
        orthogonalTiledMapRenderer.setView(camera);

        TiledObjectBodyBuilder.buildFloorBodies(tiledMap, world);
        TiledObjectBodyBuilder.buildBuildingBodies(tiledMap, world);
        TiledObjectBodyBuilder.buildBirdBodies(tiledMap, world);

        Gdx.input.setInputProcessor(new InputAdapter(){
            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                calculateAngleAndDistanceForBullet(screenX, screenY);
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                createBullet();
                firingPosition.set(anchor.cpy());
                return true;
            }
        });
        world.setContactListener(new NuttyContactListener());

        Array<Body> bodyes = new Array<Body>();
        world.getBodies(bodyes);
        for (Body body : bodyes) {
            Sprite sprite = SpriteGenerator.generateSpriteForBody(nuttyGame.getAssetManager(), body);
            if (sprite != null) sprites.put(body, sprite);
        }
        slingshot = new Sprite(nuttyGame.getAssetManager().get("slingshot.png", Texture.class));
        slingshot.setPosition(170, 64);
        squirrel = new Sprite(nuttyGame.getAssetManager().get("squirrel.png", Texture.class));
        squirrel.setPosition(32, 64);
        staticAcorn = new Sprite(nuttyGame.getAssetManager().get("acorn.png", Texture.class));
    }

//        private Body createBody() {
//        BodyDef def = new BodyDef();
//        def.type = BodyDef.BodyType.DynamicBody;
//        Body box = world.createBody(def);
//        PolygonShape poly = new PolygonShape();
//        poly.setAsBox(60 / UNITS_PER_METER, 60 / UNITS_PER_METER);
//        box.createFixture(poly, 1);
//        poly.dispose();
//        return box;
//        }

    @Override
    public void render(float delta) {
        update(delta);
        clearScreen();
        draw();
        drawDebug();
    }


    private void update(float delta) {
        clearDeadBodies();
        world.step(delta, 6, 2);
        box2dCam.position.set(UNIT_WIDTH / 2, UNIT_HEIGHT / 2, 0);
        box2dCam.update();
        updateSpritePosition();
    }

    private void clearDeadBodies() {
        for (Body body : toRemove) {
            sprites.remove(body);
            world.destroyBody(body);
        }
        toRemove.clear();
    }

    private void updateSpritePosition() {
        for (Body body : sprites.keys()) {
            Sprite sprite = sprites.get(body);
            sprite.setPosition(
                    convertMetresToUnits(body.getPosition().x) - sprite.getWidth() / 2,
                    convertMetresToUnits(body.getPosition().y) - sprite.getHeight() / 2);
            sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
        }
        staticAcorn.setPosition(firingPosition.x - staticAcorn.getWidth() / 2,
                firingPosition.y - staticAcorn.getHeight() / 2);
    }

    private void calculateAngleAndDistanceForBullet(int screenX, int screenY) {
        firingPosition.set(screenX, screenY);
        viewport.unproject(firingPosition);
        distance = distanceBetweenTwoPoints();
        angle = angleBetweenTwoPoints();

        if (distance > MAX_DISTANCE) {
            distance = MAX_DISTANCE;
        }
        if (angle > LOWER_ANGLE) {
            if (angle > UPPER_ANGLE) {
                angle = 0;
            }else {
                angle = LOWER_ANGLE;
            }
        }
        firingPosition.set(anchor.x + (distance * -MathUtils.cos(angle)), anchor.y + (distance * -MathUtils.sin(angle)));
    }

    public void drawDebug() {
        debugRenderer.render(world, camera.combined);
    }

}
