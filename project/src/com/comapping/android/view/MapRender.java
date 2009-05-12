package com.comapping.android.view;

import com.comapping.android.model.map.Topic;

public abstract class MapRender extends Render {
	public abstract void setScrollController(ScrollController scroll);
	public abstract void onKeyDown(int keyCode);
	public abstract void setBounds(int width, int height);
	public abstract void selectTopic(Topic topic);
}
