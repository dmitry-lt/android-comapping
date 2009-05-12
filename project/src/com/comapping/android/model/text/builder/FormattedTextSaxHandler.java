package com.comapping.android.model.text.builder;

import java.util.EmptyStackException;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Color;

import com.comapping.android.Log;
import com.comapping.android.model.text.FormattedText;
import com.comapping.android.model.text.TextBlock;
import com.comapping.android.model.text.TextFormat;
import com.comapping.android.model.text.TextParagraph;

public class FormattedTextSaxHandler extends DefaultHandler {

	private Stack<TextFormat> stackTextFormat;
	private FormattedText formattedText;
	private TextParagraph currentTextParagraph;
	private TextFormat currentTextFormat;
	private String currentText;
	
	private void refreshParagraph(){
		if (currentTextParagraph != null || currentText != "") {
			if (currentTextParagraph == null) {
				currentTextParagraph = new TextParagraph();
			}
			if (currentText != "") {
				currentTextParagraph.add(new TextBlock(currentText, currentTextFormat));
			}
		}
		currentText = "";
		currentTextFormat = stackTextFormat.lastElement();
	}
	
	private void addParagraph() {
		refreshParagraph();
		if (currentTextParagraph != null) {
			formattedText.add(currentTextParagraph);
		}
		currentTextParagraph = null;
	}
	
	public FormattedTextSaxHandler() {
		stackTextFormat = new Stack<TextFormat>();  
		formattedText = new FormattedText();
	}
	
	public void startDocument() throws SAXException {
		stackTextFormat.push(FormattedTextSaxBuilder.getDefFormat());
		currentTextParagraph = null;
		currentText = "";
		currentTextFormat = FormattedTextSaxBuilder.getDefFormat();
	}
	
	public void endDocument() throws SAXException {
		
		try {
			addParagraph();
			stackTextFormat.pop();
		}
		catch (EmptyStackException e) {
			Log.e("SAX Parser ", e.toString());
			throw new SAXException();
		}
		catch (NoSuchElementException e) {
			Log.e("SAX Parser ", e.toString());
			throw new SAXException();			
		}
	}
	
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		
		try {
			TextFormat newTextFormat;
			
			if (!stackTextFormat.isEmpty()) {
				newTextFormat = stackTextFormat.lastElement().clone();
			} else {
				newTextFormat = FormattedTextSaxBuilder.getDefFormat();
			}
			
			if (localName.equals(FormattedTextSaxBuilder.FONT_TAG)) {
							
				if (attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_SIZE_TAG) != null) {
					int fontSize = Integer.parseInt(attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_SIZE_TAG));
					newTextFormat.setFontSize(fontSize);
				}
				
				if (attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_COLOR_TAG) != null) {
					int fontColor = Color.parseColor(attributes.getValue(FormattedTextSaxBuilder.FONT_ATTR_COLOR_TAG));
					newTextFormat.setFontColor(fontColor);
				}
				
			} else if (localName.equals(FormattedTextSaxBuilder.HYPER_REF_TAG)) {	
				String href = attributes.getValue(FormattedTextSaxBuilder.HYPER_REF_ATTR_HREF_TAG);
				newTextFormat.setHRef(href);
				
			} else if (localName.equals(FormattedTextSaxBuilder.UNDERLINED_TAG)) {
				newTextFormat.setUnderlined(true);
				
			} else if (localName.equals(FormattedTextSaxBuilder.PARAGRAPH_TAG)) {
				addParagraph();
			}

			stackTextFormat.push(newTextFormat);
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
		try {
			stackTextFormat.pop();
		}
		catch (EmptyStackException e) {
			Log.e("SAX Text Parser ", e.toString());
			throw new SAXException();	
		}
	}
	
	public void characters(char[] ch, int start, int len) throws SAXException {
		String newText = new String(ch, start, len);
		
		try {
			TextFormat newTextFormat = stackTextFormat.lastElement();
			
			if (newText != "") {
				if (newTextFormat.equals(currentTextFormat)) {
					currentText += newText;
				} else {
					refreshParagraph();
					currentText = newText;
				}
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
