package com.comapping.android.metamap;

public abstract class MetaMapProvider {
	public abstract void goUp();
	public abstract void goHome();
	public abstract MetaMapListAdapter.MetaMapItem[] getCurrentLevel();
	public abstract void gotoFolder(int index);
	
	public abstract boolean canGoUp();
	public abstract boolean canGoHome();
	
	public abstract boolean canSync();
	public abstract boolean sync();
}
