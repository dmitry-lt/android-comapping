package com.comapping.android.metamap;

public abstract class MetaMapProvider {
	public abstract void goUp();
	public abstract void goHome();
	public abstract MetaMapListAdapter.MetaMapItem[] getCurrentLevel();
	public abstract void gotoFolder(int index);
}
