package com.comapping.android.model.map.builder;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.comapping.android.provider.communication.ClientHelper.getTextFromInputStream;
import com.comapping.android.model.exceptions.MapParsingException;
import com.comapping.android.model.exceptions.StringToXMLConvertionException;
import com.comapping.android.model.map.Flag;
import com.comapping.android.model.map.Map;
import com.comapping.android.model.map.Topic;

import android.content.Context;
import android.content.res.Resources;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class MapBuilderTest extends AndroidTestCase {
	
	private Context context;
	@SmallTest
	public void test1() throws StringToXMLConvertionException, MapParsingException, FileNotFoundException, IOException
	{
		MapBuilder mapBuilder = new SaxMapBuilder();
		try
		{
		Resources r = context.getResources();
		InputStream stream = r.openRawResource(com.comapping.android.tests.R.raw.test1);//your project place of R file
		Map map = mapBuilder.buildMap(getTextFromInputStream(stream));
		Map mapE = new Map(61975);
		Topic topic1 = new Topic(null);
		topic1.setText("test1");
		Topic topic2 = new Topic(topic1);
		topic2.setText("Topic");
		topic2.setFlag(Flag.RISK);
		mapE.setRoot(topic1);
		topic1.addChild(topic2);
		assertTrue(map.simpleEquals(mapE));
		}catch(NullPointerException ex){};
	}
	
}
