package com.comapping.android.view;

import java.util.ArrayList;

import com.comapping.android.Options;
import com.comapping.android.model.Topic;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Scroller;
import android.widget.ZoomControls;
import android.widget.AdapterView.OnItemClickListener;

public class MainMapView extends View {

	// Zoom constants
	private static final float MAX_SCALE = 1.0f;
	private static final float MIN_SCALE = 0.5f;
	private static final long TIME_TO_HIDE = 2000;

	// Taping constants
	private static final long TAP_MAX_TIME = 500;
	private static final long BLOCK_PATH_LEN = 1000;

	// Scrollbar constants
	private static final int SCROLLBAR_WIDTH = 4;
	private static final int SCROLLBAR_LINE_LEN = 15;

	// Drawing variables
	public MapRender mRender;
	public Scroller mScroller;
	public Context context;

	private Paint scrollBarBackgroundPaint = new Paint();
	private Paint scrollBarPaint = new Paint();

	private boolean isDrawing = true;

	// Debug variables

	int frameCount = 0;
	long fps = 0;
	long lastFPSCalcTime = System.currentTimeMillis();

	// Zooming variables
	private ZoomControls zoom;
	private float scale;
	private long lastZoomPress = -100000;
	private boolean zoomVisible;

	// Scrolling variables

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

	// Taping variables
	VelocityTracker mVelocityTracker;
	int oldX = -1, oldY = -1;
	int startX = -1, startY = -1;
	long touchStartTime = 0;
	boolean fixedScroll = true;

