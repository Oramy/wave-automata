package com.hx2.fresnel;

import java.util.ArrayList;
import java.util.List;

import javax.swing.plaf.multi.MultiRootPaneUI;

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
	public static final int dis[] = {0, 0, -1, 1};
	public static final int djs[] = {-1, 1, 0, 0};
	public class Point{
		public float vel;
		public float h;
		public float force;
		public float ampl;
	}

	SpriteBatch batch;
	Texture pixmapTexture;
	Pixmap pixmap;
	Point[][] points;
	Point[][] npoints;
	List<Coupure> coupures;
	List<Wall> walls; 

	long msDelay = 16, lastUp;
	int n = 300;
	int it = 0;
	private boolean showWaves = true;
	private boolean showAmpl = false;
	private boolean pause = false;
	private int itmult = 20;
	private float ondefreq = 1f/20f;
	private float lambda = 1/ondefreq;
	

	private int nbExcitation = 0;
	private boolean source = false;
	private float phi=90;
	
	private boolean cristal = false;
	private int ecart = (int) (lambda/2f);
	private int taille = 20;
	private int pos = (n-(taille-1) * ecart)/2;
	
	private boolean refrac = false;
	private float k = 2f;
	private float inclinaison = -0.3f;
	
	private boolean circle=false;
	private int rayon=n/4;

	private float dt = 0.01f;
	
	private boolean originePosee = false;
	private int originI, originJ = 0;
	private float basicK = 4f;

	@Override
	public void create () {
		batch = new SpriteBatch();
		points = new Point[n][n];
		initPoints();
		lastUp = System.currentTimeMillis();
		Gdx.input.setInputProcessor(this);
		pixmap = new Pixmap( n, n, Format.RGB888);
		pixmapTexture = new Texture(pixmap, Pixmap.Format.RGB888, false);
		this.walls = new ArrayList<Wall>();
		this.coupures = new ArrayList<Coupure>();
		
		
		addWall(new Wall(0, n/2-n*5/100, n/2-n*8/100, 10*n/100, 10f, Direction.Up));
		addWall(new Wall(n/2-2*n/100, n/2-5*n/100, 4*n/100, 10*n/100, 10f, Direction.Up));
		addWall(new Wall(n/2+8*n/100, n/2-5*n/100, n/2-8*n/100 -1, 10*n/100, 10f, Direction.Up));
		
		
		
	}
	public void addWall(Wall wall){
		this.walls.add(wall);
		this.coupures.addAll(wall.generateCoupures(n));
	}

	public void initPoints(){
		npoints = new Point[n][n];
		points = new Point[n][n];
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				points[i][j] = new Point();
				points[i][j].vel = 0f;
				points[i][j].h = 1f;

				npoints[i][j] = new Point();
				npoints[i][j].vel = 0f;
				npoints[i][j].h = 0f;
			}
		}
		it = 0;
	}
	public void updateSpringForce(){
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
				
				if(refrac && n/2 + inclinaison*i <=j)
					force*=k;
				
				points[i][j].force = force;
			}
		}
		for (Wall wall : walls){
			for(int i = (int) wall.x; i <= wall.x + wall.width; i++){
				for(int j = (int) wall.y; j <= wall.y + wall.height; j++){
					points[i][j].force += wall.forceProcess(points, i, j);
				}
			}
		}
		for(Coupure coupure : coupures){
			int i = coupure.i;
			int j = coupure.j;
			switch(coupure.dir){
			case Down:
				points[i][j].force -= (points[i][j+1].h-points[i][j].h);
				points[i][j+1].force -= (points[i][j].h-points[i][j+1].h);
				break;
			
			case Right:
				points[i][j].force -= (points[i+1][j].h - points[i][j].h);
				points[i+1][j].force -= (points[i][j].h - points[i+1][j].h);
				break;
			case Left:
				points[i][j].force -= (points[i-1][j].h - points[i][j].h);
				points[i-1][j].force -= (points[i][j].h - points[i-1][j].h);
				break;
			case Up:
				points[i][j].force -= (points[i][j-1].h-points[i][j].h);
				points[i][j-1].force -= (points[i][j].h-points[i][j-1].h);
				break;
			default:
				break;
				
			}
		}

	}
	public void updateCoordinates(){
		float sin = 1f + MathUtils.sinDeg(((float)it)*dt*ondefreq*360);
		float sinDephase1 = 1f +MathUtils.sinDeg(((float)it)*dt*ondefreq*360+phi);
		float sinDephase2 = 1f +MathUtils.sinDeg(((float)it)*dt*ondefreq*360-phi);
		if(((float)it*dt * ondefreq)<= 1/4)
			sin = 1f+(sin-1f)*(sin-1f);
		
		for(int i = 0; i < n; i++){
			for(int j = 0; j < n; j++){
				npoints[i][j].vel = points[i][j].vel +  points[i][j].force*dt;
				npoints[i][j].h = points[i][j].h + (points[i][j].vel+npoints[i][j].vel)/2f*dt +  points[i][j].force/2f*dt*dt;
			}
			
			if(((float)it*dt * ondefreq)<= nbExcitation){
				npoints[i][n-1].h = sin;
			}
		}
				
		if(source){
			if(((float)it*dt * ondefreq)<= phi/360f)
				sinDephase2=1;
			if(((float)it*dt * ondefreq)<= 0.5-phi/360f)
				sinDephase1=1;
			npoints[n/2][n/2].h=sin;
			npoints[n/2+(int) (lambda/4f)][n/2].h=sinDephase1;
			npoints[n/2-(int) (lambda/4f)][n/2].h=sinDephase2;
		}
		
		if(cristal){
			for(int k=0; k<taille; k++){
				for(int l=0; l<taille; l++){
					npoints[pos+k*ecart][pos+l*ecart].vel = 0;
					npoints[pos+k*ecart][pos+l*ecart].h=1;
				}
			}
		}

		if(showAmpl){
			for(int i = 0; i < n; i++){
				for(int j = 0; j < n; j++){
					if(Math.abs(npoints[i][j].h)>points[i][j].ampl)
						npoints[i][j].ampl=Math.abs(npoints[i][j].h);
				}
			}
		}
//		if(measAmpl*dt*ondefreq>=3)
//			measAmpl=0;
	}
	public void update(){
	
		if(!pause){
			updateSpringForce();
			updateCoordinates();
	
				
			it+=1;
			Point[][] temp = points;
			points = npoints;
			npoints = temp;
		}
	}
	
	public void cardoid(int x,int y){
		float r=MathUtils.clamp(n*(points[n/2+x][n/2+y].ampl-1), 0, n/2-1);
		float t=MathUtils.atan2(y, x);
		pixmap.drawPixel(n/2 + (int) (r*MathUtils.cos(t)), n/2 + (int) (r*MathUtils.sin(t)));
	}
	
	@Override
	public void render () {
		Gdx.gl.glClearColor(1, 1, 1, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		long startTime = System.currentTimeMillis();
		for(int i = 0 ; i < itmult ; i++)
			update();	

		pixmap.setColor(1f, 1f, 1f, 1f);
		pixmap.fill();
		pixmap.setColor(0f, 0f, 0f, 1f);
		if(showWaves){

			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					float c = MathUtils.clamp((points[i][j].h-1f)/2f, -1f, 1f);
					c = (c+1f)/2f;
					pixmap.setColor(c, c, c, 1f);
					pixmap.drawPixel(i, j);
				}
			} 
			for(Wall wall : walls){
				for (int i = (int)wall.x; i <= (int)(wall.x + wall.width); i++) {
					for (int j = (int)wall.y; j <= (int)(wall.y + wall.height); j++) {
						float c = wall.gradient(i, j);
						pixmap.setColor(c, c, 1f-c, 0.5f);
						pixmap.drawPixel(i, j);
					}
				}
			}
			pixmap.setColor(1f, 0f, 0f, 1f);
			for(Coupure coupure : coupures){
				int i = coupure.i;
				int j = coupure.j;
				
				switch(coupure.dir){
				case Down:
					pixmap.drawLine(i,(j+1), (i+1), (j+1));
					break;
				case Left:
					pixmap.drawLine(i,j, i, (j+1));
					break;
				case Right:
					pixmap.drawLine((i+1),j, (i+1), (j+1));
					break;
				case Up:
					pixmap.drawLine(i,j, (i+1), j);
					break;
				default:
					break;
					
				}
			}
		}
		
