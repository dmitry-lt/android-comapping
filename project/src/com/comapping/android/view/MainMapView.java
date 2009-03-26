package com.comapping.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

public class MainMapView extends View {

	Render mRender;
	Scroller mScroller;

	public MainMapView(Context context, Render render) {
		super(context);
		mRender = render;
		mScroller = new Scroller(context);
	}

	int frameCount = 0;
	long fps = 0;
	long lastFPSCalcTime = System.currentTimeMillis();

	@Override
	protected void onDraw(Canvas canvas) {
		mScroller.computeScrollOffset();

		Paint p = new Paint();
		canvas.drawARGB(255, 255, 255, 255);

		mRender.draw(mScroller.getCurrX(), mScroller.getCurrY(), this
				.getWidth(), this.getHeight(), canvas);

		p.setColor(Color.BLACK);
		canvas.drawText("FPS: " + fps, 20, 30, p);
		if (System.currentTimeMillis() - lastFPSCalcTime > 1000) {
			fps = (1000 * frameCount)
					/ (System.currentTimeMillis() - lastFPSCalcTime);
			lastFPSCalcTime = System.currentTimeMillis();
			frameCount = 0;
		}
		frameCount++;
		invalidate();
	}

	VelocityTracker mVelocityTracker;
	int oldX = -1, oldY = -1;
	long touchStartTime = 0;

	private static final long TAP_MAX_TIME = 100;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mVelocityTracker = VelocityTracker.obtain();
			oldX = (int) ev.getX();
			oldY = (int) ev.getY();
			touchStartTime = System.currentTimeMillis();
			return true;
		}
		case MotionEvent.ACTION_MOVE: {
			mVelocityTracker.addMovement(ev);

			int deltaX = (int) (oldX - ev.getX());
			int deltaY = (int) (oldY - ev.getY());
			oldX = (int) ev.getX();
			oldY = (int) ev.getY();

			mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(),
					deltaX, deltaY, 0);
			return true;
		}
		case MotionEvent.ACTION_UP: {
			if (System.currentTimeMillis() - touchStartTime < TAP_MAX_TIME) {
				mRender.onTouch((int)ev.getX(), (int)ev.getY());
			} else {
				mVelocityTracker.addMovement(ev);

				mVelocityTracker.computeCurrentVelocity(1000);

				int vx = (int) mVelocityTracker.getXVelocity();
				int vy = (int) mVelocityTracker.getYVelocity();

				vx = vx * 3 / 4;
				vy = vy * 3 / 4;

				mScroller
						.fling(mScroller.getCurrX(), mScroller.getCurrY(), -vx,
								-vy, 0, mRender.getWidth(), 0, mRender
										.getHeight());

				mVelocityTracker.recycle();
				
				mRender.onTouch(10000, 10000);

				return true;
			}
		}

		}
		return true;
	}
}
