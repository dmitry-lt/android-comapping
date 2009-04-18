package com.comapping.android.model;

import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Color;

import com.comapping.android.Log;
import com.comapping.android.model.exceptions.DocumentBuilderCreatingError;

public class FormattedTextSaxHandler extends DefaultHandler {

	private Stack<TextFormat> stackTextFormat;
	private FormattedText formattedText;
	private TextParagraph currentTextParagraph;
	
	private TextFormat getDefFormat() {
		TextFormat format = new TextFormat();
		format.setFontSize(16);
		format.setFontColor(Color.BLACK);
		format.setHRef("");
		format.setUnderlined(false);
		return format;
	}
	
	public FormattedTextSaxHandler() {
		stackTextFormat = new Stack<TextFormat>();  
		formattedText = new FormattedText();
	}
	
	public void startDocument() throws SAXException {
		stackTextFormat.push(getDefFormat());
		currentTextParagraph = null;
	}
	
	public void endDocument() throws SAXException {
		if (currentTextParagraph != null) {
			formattedText.add(currentTextParagraph);
		}
		currentTextParagraph = null;
		
		try {
			stackTextFormat.pop();
		}
		catch (EmptyStackException e) {
			Log.e("SAX Parser ", e.toString());
			throw new SAXException();
		}
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		if (currentTextParagraph != null) {
			formattedText.add(currentTextParagraph);
		}
		currentTextParagraph = null;
		
		try {
			TextFormat currentTextFormat;
			
			if (!stackTextFormat.isEmpty()) {
				currentTextFormat = stackTextFormat.lastElement().clone();
			} else {
				currentTextFormat = getDefFormat();
			}
			
			if (localName.equals(FormattedTextSaxBuilder.FONT_TAG)) {
							
				if (attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_SIZE_TAG) != null) {
					int fontSize = Integer.parseInt(attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_SIZE_TAG));
					currentTextFormat.setFontSize(fontSize);
				}
				
				if (attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_COLOR_TAG) != null) {
					int fontColor = Integer.parseInt(attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_COLOR_TAG));
					currentTextFormat.setFontColor(fontColor);
				}
				
			} else if (localName.equals(FormattedTextSaxBuilder.HYPER_REF_TAG)) {	
				String href = attributes.getValue(FormattedTextSaxBuilder.HYPER_REF_ATTR_HREF_TAG);
				currentTextFormat.setHRef(href);
				
			} else if (localName.equals(FormattedTextSaxBuilder.UNDERLINED_TAG)) {
				currentTextFormat.setUnderlined(true);
				
			} else if (localName.equals(FormattedTextSaxBuilder.PARAGRAPH_TAG)) {
				// we should do nothing
			}

			stackTextFormat.push(currentTextFormat);
		}
		catch (NumberFormatException e) {
			Log.e("SAX Parser ", e.toString());
			throw new SAXException();
		}
		catch (NoSuchElementException e) {
			Log.e("SAX Text Parser ", e.toString());
			throw new SAXException();		
		}
	}
	
	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (currentTextParagraph != null) {
			formattedText.add(currentTextParagraph);
		}
		currentTextParagraph = null;
		
		try {
			stackTextFormat.pop();
		}
		catch (EmptyStackException e) {
			Log.e("SAX Text Parser ", e.toString());
			throw new SAXException();	
		}
	}
	
	public void characters(char[] ch, int start, int len) throws SAXException {
		String text = new String(ch, start, len);
		
		try {
			TextFormat currentFormat = stackTextFormat.lastElement();
			if (text != "") {
				if (currentTextParagraph == null) {
					currentTextParagraph = new TextParagraph();
				}
				currentTextParagraph.add(new TextBlock(text, currentFormat));
			}
		}
		catch (NoSuchElementException e) {
			Log.e("SAX Text Parser ", e.toString());
			throw new SAXException();			
		}
	}
	
	public FormattedText getFormattedText() {
		return formattedText;
	}
}
