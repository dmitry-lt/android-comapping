package com.comapping.android.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;

public class MainMapView extends View {

	public MapRender mRender;
	public Scroller mScroller;

	ScrollController scrollController = new ScrollController() {

		@Override
		public void intermediateScroll(int destX, int destY) {

			int vx = mScroller.getCurrX() - destX;
			int vy = mScroller.getCurrY() - destY;

			mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(),
					-vx, -vy, 0);
			mScroller.computeScrollOffset();
		}

		@Override
		public void smoothScroll(int destX, int destY) {
			int vx = mScroller.getCurrX() - destX;
			int vy = mScroller.getCurrY() - destY;
			mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(),
					-vx, -vy);
		}

	};

	public MainMapView(Context context, MapRender render) {
		super(context);
		setFocusable(true);
		mRender = render;
		mRender.setScrollController(scrollController);
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

		mRender.draw(mScroller.getCurrX(), -getVertOffset()
				+ mScroller.getCurrY(), this.getWidth(), this.getHeight(),
				canvas);

		p.setColor(Color.BLACK);
		canvas.drawText("FPS: " + fps, 20, 30, p);
		canvas.drawText("Width: " + mRender.getWidth(), 20, 50, p);
		canvas.drawText("Height: " + mRender.getHeight(), 20, 70, p);
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
	int startX = -1, startY = -1;
	long touchStartTime = 0;
	boolean fixedScroll = true;

	private static final long TAP_MAX_TIME = 500;
	private static final long BLOCK_PATH_LEN = 1000;

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		int action = ev.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			mVelocityTracker = VelocityTracker.obtain();
			oldX = (int) ev.getX();
			oldY = (int) ev.getY();
			startX = oldX;
			startY = oldY;

			touchStartTime = System.currentTimeMillis();
			fixedScroll = true;
			return true;
		}
		case MotionEvent.ACTION_MOVE: {
			int pathLen = (startX - (int) ev.getX())
					* (startX - (int) ev.getX()) + (startY - (int) ev.getY())
					* (startY - (int) ev.getY());
			long timeDelta = System.currentTimeMillis() - touchStartTime;

			if ((timeDelta >= TAP_MAX_TIME) || (pathLen >= BLOCK_PATH_LEN))
				fixedScroll = false;

			if (!fixedScroll) {
				mVelocityTracker.addMovement(ev);

				int deltaX = (int) (oldX - ev.getX());
				int deltaY = (int) (oldY - ev.getY());
				oldX = (int) ev.getX();
				oldY = (int) ev.getY();

				if (mScroller.getCurrX() + deltaX > getScrollWidth())
					deltaX = getScrollWidth() - mScroller.getCurrX();
				if (mScroller.getCurrY() + deltaY > getScrollHeight())
					deltaY = getScrollHeight() - mScroller.getCurrY();

				if (mScroller.getCurrX() + deltaX < 0)
					deltaX = -mScroller.getCurrX();
				if (mScroller.getCurrY() + deltaY < 0)
					deltaY = -mScroller.getCurrY();

				mScroller.startScroll(mScroller.getCurrX(), mScroller
						.getCurrY(), deltaX, deltaY, 0);
			}
			return true;
		}
		case MotionEvent.ACTION_UP: {
			long timeDelta = System.currentTimeMillis() - touchStartTime;
			int pathLen = (startX - (int) ev.getX())
					* (startX - (int) ev.getX()) + (startY - (int) ev.getY())
					* (startY - (int) ev.getY());

			Log.i("Test", "Time:" + timeDelta + " Path len:" + pathLen);
			if ((timeDelta < TAP_MAX_TIME) && (pathLen < BLOCK_PATH_LEN)) {
				mRender.onTouch(mScroller.getCurrX() + (int) ev.getX(),
						mScroller.getCurrY() + (int) ev.getY()
								- getVertOffset());
				Log.i("Test", "Touch!");
			} else {
				Log.i("Test", "Scroll!");
				mVelocityTracker.addMovement(ev);

				mVelocityTracker.computeCurrentVelocity(1000);

				int vx = (int) mVelocityTracker.getXVelocity();
				int vy = (int) mVelocityTracker.getYVelocity();

				vx = vx * 3 / 4;
				vy = vy * 3 / 4;

				mScroller.fling(mScroller.getCurrX(), mScroller.getCurrY(),
						-vx, -vy, 0, getScrollWidth(), 0, getScrollHeight());

				mVelocityTracker.recycle();

				return true;
			}
		}

		}
		return true;
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent msg) {

    	Log.d("Test", "Press");
    	
    	mRender.onKeyDown(keyCode);
    	
    	return true;
    }
	
	
	private final int getVertOffset() {
		return 0;
		// if (mRender.getHeight() >= this.getHeight())
		// return 0;
		// else
		// return (this.getHeight() - mRender.getHeight()) / 2;
	}

	private final int getScrollWidth() {
		return mRender.getWidth() - this.getWidth();
	}

	private final int getScrollHeight() {
		return mRender.getHeight() - this.getHeight() - getVertOffset();
	}
}
