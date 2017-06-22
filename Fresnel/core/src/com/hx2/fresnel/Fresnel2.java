package com.hx2.fresnel;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class Fresnel2 extends ApplicationAdapter implements InputProcessor {
	public class Point{
		public float vel;
		public float h;
	}
	private static final int mag = 1;
	private static final Vector2 XY = Vector2.X.cpy().add(Vector2.Y).nor();

	SpriteBatch batch;
	Texture img;
	Texture pixmapTexture;
	Pixmap pixmap;
	Point[] points;
	Point[] npoints;
	long msDelay = 16, lastUp;
	int n = 500;
	int it = 0;
	float sqr2 = (float) Math.sqrt(2);
	private boolean showVector = true;
	private boolean showWaves = true;
	private int itmult = 1000;
	private float ondefreq = 1f/240f;

	private int nbExcitation = 8;

	@Override
	public void create () {
		batch = new SpriteBatch();
		pixmap = new Pixmap( n * mag, n * mag, Format.RGB888);
		pixmapTexture = new Texture(pixmap, Pixmap.Format.RGB888, false);
		points = new Point[n];
		initPoints();
		lastUp = System.currentTimeMillis();
		Gdx.input.setInputProcessor(this);
	}

	public void initPoints(){
		npoints = new Point[n];
		points = new Point[n];
		for(int i = 0; i < n; i++){
			points[i] = new Point();
			points[i].vel = 0f;
			points[i].h = 0f;

			npoints[i] = new Point();
			npoints[i].vel = 0f;
			npoints[i].h = 0f;

		}
		it = 0;
	}
	public void update(){

		for(int i = 0; i < n; i++){
			npoints[i].vel = points[i].vel;
			npoints[i].h = points[i].h + points[i].vel/1000f;

			npoints[i].vel -= points[i].vel*0.001f/1000f;
			if(i >= 1)
				npoints[i].vel += (points[i-1].h-points[i].h)/1000f;
			if(i < n-1)
				npoints[i].vel += (points[i+1].h-points[i].h)/1000f;
			if(i >= 0.9f*n){

				npoints[i].vel -= points[i].vel*0.00010f*((i - 0.9f*n)*(i - 0.9f*n))/1000f;
			}
			if((i == 0) && ((float)it * ondefreq)< 360f * nbExcitation){
				npoints[i].h = MathUtils.sinDeg(((float)it)*ondefreq);
				npoints[i].vel = 0f;
			}
		}

		it+=1;
		Point[] temp = points;
		points = npoints;
		npoints = temp;
	}
	@Override
	public void render () {
		ondefreq = 1f/60f;
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		for(int i = 0 ; i < itmult ; i++)
			update();	

		pixmap.setColor(1f, 1f, 1f, 1f);
		pixmap.fill();
		pixmap.setColor(0f, 0f, 0f, 1f);
		if(showWaves){

			for (int i = 0; i < n; i++) {

				pixmap.fillRectangle(i*mag, n * mag/2 + (int)(points[i].h*50f), mag, mag);
			} 
		}
		if(showVector){
			pixmap.setColor(Color.GREEN);
			for (int i = 0; i < n; i++) {

				pixmap.drawLine(i*mag + mag/2, n * mag/2, i*mag + mag/2, n * mag/2 + (int)( points[i].vel*500f));
			} 

		}
		batch.begin();
		batch.draw(pixmapTexture, 
				Gdx.graphics.getWidth() / 2f - pixmapTexture.getWidth() /2f, Gdx.graphics.getHeight() /2f - pixmapTexture.getHeight() / 2f); //draw pixmap texture
				Gdx.gl.glTexSubImage2D(Gdx.gl.GL_TEXTURE_2D, 0, 0, 0, pixmapTexture.getWidth(), pixmapTexture.getHeight(), //
						pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
				batch.end();

	}

	@Override
	public boolean keyDown(int keycode) {

		if(keycode == Keys.UP){
			msDelay += 1;
		}
		if(keycode == Keys.DOWN){
			msDelay -= 1;
		}
		if(keycode == Keys.V){
			showVector = !showVector;
		}
		if(keycode == Keys.W){
			showWaves = !showWaves;
		}
		if(keycode == Keys.RIGHT){
			nbExcitation += 1;
		}
		if(keycode == Keys.LEFT){
			nbExcitation -= 1;
		}
		if(keycode == Keys.I){
			initPoints();
		}
		if(keycode == Keys.L){
			it = 0;
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