//		else
//		{
//			for (int i = 0; i < n-1; i++) {
//					pixmap.drawLine(0, pixmap.getHeight()/2, pixmap.getWidth(), pixmap.getHeight()/2);
//					float c = MathUtils.clamp(points[i][10].h/2f, -1f, 1f);
//					c = (c+1f)/2f;
//
//					pixmap.setColor(0,0,0, 1f);
//					pixmap.drawLine(i, (int) (pixmap.getWidth()/2f + points[i][10].h*pixmap.getWidth()/10f), (i+1), (int) (pixmap.getWidth()/2f + points[i+1][10].h*pixmap.getWidth()/10f));
//			} 
//		}
		if(showAmpl){
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					float c = MathUtils.clamp(points[i][j].ampl-1, 0f, 1f);
					pixmap.setColor(c, c, c, 1f);
					pixmap.drawPixel(i, j);
				}
			}  

		}
		
		if(refrac){
			pixmap.setColor(Color.GREEN);
			pixmap.drawLine(0,n/2,n,(int) (n/2+n*inclinaison));
		}
		
		if(circle){
			pixmap.setColor(Color.BLUE);
			pixmap.drawCircle(n/2, n/2, rayon);
			pixmap.setColor(Color.GREEN);
			int x=rayon;
			int y=0;
			int d=rayon*rayon;
			while(x>=0){
				if(x*x+(y+1)*(y+1)>=d){
					x-=1;
				}else{
					y+=1;
				}
				cardoid(x,y);
				cardoid(x,-y);
				cardoid(-x,y);
				cardoid(-x,-y);
			}
		}
		
