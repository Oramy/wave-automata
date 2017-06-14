package com.hx2.fresnel;

import java.util.ArrayList;
import java.util.List;

import com.hx2.fresnel.Fresnel.Direction;
import com.hx2.fresnel.Fresnel.Point;

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
	public List<Coupure> generateCoupures(int n){
		List<Coupure> coupures = new ArrayList<Coupure>();
		if(dir != Direction.Left)
		{
			for(int j = (int) y; j <= y + height; j++){
				if(x+width < n-1)
					coupures.add(new Coupure((int) (x+width), j, Direction.Right));
			}
		}
		if(dir != Direction.Right)
		{
			for(int j = (int) y; j <= y + height; j++){
				if(x > 1)
					coupures.add(new Coupure((int) x, j, Direction.Left));
			}
		}
		if(dir != Direction.Up)
		{
			for(int i = (int) x; i <= x + width; i++){
				if(y+height < n-1)
					coupures.add(new Coupure(i, (int) (y+height), Direction.Down));
			}
		}
		if(dir != Direction.Down)
		{
			for(int i = (int) x; i <= x + width; i++){
				if(y > 1)
					coupures.add(new Coupure(i, (int) y, Direction.Up));
			}
		}
		return coupures;
	}
	public float process(Point[][] points, int i, int j){
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
