package com.comapping.android.model.map.builder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.comapping.android.provider.communication.ClientHelper.getTextFromInputStream;
import static com.comapping.android.TestHelper.mapSimpleEquals;
import com.comapping.android.map.model.exceptions.MapParsingException;
import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.map.model.map.Flag;
import com.comapping.android.map.model.map.Icon;
import com.comapping.android.map.model.map.Map;
import com.comapping.android.map.model.map.Smiley;
import com.comapping.android.map.model.map.TaskCompletion;
import com.comapping.android.map.model.map.Topic;
import com.comapping.android.map.model.map.builder.MapBuilder;
import com.comapping.android.map.model.map.builder.SaxMapBuilder;

import android.test.AndroidTestCase;

public class MapBuilderTest extends AndroidTestCase {

	private Map getMapFromSD(String path) throws FileNotFoundException,
			StringToXMLConvertionException, MapParsingException, IOException {
		MapBuilder mapBuilder = new SaxMapBuilder();
		return mapBuilder.buildMap(getTextFromInputStream(new FileInputStream(
				path)));
	}

	public void test0() throws StringToXMLConvertionException,
			MapParsingException, IOException {
		Map map;
		try {
			map = getMapFromSD("sdcard\\test0.comap");
		} catch (FileNotFoundException e) {
			return;
		}
		Map mapE = new Map(62037);
		Topic topic1 = new Topic(null);
		topic1.setText("test0");
		mapE.setRoot(topic1);
		assertTrue(mapSimpleEquals(map, mapE));
	}

	public void test1() throws StringToXMLConvertionException,
			MapParsingException, IOException {
		Map map;
		try {
			map = getMapFromSD("sdcard\\test1.comap");
		} catch (FileNotFoundException e) {
			return;
		}
		Map mapE = new Map(61975);
		Topic topic1 = new Topic(null);
		topic1.setText("test1");
		Topic topic2 = new Topic(topic1);
		topic2.setText("Topic");
		topic2.setFlag(Flag.RISK);
		mapE.setRoot(topic1);
		topic1.addChild(topic2);
		assertTrue(mapSimpleEquals(map, mapE));
	}

	public void test2() throws StringToXMLConvertionException,
			MapParsingException, IOException {
		Map map;
		try {
			map = getMapFromSD("sdcard\\test2.comap");
		} catch (FileNotFoundException e) {
			return;
		}
		Map mapE = new Map(62041);
		Topic topic1 = new Topic(null);
		topic1.setText("test2");
		Topic topicA = new Topic(topic1);
		topicA.setText("a");
		Topic topicB = new Topic(topic1);
		topicB.setText("b");
		Topic topicC = new Topic(topicA);
		topicC.setText("c");
		Topic topicD = new Topic(topicC);
		topicD.setText("d");
		Topic topicE = new Topic(topicC);
		topicE.setText("e");
		mapE.setRoot(topic1);
		topic1.addChild(topicA);
		topic1.addChild(topicB);
		topicA.addChild(topicC);
		topicC.addChild(topicD);
		topicC.addChild(topicE);
		assertTrue(mapSimpleEquals(map, mapE));
	}

	public void test3() throws StringToXMLConvertionException,
			MapParsingException, IOException {
		Map map;
		try {
			map = getMapFromSD("sdcard\\test3.comap");
		} catch (FileNotFoundException e) {
			return;
		}
		Map mapE = new Map(62043);
		Topic topic1 = new Topic(null);
		topic1.setText("test3");
		Topic topic = new Topic(topic1);
		topic.setText("topic");
		topic.setPriority(1);
		topic.setSmiley(Smiley.HAPPY);
		topic.setTaskCompletion(TaskCompletion.FIFTY);
		topic.addIcon(Icon.DOLLAR);
		topic.addIcon(Icon.HEART);
		mapE.setRoot(topic1);
		topic1.addChild(topic);
		assertTrue(mapSimpleEquals(map, mapE));
	}

	public void test4() throws StringToXMLConvertionException,
			MapParsingException, IOException {
		Map map;
		try {
			map = getMapFromSD("sdcard\\test4.comap");
		} catch (FileNotFoundException e) {
			return;
		}
		Map mapE = new Map(62043);
		Topic topic1 = new Topic(null);
		topic1.setText("test4");
		Topic topic = new Topic(topic1);
		topic.setText("t");
		topic.setNote("qqqqq");
		mapE.setRoot(topic1);
		topic1.addChild(topic);
		assertTrue(mapSimpleEquals(map, mapE));
	}

	public void testAllFiles() throws StringToXMLConvertionException,
			MapParsingException, IOException {
		Map map;
		map = getMapFromSD("sdcard\\test0.comap");
		map = getMapFromSD("sdcard\\test1.comap");
		map = getMapFromSD("sdcard\\test2.comap");
		map = getMapFromSD("sdcard\\test3.comap");
		map = getMapFromSD("sdcard\\test4.comap");
	}

}
