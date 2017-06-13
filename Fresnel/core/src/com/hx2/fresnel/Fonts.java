package com.hx2.fresnel;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

public class Fonts {
	public static BitmapFont choiceFont;
	public static GlyphLayout glyphLayout;
	static {
		glyphLayout = new GlyphLayout();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(
				Gdx.files.internal("fonts/coffee+teademo-Regular.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 72;
		parameter.magFilter = TextureFilter.Linear;
		parameter.minFilter = TextureFilter.MipMapLinearLinear;
		parameter.genMipMaps = true;
		parameter.shadowColor = Color.BLACK;
		parameter.shadowOffsetX = 3;
		parameter.shadowOffsetY = 3;
		choiceFont = generator.generateFont(parameter); // font size 12 pixels
		parameter.size = 72;
		
		generator.dispose();
	}
}