	public MainMapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initScrolling(context);
		setFocusable(true);
	}

	void initScrolling(Context context) {
		mScroller = new Scroller(context);

		// Painting
		scrollBarBackgroundPaint.setColor(Color.GRAY);
		scrollBarBackgroundPaint.setAlpha(127);

		scrollBarPaint.setColor(Color.GRAY);
	}

	public void setRender(MapRender render) {
		mRender = render;
		mRender.setScrollController(scrollController);
	}

	private LinearLayout layout;

	// private ArrayList<Topic> findTopics;

	public void setlayout(LinearLayout layout, ImageButton cancel,
			ImageButton next, ImageButton previous,
			final AutoCompleteTextView text, final ArrayList<Topic> topics) {
		this.layout = layout;

		layout.setVisibility(VISIBLE);

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isVisible(INVISIBLE);
			}

		});
		next.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String find = text.getText().toString();
				for (int i = 0; i < topics.size(); i++) {
					if (topics.get(i).getText().equals(find)) {
						mRender.selectTopic(topics.get(i));
					}
				}
			}
		});
		text.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				/*
				 * String find = text.getAdapter().getItem(arg2).toString();
				 * if(!findTopics.isEmpty()) findTopics.clear(); for(int i =
				 * 0;i<topics.size();i++) {
				 * if(topics.get(i).getText().equals(find)) {
				 * findTopics.add(topics.get(i)); } }
				 * mRender.selectTopic(findTopics.get(0));
				 */
			}

		});

	}

	public void isVisible(int visibility) {
		layout.setVisibility(visibility);
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
				refresh();
			}
		});
		zoom.setOnZoomOutClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setScale(getScale() - 0.1f);
				lastZoomPress = System.currentTimeMillis();
				refresh();
			}
		});
	}

	public void setZoomVisible() {
		lastZoomPress = System.currentTimeMillis();
		if (!zoomVisible) {
			zoomVisible = true;
			zoom.show();
		}
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
	}

	private void refresh() {
		while (!isDrawing) {

		}
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas) {
		if (System.currentTimeMillis() - lastZoomPress > TIME_TO_HIDE) {
			if (zoomVisible) {
				zoom.hide();
				zoomVisible = false;
			}
		}
		isDrawing = false;

		// Clear screen
		canvas.drawARGB(255, 255, 255, 255);

		// Save matrix
		canvas.save();

		// Scaling
		canvas.scale(scale, scale);
		mRender
				.setBounds(getScreenForRenderWidth(),
						getScreenForRenderHeight());

		// Draw map
		mRender.draw(mScroller.getCurrX(), mScroller.getCurrY(),
				getScreenForRenderWidth(), getScreenForRenderHeight(), canvas);

		// Restore matrix
		canvas.restore();

		// Draw scrollbars
		drawScrollBars(canvas);

		if (Options.DEBUG_RENDERING) {
			drawDebugInfo(canvas);
		}

		isDrawing = true;

		if (mScroller.computeScrollOffset()) {
			invalidate();
		}

		if (Options.DEBUG_RENDERING) {
			debugFrameTick();
		}
	}

	void debugFrameTick() {
		if (System.currentTimeMillis() - lastFPSCalcTime > 1000) {
			fps = (1000 * frameCount)
					/ (System.currentTimeMillis() - lastFPSCalcTime);
			lastFPSCalcTime = System.currentTimeMillis();
			frameCount = 0;
		}
		frameCount++;
	}

	void drawDebugInfo(Canvas canvas) {
		Paint p = new Paint();
		p.setColor(Color.BLACK);
		canvas.drawText("FPS: " + fps, 20, 30, p);
		canvas.drawText("Width: " + mRender.getWidth(), 20, 50, p);
		canvas.drawText("Height: " + mRender.getHeight(), 20, 70, p);
	}

	void drawScrollBars(Canvas c) {
		// Horizontal
		c.drawRect(0, getHeight() - SCROLLBAR_WIDTH, getWidth(), getHeight(),
				scrollBarBackgroundPaint);

		float horBarAlpha = (float) getScreenForRenderWidth()
				/ (float) mRender.getWidth();
		float horBarLen = horBarAlpha * this.getWidth();
		if (horBarLen < SCROLLBAR_LINE_LEN)
			horBarLen = SCROLLBAR_LINE_LEN;

		float horLen = getWidth() - horBarLen;
		float horLinePos = (float) mScroller.getCurrX()
				/ (float) getScrollWidth();

		c.drawRect(horLen * horLinePos, getHeight() - SCROLLBAR_WIDTH, horLen
				* horLinePos + horBarLen, getHeight(), scrollBarPaint);

		// Vertical
		// Not "getHeight()"!!
		// Should be "getHeight() - SCROLLBAR_WIDTH" because of intersection
		c.drawRect(getWidth() - SCROLLBAR_WIDTH, 0, getWidth(), getHeight()
				- SCROLLBAR_WIDTH, scrollBarBackgroundPaint);

		float vertBarAlpha = (float) getScreenForRenderHeight()
				/ (float) mRender.getHeight();
		float vertBarLen = vertBarAlpha * this.getHeight();
		if (vertBarLen < SCROLLBAR_LINE_LEN)
			vertBarLen = SCROLLBAR_LINE_LEN;

		float vertLen = getHeight() - vertBarLen;
		float vertLinePos = (float) mScroller.getCurrY()
				/ (float) getScrollHeight();

		c.drawRect(getWidth() - SCROLLBAR_WIDTH, vertLen * vertLinePos,
				getWidth(), vertLen * vertLinePos + vertBarLen, scrollBarPaint);

	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {

		switch (ev.getAction()) {
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

			if (pathLen < BLOCK_PATH_LEN) {
				if (timeDelta < TAP_MAX_TIME) {
					mRender.onTouch(mScroller.getCurrX()
							+ (int) (ev.getX() / scale), mScroller.getCurrY()
							+ (int) (ev.getY() / scale));
				} else {
					setZoomVisible();
				}
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
		mRender.onKeyDown(keyCode);
		refresh();
		return false;
	}

	private final int getScrollWidth() {
		return mRender.getWidth() - (int) (this.getWidth() / scale)
				+ SCROLLBAR_WIDTH;
	}

	private final int getScrollHeight() {
		return mRender.getHeight() - (int) (this.getHeight() / scale)
				+ SCROLLBAR_WIDTH;
	}

	private final int getScreenForRenderWidth() {
		return (int) (this.getWidth() / scale) - SCROLLBAR_WIDTH;
	}

	private final int getScreenForRenderHeight() {
		return (int) (this.getHeight() / scale) - SCROLLBAR_WIDTH;
	}
}
