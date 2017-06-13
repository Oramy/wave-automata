package com.hx2.fresnel.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.hx2.fresnel.Fresnel;
import com.hx2.fresnel.Fresnel2;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.width = 500;
		config.height = 500;
		config.title = "Modèle onde 2D : diffraction avec mur absorbant";
		new LwjglApplication(new Fresnel(), config);
	}
}
