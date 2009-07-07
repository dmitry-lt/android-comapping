package com.comapping.android.metamap.provider;

import com.comapping.android.metamap.MetaMapItem;

public abstract class MetaMapProvider {
	public abstract void goUp();
	public abstract void goHome();
	public abstract MetaMapItem[] getCurrentLevel();
	public abstract void gotoFolder(int index);
	
	public abstract boolean canGoUp();
	public abstract boolean canGoHome();
	
	public abstract boolean canSync();
	public abstract boolean sync();
}
