package com.lanit_tercom.comapping.android.map.render;

import com.lanit_tercom.comapping.android.map.ScrollController;
import com.lanit_tercom.comapping.android.map.model.map.Topic;

public abstract class MapRender extends Render {
	public abstract void setScrollController(ScrollController scroll);
	public abstract void onKeyDown(int keyCode);
	public abstract void setBounds(int width, int height);
	public abstract void selectTopic(Topic topic);
	public abstract boolean canRotate();
	public abstract void onChangeSize();
}
