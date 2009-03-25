/*
 * Abstract Render
 * Android Comapping, 2009
 * Autor: Korshakov Stepan
 * 
 * Abstract Render for map rendering engines
 */

package com.comapping.android.view;

import android.graphics.Canvas;


public abstract class Render {
	
	public abstract void draw(int x, int y, int width, int height, Canvas c);
	public abstract void onTouch(int x, int y);
	public abstract int getWidth();
	public abstract int getHeight();
}
