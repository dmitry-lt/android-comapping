package com.comapping.android.metamap.provider;

import java.util.Comparator;

import com.comapping.android.metamap.MetaMapItem;

public abstract class MetaMapProvider {
	
	protected class MetaMapItemComparator implements Comparator<MetaMapItem> {

		
		public int compare(MetaMapItem topic1, MetaMapItem topic2) {
			if ((topic1.isFolder && topic2.isFolder) || 
				(!topic1.isFolder && !topic2.isFolder)) {
				// if both folder or both maps we compare texts
				return topic1.name.compareTo(topic2.name);
			} else {
				if (topic1.isFolder) {
					return -1;
				} else {
					return 1;
				}
			}
		}
	}
	
	public abstract void goUp();
	public abstract void goHome();
	public abstract MetaMapItem[] getCurrentLevel();
	public abstract void gotoFolder(int index);
	
	public abstract boolean canGoUp();
	public abstract boolean canGoHome();
	
	public abstract boolean canSync();
	public abstract boolean sync();
	
	public abstract boolean canLogout();
	public abstract void logout();
	
	public abstract String getEmptyListText();
}
