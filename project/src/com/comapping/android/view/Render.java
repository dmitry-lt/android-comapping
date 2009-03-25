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
	
	 /**
	  * Renders a map
	  * @author Stepan Korshakov
	  * @param x		 X-coordinate
	  * @param y		 Y-coordinate
	  * @param wigth	 width of rendering rectangle
	  * @param height 	 height of rendering rectangle
	  * @param c 	 	 Canvas for rendering
	  */
	public abstract void draw(int x, int y, int width, int height, Canvas c);
	/**
	  * OnTouch event
	  * @author Stepan Korshakov
	  * @param x	absolute X-coordinate
	  * @param y	absolute Y-coordinate
	 */
	public abstract void onTouch(int x, int y);
	
	/**
	  * Returns width of rendering rectangle
	  * @author Stepan Korshakov
	  * @return Width of rendering rectangle
	  */
	public abstract int getWidth();
	
	/**
	  * Returns height of rendering rectangle
	  * @author Stepan Korshakov
	  * @return Height of rendering rectangle
	  */
	public abstract int getHeight();
}
