package com.comapping.android.map;

import android.view.ScaleGestureDetector;

/**
 * @author Grigory Kalabin grigory.kalabin@gmail.com
 */
public class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

	private final MapView mMapView;
	private float mScaleFactor = 1.f;
	public ScaleGestureListener(MapView mMapView) {
		this.mMapView = mMapView;
	}

	@Override
	public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
		mScaleFactor *= scaleGestureDetector.getScaleFactor();
		mScaleFactor = Math.max(MapView.MIN_SCALE,Math.min(MapView.MAX_SCALE,mScaleFactor));
		mMapView.setScale(mScaleFactor);
		return true;
	}
}
