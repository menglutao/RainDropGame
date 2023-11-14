package com.badlogic.drop;

import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;


public class Drop extends ApplicationAdapter {
	private Array<Item> items;
	private Texture[] itemTextures;

	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private Music birthdayMusic;

	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Animation<TextureRegion> animation;
	private Animation<TextureRegion> endingAnimation;
	private float elapsed;

	private int score;
	private boolean isGameOver;
	private Texture gameOverTexture;

	private Rectangle bucket;
	private Array<Rectangle> raindrops;
	private long lastDropTime;

	private BitmapFont font;
	private float fadeOutTimer = 0.0f;
	private final float fadeOutDuration = 1.0f; // 1 second fade out

	private final float transitionDelay = 3.0f; // 1 to 2 seconds delay
	private float transitionTimer = 0.0f;



	private final int rainDropWidth = 65;
	private final int rainDropHeight = 65;
	private final int bucketWidth = 120;
	private final int bucketHeight = 120;




	private void spawnRaindrop() {
		float x = MathUtils.random(0, 800 - rainDropWidth);
		Texture texture = itemTextures[MathUtils.random(itemTextures.length - 1)];
		items.add(new Item(texture, x, 480, rainDropWidth, rainDropHeight, 100));
		// Record the spawn time for this item
		lastDropTime = TimeUtils.nanoTime();
	}



	@Override
	public void create () {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);
		batch = new SpriteBatch();


		// load the image for the drop and the bucket, 64 x 64 pixels each
//		dropImage = new Texture(Gdx.files.internal("gold.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));
		animation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("wave.gif").read());
		endingAnimation = GifDecoder.loadGIFAnimation(Animation.PlayMode.LOOP, Gdx.files.internal("happy_birthday.gif").read());
		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("click.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("merryMusic.mp3"));
		birthdayMusic = Gdx.audio.newMusic(Gdx.files.internal("birthday_music.wav"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();
		birthdayMusic.setLooping(true);
		birthdayMusic.pause();

		bucket = new Rectangle();
		bucket.x = 800/2 - bucketWidth/2;
		bucket.y = 10;
		bucket.width = bucketWidth;
		bucket.height = bucketHeight;

//		raindrops = new Array<Rectangle>();

		itemTextures = new Texture[] {
				new Texture(Gdx.files.internal("peach.png")),
				new Texture(Gdx.files.internal("cake.png")),
				new Texture(Gdx.files.internal("money.png")),
				new Texture(Gdx.files.internal("fu.png"))
		};

		items = new Array<Item>();
		spawnRaindrop();

		score = 0;
		isGameOver = false;
		font = new BitmapFont(); // Uses default Arial font
		camera.update();

	}

	@Override
	public void render () {
		ScreenUtils.clear(135 / 255f, 206 / 255f, 235 / 255f, 1f); // deep ocean blue
//		Gdx.gl.glClearColor(21/255.0f, 151/255.0f, 187/255.0f, 1);// Convert the hex color to RGB and then divide by 255 to fit the range 0-1 for glClearColor
//		Gdx.gl.glClearColor(135 / 255f, 206 / 255f, 235 / 255f, 1f); // Sky blue color
		camera.update();

		if (!isGameOver) {
			batch.setProjectionMatrix(camera.combined);
			elapsed += Gdx.graphics.getDeltaTime();
			//		Gdx.gl.glClearColor(1, 0, 0, 0);
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
			batch.begin();
			for (Iterator<Item> iter = items.iterator(); iter.hasNext(); ) {
				Item item = iter.next();
				item.update(Gdx.graphics.getDeltaTime());

				if (item.bounds.y + rainDropHeight < 0) iter.remove();
				if (item.bounds.overlaps(bucket)) {
					dropSound.play();
					iter.remove();
					score += 10;
					if (score >= 200) {
						isGameOver = true;
						break;
					}
				}
			}


			batch.draw(bucketImage, bucket.x, bucket.y, bucketWidth, bucketHeight);
			batch.draw(animation.getKeyFrame(elapsed), 0.0f, 0.0f, 800, 200);
			if (Gdx.input.isTouched()) {
				Vector3 touchPos = new Vector3();
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos);
				bucket.x = touchPos.x - bucketWidth / 2;
			}
			if (Gdx.input.isKeyPressed(Keys.LEFT)) bucket.x -= 200 * Gdx.graphics.getDeltaTime();
			if (Gdx.input.isKeyPressed(Keys.RIGHT)) bucket.x += 200 * Gdx.graphics.getDeltaTime();
			if (bucket.x < 0) bucket.x = 0;
			if (bucket.x > 800 - bucketWidth) bucket.x = 800 - bucketWidth;
			if (TimeUtils.nanoTime() - lastDropTime > 600000000) spawnRaindrop();

			batch.draw(bucketImage, bucket.x, bucket.y, bucketWidth, bucketHeight);
			for (Item item : items) {
				item.draw(batch);
			}
			font.draw(batch, "Score: " + score, 10, 450);
			batch.end();
		} else {
			// When the game is over, start the fade out
			fadeOutTimer += Gdx.graphics.getDeltaTime();
			float alpha = MathUtils.clamp(fadeOutTimer / fadeOutDuration, 0, 1);

//			ScreenUtils.clear(0.1f, 0.2f, 0.7f, 1f); // deep ocean blue

			batch.setProjectionMatrix(camera.combined);
			batch.begin();
			batch.setColor(1, 1, 1, 1 - alpha); // fade out game elements

			batch.setColor(1, 1, 1, 1); // reset the batch color to opaque
			batch.end();

			if (fadeOutTimer >= fadeOutDuration) {
				transitionTimer += Gdx.graphics.getDeltaTime();
				if (transitionTimer >= transitionDelay) {
					// Stop the game music and start the birthday music
					if (!birthdayMusic.isPlaying()) {
						rainMusic.stop();
						birthdayMusic.play();
					}

				batch.begin();
				batch.setColor(1, 1, 1, alpha); // fade in birthday message
				batch.draw(endingAnimation.getKeyFrame(elapsed), 0.0f, 0.0f, 800, 400);
				batch.setColor(1, 1, 1, 1); // reset the batch color to opaque
				batch.end();

			}

			batch.begin();
			// Then draw the score once
			font.draw(batch, "You Win!!!!", 100, 270);
			rainMusic.stop();
			birthdayMusic.play();


			elapsed += Gdx.graphics.getDeltaTime();
			Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

			batch.draw(endingAnimation.getKeyFrame(elapsed), 0.0f, 0.0f, 800, 400);
//			batch.draw(gameOverTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
			batch.end();

		}
	}}

	@Override
	public void dispose() {
		for (Texture texture : itemTextures) {
			if (texture != null) texture.dispose();
		}
		if (gameOverTexture != null) gameOverTexture.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		font.dispose();
		batch.dispose();
	}


}
