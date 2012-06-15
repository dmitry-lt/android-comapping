package com.lanit_tercom.comapping.android.map.render.topic;

import android.content.Context;

import com.lanit_tercom.comapping.android.map.model.map.Topic;

public class CachedTopicRender {
	private static final int CACHE_SIZE = 2;
	
	private static class CacheItem {
		public TopicRender topicRender;
		public int maxWidth;
		
		public CacheItem(int maxWidth, TopicRender topicRender) {
			this.maxWidth = maxWidth;
			this.topicRender = topicRender;
		}
	}
			
	private CacheItem[] cache = new CacheItem[CACHE_SIZE];
	private TopicRender curTopicRender;
	
	private Topic topic;
	private Context context;
	
	public CachedTopicRender(Topic topic, Context context) {
		this.topic = topic;
		this.context = context;
		curTopicRender = new TopicRender(topic, context); 
	}
	
	public TopicRender getCurTopicRender() {
		return curTopicRender;
	}
	
	public void setMaxWidth(int maxWidth) {
		precalcMaxWidthSetting(maxWidth);
		cache[CACHE_SIZE - 1].topicRender.setSelected(curTopicRender.isSelected());
		curTopicRender = cache[CACHE_SIZE - 1].topicRender;
	}
	
	synchronized public void precalcMaxWidthSetting(int maxWidth) {
		for (int i = 0; i < CACHE_SIZE; i++) {
			if (cache[i] != null && cache[i].maxWidth == maxWidth) {
				CacheItem item = cache[i];
				cache[i] = cache[CACHE_SIZE - 1];
				cache[CACHE_SIZE - 1] = item;
				return;
			}
		}

		System.arraycopy(cache, 1, cache, 0, CACHE_SIZE - 1);

		TopicRender topicRender = new TopicRender(topic, context);
		topicRender.setMaxWidth(maxWidth);
		cache[CACHE_SIZE - 1] = new CacheItem(maxWidth, topicRender);
	}

}
