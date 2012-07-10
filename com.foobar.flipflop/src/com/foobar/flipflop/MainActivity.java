package com.foobar.flipflop;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.*;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.input.sensor.acceleration.AccelerationData;
import org.andengine.input.sensor.acceleration.IAccelerationListener;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.renderscript.Font;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class MainActivity extends SimpleBaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {

	// ===========================================================
	// Constants
	// ===========================================================
	final FixtureDef ballDef = PhysicsFactory.createFixtureDef(0.5f,0.5f,0.5f);


	// ===========================================================
	// Vars
	// ===========================================================
	private static int CAMERA_WIDTH = 400;
	private static int CAMERA_HEIGHT = 800;
	private Scene mScene;
	private BuildableBitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mCircleTextureRegion;
	private PhysicsWorld mPhysicsWorld;



	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public EngineOptions onCreateEngineOptions() {
		//Toast.makeText(this, "Gesutred", Toast.LENGTH_LONG).show();

		final Camera mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
				new FillResolutionPolicy(), mCamera);
		return engineOptions;
	}

	@Override
	protected void onCreateResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");

		this.mBitmapTextureAtlas = new BuildableBitmapTextureAtlas(this.getTextureManager(), 32, 32, TextureOptions.BILINEAR);
		this.mCircleTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mBitmapTextureAtlas, this, "ball.png");

		try {
			// what the hell is a BlackPawnTextureAtlasBuilder?!
			this.mBitmapTextureAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(0,0,0));
			this.mBitmapTextureAtlas.load();
		} catch (TextureAtlasBuilderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void onResumeGame() {
		super.onResumeGame();
		this.enableAccelerationSensor(this);
	}
	@Override
	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
		// TODO Auto-generated method stub

	}
	@Override
	public void onAccelerationChanged(AccelerationData pAccelerationData) {
		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
		this.mPhysicsWorld.setGravity(gravity);
		Vector2Pool.recycle(gravity);
	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		this.mScene.setBackground(new Background(0,0,0));
		this.mScene.setOnSceneTouchListener(this);
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
		final VertexBufferObjectManager vertexBufferObjectManager = this.getVertexBufferObjectManager(); 


		// falling rectangle
		// final Rectangle rect = new Rectangle(CAMERA_WIDTH/2, 5 , 10, 10, vertexBufferObjectManager);

		//rect.setColor(0,191,255);

		//Create objects
		final Sprite ball = new Sprite(CAMERA_WIDTH/2, 5, 32, 32, this.mCircleTextureRegion, vertexBufferObjectManager);
		final Rectangle ground = new Rectangle(0,CAMERA_HEIGHT - 1, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 1, 0, 2, CAMERA_HEIGHT, vertexBufferObjectManager);

		//Create Bodies
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody,ballDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody,ballDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody,ballDef);
		final Body ballBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, ball, BodyType.DynamicBody, ballDef);
		addTarget();
		//this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rect,rectBody,true,true));
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, ballBody, true, true));

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);
		/* The actual collision-checking. */
		this.mScene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void reset() { }

			@Override
			public void onUpdate(final float pSecondsElapsed) {
				if(ball.collidesWith(target)){
					left.setColor(0, 1, 0);
				}
				if(ball.collidesWith(ground)) {
					ground.setColor(1, 0, 0);
				}                              
			}
		});

		//Attach objs to scene
		this.mScene.attachChild(ground);
		this.mScene.attachChild(ball);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);
		this.mScene.attachChild(target);

		return this.mScene;
	}


	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {

		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {

				addPlank(pSceneTouchEvent.getX(),pSceneTouchEvent.getY());
				/*
				final FixtureDef ballDef = PhysicsFactory.createFixtureDef(0.5f,0.5f,0.5f);

				final Rectangle rect = new Rectangle(x, y, 10, 10, this.getVertexBufferObjectManager());
				final Body rectBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.DynamicBody, ballDef);

				this.mScene.attachChild(rect);
				this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(rect,rectBody,true,true));
				 */
				//return true;
			}
		}
		return false;
	}


	// ===========================================================
	// Methods 
	// ===========================================================
	Sprite target; 
	Body ballBody; 
	public void addTarget(){
		target = new Sprite(0, 250, 32, 32, this.mCircleTextureRegion, this.getVertexBufferObjectManager());
		ballBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld, target, BodyType.StaticBody, ballDef);
	}

	final FixtureDef plankDef = PhysicsFactory.createFixtureDef(0.5f,0.5f,0.5f);

	public void addPlank(final float startX, final float startY){
		Rectangle plank = new Rectangle(startX, startY,2, 200,this.getVertexBufferObjectManager());
		//Rectangle plan2 = new Rectangle()
		final Body plankBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, plank, BodyType.StaticBody, plankDef);
		this.mScene.attachChild(plank);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(plank,plankBody,false,false));
	}

}
