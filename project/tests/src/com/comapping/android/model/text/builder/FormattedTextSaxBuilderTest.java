package com.comapping.android.model.text.builder;

import java.util.List;

import com.comapping.android.model.exceptions.StringToXMLConvertionException;

import com.comapping.android.model.text.TextParagraph;
import com.comapping.android.model.text.FormattedText;
import com.comapping.android.model.text.TextFormat;


import android.graphics.Color;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class FormattedTextSaxBuilderTest extends AndroidTestCase {
	
	@SmallTest
	public void test0() throws StringToXMLConvertionException {
		assertEquals("Error while parsing empty text", "", FormattedTextSaxBuilder.buildFormattedText("").getSimpleText());
	}
	
	@SuppressWarnings("static-access")
	@SmallTest
	public void test1() throws StringToXMLConvertionException {
		String text = "Hello world!";
		String[] xml = new String[] {
				"<TEXT><P><FONT>Hello world!</FONT></P></TEXT>",
				"<P><FONT>Hello world!</FONT></P>",
				"<FONT>Hello world!</FONT>", "Hello world!",
				"<PHello World!",
				"<FONT>   </FONT>"};
		FormattedTextSaxBuilder builder = new FormattedTextSaxBuilder();
		
		assertEquals("Error:String is not right format",text,builder.buildFormattedText(xml[0]).toString());
		assertEquals("Error:String is not right format",text,builder.buildFormattedText(xml[1]).toString());
		assertEquals("Error:String is not right format",text,builder.buildFormattedText(xml[2]).toString());
		assertEquals("Error:String is not right format",text,builder.buildFormattedText(xml[3]).toString());
		assertFalse("Error:String is not right format",text == builder.buildFormattedText(xml[4]).toString());
		assertTrue("Error:Spaces are also symbols","" != builder.buildFormattedText(xml[5]).toString());
	}


	@SmallTest
	public void test2() throws StringToXMLConvertionException 
	{
		String xmlTestString = "<FONT SIZE=\"16\" COLOR=\"#000000\" LETTERSPACING=\"0\" KERNING=\"1\">Hello, world!</FONT>";
		FormattedText formattedText = FormattedTextSaxBuilder.buildFormattedText(xmlTestString);
		assertEquals("Error with parsing text", "Hello, world!", formattedText.getSimpleText());
		List<TextParagraph> paragraphs = formattedText.getTextParagraphs();
		assertEquals("Error with number of text paragraphs", 1, paragraphs.size());
		assertEquals("Error with number of text blocks", 1, paragraphs.get(0).getTextBlocks().size());
		TextFormat format = paragraphs.get(0).getTextBlocks().get(0).getFormat();
		assertEquals("Error with format - size", 16, format.getFontSize());
		assertEquals("Error with format - color",Color.BLACK, format.getFontColor());
		assertEquals("Error with format - underling", false, format.isUnderlined());
	}

}
