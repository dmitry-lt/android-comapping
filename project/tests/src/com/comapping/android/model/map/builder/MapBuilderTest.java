package com.comapping.android.model.map.builder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static com.comapping.android.provider.communication.ClientHelper.getTextFromInputStream;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Flag;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;

import android.test.AndroidTestCase;

public class MapBuilderTest extends AndroidTestCase {
	
	public void test1() throws StringToXMLConvertionException, MapParsingException, FileNotFoundException, IOException
	{
		MapBuilder mapBuilder = new SaxMapBuilder();
		Map map = mapBuilder.buildMap(getTextFromInputStream(new FileInputStream("sdcard\\test1.comap")));
		Map mapE = new Map(61975);
		Topic topic1 = new Topic(null);
		topic1.setText("test1");
		Topic topic2 = new Topic(topic1);
		topic2.setText("Topic");
		topic2.setFlag(Flag.RISK);
		mapE.setRoot(topic1);
		topic1.addChild(topic2);
		assertEquals(map.simpleEquals(mapE), true);
	}
	
}
