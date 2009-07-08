package com.comapping.android.model.text.builder;

import com.comapping.android.model.exceptions.StringToXMLConvertionException;

import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class FormattedTextSaxBuilderTest extends AndroidTestCase {

	@SuppressWarnings("static-access")
	@SmallTest
	public void TestFormattedText() throws StringToXMLConvertionException {
		String text = "Hello world!";
		String[] xml = new String[] {
				"<TEXT><P><FONT>Hello world!</FONT></P></TEXT>",
				"<P><FONT>Hello world!</FONT></P>",
				"<FONT>Hello world!</FONT>", "Hello world!" };
		FormattedTextSaxBuilder builder = new FormattedTextSaxBuilder();
		
		assertEquals(text,builder.buildFormattedText(xml[0]).toString());
		assertEquals(text,builder.buildFormattedText(xml[1]).toString());
		assertEquals(text,builder.buildFormattedText(xml[2]).toString());
		assertEquals(text,builder.buildFormattedText(xml[3]).toString());
	}
}
