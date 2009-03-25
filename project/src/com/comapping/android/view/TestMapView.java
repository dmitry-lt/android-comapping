/*
 * TestMapView Controller
 * Android Comapping, 2009
 * Last change: Korshakov Stepan
 * 
 * Test class for MapRender
 */

package com.comapping.android.view;

import com.comapping.android.model.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;

public class TestMapView extends View {

	Drawable image;
	Bitmap bmp;
	ComappingRender render;
	
	VelocityTracker mVelocityTracker;

	public TestMapView(Context context, Map map) {
		super(context);
		render = new ComappingRender(context, map.getRoot());
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
		
		canvas.save();
		Matrix m =  canvas.getMatrix();
		m.postScale(0.75f, 0.75f);
		canvas.setMatrix(m);
		render.draw(0, 0, 320, 480,canvas);
		canvas.restore();
		
		p.setColor(Color.BLACK);
		canvas.drawText("FPS: " + FPS, 10, 30, p);
		if (System.currentTimeMillis() - startTime > 1000) {
			FPS = (1000 * count) / (System.currentTimeMillis() - startTime);
			startTime = System.currentTimeMillis();
			count = 0;
		}
		count++;
		invalidate();
	}
}
