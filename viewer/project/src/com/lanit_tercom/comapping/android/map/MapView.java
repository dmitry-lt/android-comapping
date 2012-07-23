package com.lanit_tercom.comapping.android.map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.*;
import android.widget.*;
import com.lanit_tercom.comapping.android.Log;
import com.lanit_tercom.comapping.android.Options;
import com.lanit_tercom.comapping.android.R;
import com.lanit_tercom.comapping.android.map.model.map.Topic;
import com.lanit_tercom.comapping.android.map.render.MapRender;

import java.util.ArrayList;

public class MapView extends View {

	// Zoom constants
	public static final float MAX_SCALE = 2.0f;
	public static final float MIN_SCALE = 0.4f;

	// Taping constants
	private static final long TAP_MAX_TIME = 500;
	private static final long BLOCK_PATH_LEN = 1000;

	// Scrollbar constants
	private static final int SCROLLBAR_WIDTH = 4;
	private static final int SCROLLBAR_LINE_LEN = 15;

	private static final int SCROLL_BOTTOM_OFFSET = 100;

	// Drawing variables
	public MapRender mRender;
	public Scroller mScroller;
	public Context context;

	private Paint scrollBarBackgroundPaint = new Paint();
	private Paint scrollBarPaint = new Paint();

	private boolean isDrawing = false;
	private boolean isInitialized = false;
	private boolean needChangeSize = true;

	private Drawable background;

	// Debug variables

	int frameCount = 0;
	long fps = 0;
	long lastFPSCalcTime = System.currentTimeMillis();

	// Zooming variables
	private float scale = MAX_SCALE;
	private ZoomControls zoom;
	private ScaleGestureDetector mScaleGestureDetector;

	// used for offsetting canvas relative focus point on zoom in onDraw method instead mScroll.startScroll(...)
	private int delayedOffsetX = 0;
	private int delayedOffsetY = 0;

	// Scrolling variables

