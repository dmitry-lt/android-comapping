package com.lanit_tercom.comapping.android.map.render.explorer;

public class Expander implements Comparable<Expander> {
	public int x, y;
	public TopicView topic;

	public Expander(int x, int y, TopicView topic) {
		this.x = x;
		this.y = y;
		this.topic = topic;
	}

	
	public int compareTo(Expander another) {
		if (this.y < another.y) {
			return -1;
		} else if (this.y == another.y) {
			return 0;
		} else {
			return 1;
		}
	}
}

