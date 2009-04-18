package com.comapping.android.view;

public abstract class MapRender extends Render {
	public abstract void setScrollController(ScrollController scroll);
	public abstract void onKeyDown(int keyCode);
	public abstract void update();
}