	ScrollController scrollController = new ScrollController() {
		@Override
		public void intermediateScroll(int dx, int dy) {
			mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), fixXOffset(dx), fixYOffset(dy), 0);
			mScroller.computeScrollOffset();
		}

		@Override
		public void smoothScroll(int dx, int dy) {
			mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(), fixXOffset(dx), fixYOffset(dy));
		}
	};

	// Taping variables
	VelocityTracker mVelocityTracker;
	int oldX = -1, oldY = -1;
	int startX = -1, startY = -1;
	long touchStartTime = 0;
	boolean fixedScroll = true;

	// Search variables
	ImageButton cancel, next, prev;
	TextView queryLabel;
	String query;
	LinearLayout findLayout;

	// ===============================================
	// Zoom
	// ===============================================

	private static final long ZOOM_CONTROLS_TIME_TO_HIDE = 2000;

	private long lastZoomPress = 0;

	private Thread zoomUpdateThread = new Thread() {
		@Override
		public void run() {
			// Zoom code
			while (true) {
				if (System.currentTimeMillis() - lastZoomPress > ZOOM_CONTROLS_TIME_TO_HIDE) {
					hideZoom();
				}
				try {
					sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

	};

	private static final float eps = 1e-6f;

	public void setScale(float scale) {
		scale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, scale));
		zoom.setIsZoomInEnabled(Math.abs(scale - MAX_SCALE) > eps);
		zoom.setIsZoomOutEnabled(Math.abs(scale - MIN_SCALE) > eps);
		int oldW = getScreenForRenderWidth();
		int oldH = getScreenForRenderHeight();
		this.scale = scale;
		int newW = getScreenForRenderWidth();
		int newH = getScreenForRenderHeight();
		delayedOffsetX = fixXOffset((oldW - newW)/2);
		delayedOffsetY = fixYOffset((oldH - newH)/2);

		showZoom();
	}

	public void setScale(float scale, float focusX, float focusY) {
		scale = Math.min(MAX_SCALE, Math.max(MIN_SCALE, scale));
		zoom.setIsZoomInEnabled(Math.abs(scale - MAX_SCALE) > eps);
		zoom.setIsZoomOutEnabled(Math.abs(scale - MIN_SCALE) > eps);

		int oldW = getScreenForRenderWidth();
		int oldH = getScreenForRenderHeight();
		this.scale = scale;
		int newW = getScreenForRenderWidth();
		int newH = getScreenForRenderHeight();

		// calculate actual scale
		float scaleX = (float) newW / (float) oldW;
		float scaleY = (float) newH / (float) oldH;
		// calculate coordinates of the focus point after transformation
		int focusNewX = (int) (focusX * scaleX);
		int focusNewY = (int) (focusY * scaleY);
		// move coordinate system
		delayedOffsetX = fixXOffset((int) (focusX - focusNewX));
		delayedOffsetY = fixYOffset((int) (focusY - focusNewY));

		showZoom();
	}

	public void setZoom(ZoomControls zoom) {
		this.zoom = zoom;
	}

	public void showZoom() {

		lastZoomPress = System.currentTimeMillis();

		if (zoom == null)
			return;


		if (zoom.getVisibility() != View.VISIBLE) {
			((Activity) getContext()).runOnUiThread(new Runnable() {

				public void run() {

					zoom.show();
				}
			});
		}
	}

	public void hideZoom() {
		if (zoom == null)
			return;

		if (zoom.getVisibility() == View.VISIBLE) {
			((Activity) getContext()).runOnUiThread(new Runnable() {

				public void run() {
					zoom.hide();
				}
			});
		}
	}

	public MapView(Context context, AttributeSet attrs) {
		super(context, attrs);

		initScrolling(context);
		setFocusable(true);

		background = context.getResources().getDrawable(
				R.drawable.map_background);
		background.setBounds(0, 0, background.getIntrinsicWidth(), background
				.getIntrinsicHeight());
		background.setAlpha(127);

		zoomUpdateThread.start();

		requestFocus();

		mScaleGestureDetector = new ScaleGestureDetector(context, new ScaleGestureListener(this));
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

	public void showSearchUI() {
		findLayout.setVisibility(View.VISIBLE);
	}

	public void hideSearchUI() {
		findLayout.setVisibility(View.INVISIBLE);
	}

	private ArrayList<Topic> findTopics = new ArrayList<Topic>();
	private int selectedSearchResult = 0;

	public void setSearchUI(LinearLayout findLayout, ImageButton cancel,
							ImageButton next, ImageButton prev, TextView queryLabel) {
		this.cancel = cancel;
		this.next = next;
		this.prev = prev;
		this.queryLabel = queryLabel;
		this.findLayout = findLayout;

		this.cancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				hideSearchUI();
			}
		});

		this.next.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				selectedSearchResult = (selectedSearchResult + 1)
						% findTopics.size();

				if (findTopics.size() > 0)
					mRender.selectTopic(findTopics.get(selectedSearchResult));

				updateLabel();
			}
		});

		this.prev.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				selectedSearchResult = (selectedSearchResult
						+ findTopics.size() - 1)
						% findTopics.size();

				if (findTopics.size() > 0)
					mRender.selectTopic(findTopics.get(selectedSearchResult));

				updateLabel();
			}
		});

		hideSearchUI();
	}

	public void onSearch(ArrayList<Topic> findTopics, String query) {

		this.findTopics = findTopics;
		this.query = query;
		selectedSearchResult = 0;
		if (findTopics.size() > 0) {
			next.setEnabled(true);
			prev.setEnabled(true);
			mRender.selectTopic(findTopics.get(selectedSearchResult));
		} else {
			next.setEnabled(false);
			prev.setEnabled(false);
		}
		updateLabel();
		showSearchUI();
	}

	private void updateLabel() {
		if (findTopics.size() > 0) {
			queryLabel.setText((selectedSearchResult + 1) + "\\"
					+ findTopics.size() + "\n" + query);
		} else {
			queryLabel.setText(R.string.MapActivityNothingFound);
		}

	}

	public boolean isInitialized() {
		return isInitialized;
	}

	public float getScale() {
		return scale;
	}

	public void refresh() {
		while (!isDrawing) {

		}

		invalidate();
	}

	private MapActivity activity;

	public void setActivity(MapActivity activity) {
		this.activity = activity;
	}

	private boolean isFinished = false;

	@Override
	protected void onDraw(Canvas canvas) {

		if (!activity.canDraw()) {
			new Thread() {
				@Override
				public void run() {
					isFinished = false;
					while (!activity.canDraw()) {
						try {
							sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					isFinished = true;
					postInvalidate();
				}
			}.start();
		} else {
			isFinished = true;
		}
		if (!isFinished) {
			return;
		}

		isDrawing = false;

		if (needChangeSize) {
			final View mainMapView = this;
			new Thread(new Runnable() {

				public void run() {
					mRender.onChangeSize();
					mRender.setBounds(getScreenForRenderWidth(),
							getScreenForRenderHeight());
					needChangeSize = false;
					mainMapView.postInvalidate();
				}
			}).start();
		} else {
			mRender.setBounds(getScreenForRenderWidth(),
					getScreenForRenderHeight());
		}

		if (needChangeSize) {
			return;
		}

		// fixScrolCoordinates();

		// Save matrix
		canvas.save();

		// Clear screen

		// canvas.drawARGB(255, 213, 255, 251);
		canvas.drawARGB(255, 255, 255, 255);

		// Draw logo
		// Rect size = background.getBounds();
		// canvas.translate(this.getWidth() - size.right, this.getHeight() -
		// size.bottom);
		// background.draw(canvas);

		// Scaling
		canvas.restore();
		canvas.scale(scale, scale);

		// Draw map
		Log.d("Map", "Draw");
		int x = mScroller.getCurrX() + delayedOffsetX;
		int y = mScroller.getCurrY() + delayedOffsetY;
		if (delayedOffsetX != 0) {
			mScroller.setFinalX(x);
		}
		if (delayedOffsetY != 0) {
			mScroller.setFinalY(y);
		}
		mRender.draw(x, y,
				getScreenForRenderWidth(), getScreenForRenderHeight(), canvas);
		delayedOffsetX = 0;
		delayedOffsetY = 0;

		// Restore matrix
		canvas.restore();

		// Draw scrollbars
		drawScrollBars(canvas);

		if (Options.DEBUG_RENDERING) {
			drawDebugInfo(canvas);
		}

		isDrawing = true;
		isInitialized = true;

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
		mScaleGestureDetector.onTouchEvent(ev);

		switch (ev.getAction()) {
			case MotionEvent.ACTION_DOWN: {

				mVelocityTracker = VelocityTracker.obtain();
				oldX = (int) ev.getX();
				oldY = (int) ev.getY();
				startX = oldX;
				startY = oldY;

				touchStartTime = System.currentTimeMillis();
				fixedScroll = true;
				break;
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
				break;
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
				}
				break;
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
		if (mRender == null)
			return 0;

		return mRender.getWidth() - (int) (this.getWidth() / scale)
				+ SCROLLBAR_WIDTH;
	}

	private final int getScrollHeight() {
		if (mRender == null)
			return 0;

		return mRender.getHeight() - (int) (this.getHeight() / scale)
				+ SCROLLBAR_WIDTH + SCROLL_BOTTOM_OFFSET;
	}

	private final int getScreenForRenderWidth() {
		return (int) (this.getWidth() / scale) - SCROLLBAR_WIDTH;
	}

	private final int getScreenForRenderHeight() {
		return (int) (this.getHeight() / scale) - SCROLLBAR_WIDTH;
	}

	void fixScrolCoordinates() {
		int deltaX = 0, deltaY = 0;
		if (mScroller.getCurrX() + deltaX > getScrollWidth())
			deltaX = getScrollWidth() - mScroller.getCurrX();
		if (mScroller.getCurrY() + deltaY > getScrollHeight())
			deltaY = getScrollHeight() - mScroller.getCurrY();

		if (mScroller.getCurrX() + deltaX < 0)
			deltaX = -mScroller.getCurrX();
		if (mScroller.getCurrY() + deltaY < 0)
			deltaY = -mScroller.getCurrY();

		mScroller.startScroll(mScroller.getCurrX(), mScroller.getCurrY(),
				deltaX, deltaY, 0);
	}

	/**
	 * Returns fixed distance to scroll by x axis: if dx out of scrolling rectangle's bounds - returned closest bound
	 *
	 * @param dx distance to scroll by x axis
	 * @return dx if it in scrolling rectangle's bounds else - closest bound
	 */
	private int fixXOffset(int dx) {
		int currX = mScroller.getCurrX();
		int maxDx = getScrollWidth() - currX;
		dx = maxDx < dx ? maxDx : dx;
		dx = currX + dx < 0 ? -currX : dx;
		return dx;
	}

	/**
	 * Returns fixed distance to scroll by y axis: if dy out of scrolling rectangle's bounds - returned closest bound
	 *
	 * @param dy distance to scroll by y axis
	 * @return dy if it in scrolling rectangle's bounds else - closest bound
	 */
	private int fixYOffset(int dy) {
		int currY = mScroller.getCurrY();
		int maxDy = getScrollHeight() - currY;
		dy = maxDy < dy ? maxDy : dy;
		dy = currY + dy < 0 ? -currY : dy;
		return dy;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		fixScrolCoordinates();
	}

	public void onRotate() {
		needChangeSize = true;
		isInitialized = false;
	}
}
