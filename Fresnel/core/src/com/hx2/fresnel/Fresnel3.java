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

public class Fresnel3 extends ApplicationAdapter implements InputProcessor {
	public class Point{
		public float vel;
		public float h;
		public float force;
		public float ampl;
	}

	SpriteBatch batch;
	Texture img;
	Texture pixmapTexture;
	Pixmap pixmap;
	Point[] points;
	Point[] npoints;
	long msDelay = 16, lastUp;
	int n = 1000;
	float it = 0f;
	private boolean showVector = false;
	private boolean showCelerity = true;
	private boolean showWaves = true;
	private boolean showAmpl = false;
	private int measAmpl = 0;	
	private int itmult = 1000;
	private float dt = 1/1000f;
	private float k_frot = 0.00f;
	private float pas=0.01f;
	private float k_ress=1f;
	private float masse=1f;
	private float K=0.001f;
	private float M=100f;
	private float celerity= pas * (float)Math.sqrt((double)k_ress/masse);
//	private float puls = (float) 4f*MathUtils.PI*celerity/(pas*(n+1));
	private float puls = (float) Math.sqrt((K+k_ress)/M);
	private float freq = puls/MathUtils.PI/2f;

	private int nbExcitation = 6;

	@Override
	public void create () {
		batch = new SpriteBatch();
		pixmap = new Pixmap(n, n, Format.RGB888);
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
			points[i].ampl = 0f;

			npoints[i] = new Point();
			npoints[i].vel = 0f;
			npoints[i].h = 0f;
			points[i].ampl = 0f;
		}
		it = 0f;
	}
	public void update(){
		
		for(int i = 0; i < n; i++){
			npoints[i].force =  0;
			
			if(i >= 1)
				npoints[i].force += points[i-1].h-points[i].h;
			if(i < n-1)
				npoints[i].force += points[i+1].h-points[i].h;
			npoints[i].force *= k_ress;
			
			
			
			if(i == n-1){
				npoints[i].force -= points[i].h*K;
				npoints[i].force /= M;
			} else{
				npoints[i].force -= points[i].vel*k_frot;
				npoints[i].force /= masse;
			}
			
			
			
			
			
			npoints[i].vel = points[i].vel +  points[i].force*dt;
			npoints[i].h = points[i].h + (points[i].vel+npoints[i].vel)/2f*dt +  points[i].force/2f*dt*dt;
			
			if(i == 0 && it*freq < nbExcitation){
//				npoints[i].h = MathUtils.sin (it*puls) > 0f ? 2f : -2f;
				npoints[i].h = MathUtils.sin (it*puls);
				npoints[i].vel = 0f;
			}
			
			if(measAmpl>0){
				if(npoints[i].h>points[i].ampl)
					npoints[i].ampl=npoints[i].h;
			}
		}
		
		if(measAmpl>0)
			measAmpl+=1;
		if(measAmpl*dt*freq>=2){
			measAmpl=0;
		}
		it+=dt;
		Point[] temp = points;
		points = npoints;
		npoints = temp;
	}
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		while(lastUp + msDelay < System.currentTimeMillis()){
			lastUp += msDelay;
			for(int i = 0 ; i < itmult ; i++)
				update();	
		}

		pixmap.setColor(1f, 1f, 1f, 1f);
		pixmap.fill();
		if(showCelerity){
			pixmap.setColor(Color.GOLD);
			pixmap.fillRectangle(0, 0, (int) (celerity*it/pas), n);
//			pixmap.setColor(Color.GREEN);
//			pixmap.fillRectangle(0, n/2-n/10, n,2*n/10);
			
		}
		if(showWaves){
			pixmap.setColor(Color.BLACK);
			for (int i = 0; i < n; i++) {

				pixmap.drawPixel(i, n /2 + (int)(points[i].h*n/10f));
			} 
		}
		if(showVector){
			pixmap.setColor(Color.GREEN);
			for (int i = 0; i < n; i++) {

				pixmap.drawLine(i, n /2, i, n /2 + (int)( points[i].vel*500f));
			} 

		}
		if(showAmpl){
			pixmap.setColor(Color.GREEN);
			for (int i = 0; i < n; i++) {

				pixmap.drawLine(i, n /2, i, n /2 + (int)( points[i].ampl*n/10));
			} 

		}
		batch.begin();
		batch.draw(pixmapTexture, 
				0f, 0f, 500f, 500f); //draw pixmap texture
		Gdx.gl.glTexSubImage2D(Gdx.gl.GL_TEXTURE_2D, 0, 0, 0, pixmapTexture.getWidth(), pixmapTexture.getHeight(), //
			pixmap.getGLFormat(), pixmap.getGLType(), pixmap.getPixels());
		batch.end();

	}

	@Override
	public boolean keyDown(int keycode) {

		if(keycode == Keys.UP){
			puls *= 2;
		}
		if(keycode == Keys.DOWN){
			puls /= 2;
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
			it = 0f;
		}
		if(keycode == Keys.C){
			showCelerity=!showCelerity;
		}
		if(keycode == Keys.A){
			for(int i = 0; i < n; i++){
				points[i].ampl=0f;
				npoints[i].ampl=0f;
			}
			measAmpl=1;
			showAmpl=true;
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
