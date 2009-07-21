package com.comapping.android.model.text.builder;

import java.util.List;

import com.comapping.android.map.model.exceptions.StringToXMLConvertionException;

import com.comapping.android.map.model.text.TextParagraph;
import com.comapping.android.map.model.text.FormattedText;
import com.comapping.android.map.model.text.TextFormat;
import com.comapping.android.map.model.text.builder.FormattedTextSaxBuilder;

import android.graphics.Color;
import android.test.AndroidTestCase;
import android.test.suitebuilder.annotation.SmallTest;

public class FormattedTextSaxBuilderTest extends AndroidTestCase {

	String text = "Hello world!";
	
	@SmallTest
	public void testEmptyString() throws StringToXMLConvertionException {
		assertEquals("Error while parsing empty text", "",
				FormattedTextSaxBuilder.buildFormattedText("").getSimpleText());
	}

	// @SuppressWarnings("static-access")
	@SmallTest
	public void testTPF() throws StringToXMLConvertionException {
		String xmlTestString = "<TEXT><P><FONT>Hello world!</FONT></P></TEXT>";
	    FormattedText formattedText = FormattedTextSaxBuilder
					.buildFormattedText(xmlTestString);
	    assertEquals("Error:String is not right format", text,
				formattedText.getSimpleText());
	}
	
	@SmallTest
	public void testPF() throws StringToXMLConvertionException{
		String xmlTestString = "<P><FONT>Hello world!</FONT></P>";
	    FormattedText formattedText = FormattedTextSaxBuilder
					.buildFormattedText(xmlTestString);
	    assertEquals("Error:String is not right format", text,
				formattedText.getSimpleText());
	}
	
	@SmallTest
	public void testF() throws StringToXMLConvertionException{
		String xmlTestString = "<FONT>Hello world!</FONT>";
	    FormattedText formattedText = FormattedTextSaxBuilder
					.buildFormattedText(xmlTestString);
	    assertEquals("Error:String is not right format", text,
				formattedText.getSimpleText());
	}
	
	@SmallTest
	public void testString() throws StringToXMLConvertionException{
		String xmlTestString = "Hello world!"; 
	    FormattedText formattedText = FormattedTextSaxBuilder
					.buildFormattedText(xmlTestString);
	    assertEquals("Error:String is not right format", text,
				formattedText.getSimpleText());
	}
	
	@SmallTest
	public void testFalseString() throws StringToXMLConvertionException{
		String xmlTestString = "<PHello World!";
	    FormattedText formattedText = FormattedTextSaxBuilder
					.buildFormattedText(xmlTestString);
		assertFalse("Error:String is not right format",text ==
		    formattedText.getSimpleText());
	}
	
	@SmallTest
	public void testFont() throws StringToXMLConvertionException {
		String xmlTestString = "<FONT SIZE=\"16\" COLOR=\"#000000\" LETTERSPACING=\"0\" KERNING=\"1\">Hello, world!</FONT>";
		FormattedText formattedText = FormattedTextSaxBuilder
		.buildFormattedText(xmlTestString);
		assertEquals("Error with parsing text", "Hello, world!", formattedText
				.getSimpleText());
		List<TextParagraph> paragraphs = formattedText.getTextParagraphs();
		assertEquals("Error with number of text paragraphs", 1, paragraphs
				.size());
		assertEquals("Error with number of text blocks", 1, paragraphs.get(0)
				.getTextBlocks().size());
		TextFormat format = paragraphs.get(0).getTextBlocks().get(0)
				.getFormat();
		assertEquals("Error with format - size", 16, format.getFontSize());
		assertEquals("Error with format - color", Color.BLACK, format
				.getFontColor());
		assertEquals("Error with format - underling", false, format
				.isUnderlined());
	}
	
	@SmallTest
	public void testTextWithLink() throws StringToXMLConvertionException
	{
		String xmlTestString = "Text <A HREF=\"http://www.google.ru/search?hl=ru&q=O%28n+%5E+k%29&lr=&aq=f&oq=\" TARGET=\"_blank\">endtext</A>";
		FormattedText formattedText = FormattedTextSaxBuilder.buildFormattedText(xmlTestString);
	}

}