//		pixmap.setColor(Color.CYAN);
//		pixmap.drawLine(pixmap.getWidth()/2, pixmap.getHeight()/2, pixmap.getWidth()/2 + (int) lambda, pixmap.getHeight()/2);
		
		batch.begin();
		pixmapTexture.draw(pixmap, 0, 0);
		batch.draw(pixmapTexture,  0f, 0f,
				Constants.WIDTH, Constants.HEIGHT); //draw pixmap texture
		batch.end();
		batch.begin();
//		Fonts.choiceFont.draw(batch, "Dhuruhe", 20, 20);
		batch.end();

	}


	@Override
	public boolean keyDown(int keycode) {

		if(keycode == Keys.UP){
			phi += 5;
		}
		if(keycode == Keys.DOWN){
			phi -= 5;
		}
		if(keycode == Keys.W){
			addWall(8*n/100,0, 0, n-1);
			addWall(n-1, 8*n/100, 0, 0);
			addWall(n-8*n/100-1,0,n-1,n-1);
			addWall(n-1,n-8*n/100-1,0,n-1);
			
		}
		if(keycode == Keys.RIGHT){
			nbExcitation += 1;
		}
		if(keycode == Keys.LEFT){
			nbExcitation -= 1;
		}
		if(keycode == Keys.I){
			initPoints();
			walls.clear();
			coupures.clear();
			source=false;
			showAmpl=false;
			nbExcitation=0;
		}
		if(keycode == Keys.J){
			initPoints();
		}
		if(keycode == Keys.L){
			it =  0;
		}
		if(keycode == Keys.S){
			source=!source;
		}
		if(keycode == Keys.A){
			for(int i = 0; i < n; i++){
				for(int j=0; j<n;j++){
					points[i][j].ampl=0f;
					npoints[i][j].ampl=0f;
				}
			}
			showAmpl=true;
			showWaves=false;
		}
		if(keycode == Keys.O){
			for(int i = 0; i < n; i++){
				for(int j=0; j<n;j++){
					points[i][j].ampl=0f;
					npoints[i][j].ampl=0f;
				}
			}
			showAmpl=false;
			showWaves=true;
		}
		if(keycode == Keys.P){
			pause=!pause;
		}
		if(keycode == Keys.C)
			cristal = !cristal;
		if(keycode == Keys.R)
			refrac=!refrac;
		if(keycode == Keys.M)
			circle=!circle;
			
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

	public void addWall(int originI, int originJ, int i, int j){
		int mi = Math.min(i, originI);
		int mj = Math.min(j, originJ);
		int Mi = Math.max(i, originI);
		int Mj = Math.max(j, originJ);
		
		Direction dir;
		if(Mi-mi > Mj-mj){
			if(originJ > j)
				dir = Direction.Up;
			else
				dir = Direction.Down;
		}
		else{
			if(originI > i)
				dir = Direction.Left;
			else
				dir = Direction.Right;
		}
		
		Wall wall = new Wall(mi, mj, Mi-mi, Mj-mj, basicK, dir);
		addWall(wall);
	}
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		int i = screenX*n/Gdx.graphics.getWidth();
		int j = screenY*n/Gdx.graphics.getHeight();
		
		
		if(!originePosee){
			originJ = j;
			originI = i;
		}
		else
		{
			addWall(i, j, originI, originJ);
		}
		originePosee = !originePosee;
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return false;
	}

	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
}
