package com.comapping.android.view;

import com.comapping.android.Options;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Scroller;
import android.widget.ZoomControls;

public class MainMapView extends View {

	private static final float MAX_SCALE = 1.0f;
	private static final float MIN_SCALE = 0.5f;
	private static final long TIME_TO_HIDE = 2000;

	private static final int SCROLLBAR_WIDTH = 4;
	private static final int SCROLLBAR_LINE_LEN = 15;

	public MapRender mRender;
	public Scroller mScroller;

	private Paint scrollBarBackgroundPaint = new Paint();
	private Paint scrollBarPaint = new Paint();

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

	public MainMapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		scrollBarBackgroundPaint.setColor(Color.GRAY);
		scrollBarBackgroundPaint.setAlpha(127);

		scrollBarPaint.setColor(Color.GRAY);

		setFocusable(true);
		mScroller = new Scroller(context);
	}

	public void setRender(MapRender render) {
		mRender = render;
		mRender.setScrollController(scrollController);
	}

	public void setZoom(ZoomControls zoom) {
		this.zoom = zoom;
		zoom.hide();
		zoomVisible = false;
		scale = MAX_SCALE;
		zoom.setIsZoomInEnabled(false);
		zoom.setOnZoomInClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setScale(getScale() + 0.1f);
				lastZoomPress = System.currentTimeMillis();
			}
		});
		zoom.setOnZoomOutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setScale(getScale() - 0.1f);
				lastZoomPress = System.currentTimeMillis();
			}
		});
	}

	int frameCount = 0;
	long fps = 0;
	long lastFPSCalcTime = System.currentTimeMillis();
	private ZoomControls zoom;
	private float scale;
	private long lastZoomPress = -100000;
	private boolean zoomVisible;

	public void setVisible() {
		lastZoomPress = System.currentTimeMillis();
		zoomVisible = true;
	}

	public float getScale() {
		return scale;
	}

	private static final float eps = 1e-6f;

	public void setScale(float scale) {
		if (scale > MAX_SCALE)
			scale = MAX_SCALE;
		if (scale < MIN_SCALE)
			scale = MIN_SCALE;
		zoom.setIsZoomInEnabled(Math.abs(scale - MAX_SCALE) > eps);
		zoom.setIsZoomOutEnabled(Math.abs(scale - MIN_SCALE) > eps);
		this.scale = scale;
		mRender.update();
	}

	boolean isDrawing = false;

	private void refresh() {
		while (!isDrawing)
			;
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		isDrawing = false;

		Paint p = new Paint();
		canvas.drawARGB(255, 255, 255, 255);

		canvas.save();
		canvas.scale(scale, scale);

		mRender.draw(mScroller.getCurrX(), -getVertOffset()
				+ mScroller.getCurrY(), (int) (this.getWidth() / scale)
				- SCROLLBAR_WIDTH, (int) (this.getHeight() / scale)
				- SCROLLBAR_WIDTH, canvas);

		canvas.restore();

		drawScrollBars(canvas);

		if (Options.DEBUG_RENDERING) {
			p.setColor(Color.BLACK);
			canvas.drawText("FPS: " + fps, 20, 30, p);
			canvas.drawText("Width: " + mRender.getWidth(), 20, 50, p);
			canvas.drawText("Height: " + mRender.getHeight(), 20, 70, p);
		}

		if (System.currentTimeMillis() - lastZoomPress > TIME_TO_HIDE) {
			if (zoomVisible) {
				zoom.hide();
				zoomVisible = false;
			}
		}

		isDrawing = true;

		if (mScroller.computeScrollOffset())
			invalidate();

		if (Options.DEBUG_RENDERING) {
			if (System.currentTimeMillis() - lastFPSCalcTime > 1000) {
				fps = (1000 * frameCount)
						/ (System.currentTimeMillis() - lastFPSCalcTime);
				lastFPSCalcTime = System.currentTimeMillis();
				frameCount = 0;
			}
			frameCount++;
		}
	}

	void drawScrollBars(Canvas c) {
		// Horizontal
		c.drawRect(0, getHeight() - SCROLLBAR_WIDTH, getWidth(), getHeight(),
				scrollBarBackgroundPaint);

		int horLen = getWidth() - SCROLLBAR_LINE_LEN;
		float horLinePos = (float) mScroller.getCurrX()
				/ (float) (mRender.getWidth() - this.getWidth());

		c.drawRect(horLen * horLinePos, getHeight() - SCROLLBAR_WIDTH, horLen
				* horLinePos + SCROLLBAR_LINE_LEN, getHeight(), scrollBarPaint);

		// Vertical
		// Not "getHeight()"!!
		// Should be "getHeight() - SCROLLBAR_WIDTH" because of intersection
		c.drawRect(getWidth() - SCROLLBAR_WIDTH, 0, getWidth(), getHeight()
				- SCROLLBAR_WIDTH, scrollBarBackgroundPaint);

		int vertLen = getHeight() - SCROLLBAR_LINE_LEN;
		float vertLinePos = (float) mScroller.getCurrY()
				/ (float) ( mRender.getHeight() - this.getHeight());

		c.drawRect(getWidth() - SCROLLBAR_WIDTH, vertLen * vertLinePos,
				getWidth(), vertLen * vertLinePos + SCROLLBAR_LINE_LEN,
				scrollBarPaint);

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

				refresh();
			}
			return true;
		}
		case MotionEvent.ACTION_UP: {
			long timeDelta = System.currentTimeMillis() - touchStartTime;
			int pathLen = (startX - (int) ev.getX())
					* (startX - (int) ev.getX()) + (startY - (int) ev.getY())
					* (startY - (int) ev.getY());

			if ((timeDelta < TAP_MAX_TIME) && (pathLen < BLOCK_PATH_LEN)) {
				mRender.onTouch(mScroller.getCurrX()
						+ (int) (ev.getX() / scale), mScroller.getCurrY()
						+ (int) (ev.getY() / scale) - getVertOffset());
				refresh();
			} else {
				mVelocityTracker.addMovement(ev);

				mVelocityTracker.computeCurrentVelocity(1000);

				int vx = (int) mVelocityTracker.getXVelocity();
				int vy = (int) mVelocityTracker.getYVelocity();

				vx = vx * 3 / 4;
				vy = vy * 3 / 4;

				mScroller.fling(mScroller.getCurrX(), mScroller.getCurrY(),
						-vx, -vy, 0, getScrollWidth(), 0, getScrollHeight());

				mVelocityTracker.recycle();

				refresh();

				return true;
			}
		}

		}
		return true;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent msg) {

		// Back button fix
		if (keyCode == KeyEvent.KEYCODE_BACK)
			return false;

		Log.d("Test", "Press");

		mRender.onKeyDown(keyCode);
		refresh();
		return false;
	}

	private final int getVertOffset() {
		return 0;
		// if (mRender.getHeight() >= this.getHeight())
		// return 0;
		// else
		// return (this.getHeight() - mRender.getHeight()) / 2;
	}

	private final int getScrollWidth() {
		return mRender.getWidth() - (int) (this.getWidth() / scale);
	}

	private final int getScrollHeight() {
		return mRender.getHeight() - (int) (this.getHeight() / scale)
				- getVertOffset();
	}
}
