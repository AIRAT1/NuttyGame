package de.android.ayrathairullin.nuttygame;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.physics.box2d.Box2D;

import de.android.ayrathairullin.nuttygame.screens.GameScreen;

public class NuttyGame extends Game {
	private final AssetManager assetManager = new AssetManager();
	
	@Override
	public void create () {
		Box2D.init();
		setScreen(new GameScreen(this));
	}

	public AssetManager getAssetManager() {
		return assetManager;
	}
}
