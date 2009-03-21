/*
 * TestMapView Controller
 * Android Comapping, 2009
 * Last change: Korshakov Stepan
 * 
 * Test class for MapRender
 */

package com.comapping.android.view;

import com.comapping.android.model.Map;
import com.comapping.android.model.MapRender;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.View;

public class TestMapView extends View {

	Drawable image;
	Bitmap bmp;
	MapRender render;

	public TestMapView(Context context, Map map) {
		super(context);
		render = new MapRender(map.getRoot());
	}

	int count = 0;
	long FPS = 0;
	long startTime = System.currentTimeMillis();
	
	//FPS added for cheking if we make a mistake in code
	//Rendering must be fast for smooth scrolling
	@Override
	protected void onDraw(Canvas canvas) {
		Paint p = new Paint();
		canvas.drawARGB(255, 255, 255, 255);
		
		render.draw(canvas);
		
		p.setColor(Color.GREEN);
		canvas.drawText("FPS: " + FPS, 10, 10, p);
		if (System.currentTimeMillis() - startTime > 1000) {
			FPS = (1000 * count) / (System.currentTimeMillis() - startTime);
			startTime = System.currentTimeMillis();
			count = 0;
		}
		count++;
		invalidate();
	}
}
