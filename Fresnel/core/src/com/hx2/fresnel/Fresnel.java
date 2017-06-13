package com.hx2.fresnel;

import java.util.ArrayList;
import java.util.List;

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

public class Fresnel extends ApplicationAdapter implements InputProcessor {
	public enum Direction{
		Up, Down, Left, Right
	}
	public class Point{
		public float vel;
		public float h;
	}
	public class Wall{
		public float K;
		public Direction dir;
		public final float x, y, width, height;
		public Wall(int x, int y, int width, int height, float K, Direction dir){
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			this.K = K;
			this.dir = dir;
		}
		public float gradient(int i, int j){
			switch(dir){
			case Right:
				return (i - x)/width;
			case Left:
				return (x+width - i)/width;
			case Down:
				return (j - y)/height;
			case Up:
				return (y+height - j)/height;
			
			}	
			return 0;
		}
		public float process(int i, int j){
			float force = 0f;
			if(i >= x && i <= x + width
					&& j >= y && j <= y + height){

				switch(dir){
				case Right:
					force -= points[i][j].vel*K*((i - x)*(i - x)/(width*width));
					break;
				case Left:
					force -= points[i][j].vel*K*(((x+width) - i)*((x+width))/(width*width));
					break;
				case Up:
					force -= points[i][j].vel*K*((y+height) - j)*((y + height) - j)/(height*height);
					break;
				case Down:
					force -= points[i][j].vel*K*((j - y)*(j - y)/(height*height));
					break;
				}	
			}
			return force;
		}
	}
	/*
	 * Murs
	 * rectangle
	 * Entrees/sorties
	 * Afficher spécialement
	 * Coef frottement fluide
	 */
	private static final int mag = 5;

	SpriteBatch batch;
	Texture pixmapTexture;
	Pixmap pixmap;
	Point[][] points;
	Point[][] npoints;
	List<Wall> walls; 

	long msDelay = 16, lastUp;
	int n = 100;
	int it = 0;
	float sqr2 = (float) Math.sqrt(2);
	private boolean showVector = true;
	private boolean showWaves = true;
	private int itmult = 10;
	private float ondefreq = 1f/10f;

	private int nbExcitation = 5;

	private float dt = 0.01f;

	@Override
	public void create () {
		batch = new SpriteBatch();
		points = new Point[n][n];
		initPoints();
		lastUp = System.currentTimeMillis();
		Gdx.input.setInputProcessor(this);
		pixmap = new Pixmap( n * mag, n * mag, Format.RGB888);
		pixmapTexture = new Texture(pixmap, Pixmap.Format.RGB888, false);
		this.walls = new ArrayList<Wall>();
//		this.walls.add(new Wall(0, n/2-5, n/2-8, 10, 10f, Direction.Up));
//		this.walls.add(new Wall(n/2-2, n/2-5, 4, 10, 10f, Direction.Up));
//		this.walls.add(new Wall(n/2+8, n/2-5, n, 10, 10f, Direction.Up));
	}

	public void initPoints(){
		npoints = new Point[n][n];
		points = new Point[n][n];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				points[i][j] = new Point();
				points[i][j].vel = 0f;
				points[i][j].h = 0f;

				npoints[i][j] = new Point();
				npoints[i][j].vel = 0f;
				npoints[i][j].h = 0f;
			}
		}
		it = 0;
	}
	public void update(){

		float force = 0f;
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				force = 0f;

				if(i >= 1)
					force += (points[i-1][j].h-points[i][j].h);
				if(i < n-1)
					force += (points[i+1][j].h-points[i][j].h);
				if(j >= 1)
					force += (points[i][j-1].h-points[i][j].h);
				if(j < n-1)
					force += (points[i][j+1].h-points[i][j].h);

				for(Wall wall : walls){
					force += wall.process(i, j);
				}
				//				if((i >= n/2 && i <= n/2 + 5) && j == n/2 - 2 ){
				//					force -= (points[i][j-1].h-points[i][j].h);
				//				}
				//				if((i >= n/2 && i <= n/2 + 5) && j == n/2 - 3 ){
				//					force -= (points[i][j+1].h-points[i][j].h);
				//				}
				//				if((i >= n/2 && i <= n/2 + 5) && j == n/2 + 2 ){
				//					force -= (points[i][j+1].h-points[i][j].h);
				//				}
				//				if((i >= n/2 && i <= n/2 + 5) && j == n/2 + 3 ){
				//					force -= (points[i][j-1].h-points[i][j].h);
				//				}
				//				if((j <= n/2-2 || j >= n/2 +2)){
				//					if(i == n/2 + 5){
				//						force -= (points[i+1][j].h-points[i][j].h);
				//					}
				//					if(i == n/2 + 6){
				//						force -= (points[i-1][j].h-points[i][j].h);
				//					}
				//					if((i >= n/2 && i <= n/2 + 5)){
				//						force -= points[i][j].vel*0.05f*((i - n/2)*(i - n/2));
				//					}
				//				}
				npoints[i][j].vel = points[i][j].vel +  force*dt;

				npoints[i][j].h = points[i][j].h + (points[i][j].vel+npoints[i][j].vel)/2f*dt + force/2f*dt*dt;
				if((i==0) && ((float)it * ondefreq)< 360f * nbExcitation){
					npoints[i][j].h = MathUtils.sinDeg(((float)it)*ondefreq);
				}
			}
		}

		it+=1;
		Point[][] temp = points;
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
		pixmap.setColor(0f, 0f, 0f, 1f);
		if(showWaves){

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					float c = MathUtils.clamp(points[i][j].h/2f, -1f, 1f);
					c = (c+1f)/2f;
//					if((j <= n/2-2 || j >= n/2 +2)){
//						if((i >= n/2 && i <= n/2 + 5)){
//							c = 0f;
//						}
//					}
					pixmap.setColor(c, c, c, 1f);
					pixmap.fillRectangle(i*mag, j*mag, mag, mag);
				}
			} 
			for(Wall wall : walls){
				for (int i = (int)wall.x; i < (int)(wall.x + wall.width); i++) {
					for (int j = (int)wall.y; j < (int)(wall.y + wall.height); j++) {
						float c = wall.gradient(i, j);
						pixmap.setColor(c, c, 1f-c, 0.5f);
						pixmap.fillRectangle(i*mag, j*mag, mag, mag);
					}
				}
			}
			//			fillPixmap(pixmap, points, n, n, true);
		}
		else
		{
			for (int i = 0; i < n-1; i++) {
					pixmap.drawLine(0, pixmap.getHeight()/2, pixmap.getWidth(), pixmap.getHeight()/2);
					float c = MathUtils.clamp(points[i][10].h/2f, -1f, 1f);
					c = (c+1f)/2f;
//					if((j <= n/2-2 || j >= n/2 +2)){
//						if((i >= n/2 && i <= n/2 + 5)){
//							c = 0f;
//						}
//					}
					pixmap.setColor(0,0,0, 1f);
					pixmap.drawLine(i*mag, (int) (pixmap.getWidth()/2f + points[i][10].h*pixmap.getWidth()/10f), (i+1)*mag, (int) (pixmap.getWidth()/2f + points[i+1][10].h*pixmap.getWidth()/10f));
			} 
		}
		
		if(showVector){
			pixmap.setColor(Color.GREEN);
			//			for (int i = 0; i < n; i++) {
			//				
			//				pixmap.drawLine(i*mag + mag/2, n * mag/2, i*mag + mag/2, n * mag/2 + (int)( points[i][j].vel*500f));
			//			} 

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
			it =  0;
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
