package com.sakasu;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sakasu.MyLauncherActivity.Quote;

/**
 * for brainy quotes.
 * 
 * @author sujay
 * 
 */

public class QuoteContentHandler implements ContentHandler {

	private boolean itemFlag;

	Quote quote;

	private ArrayList<Quote> receivedQuotes = new ArrayList<Quote>(4);

	private String lastElement;

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if (itemFlag) {
			if (lastElement.equals("title")) {
				quote = new Quote();
				quote.author = new String(ch, start, length);
				System.out.println("auh " + quote.author);
			} else if (lastElement.equals("description")) {
				quote.quote = new String(ch, start, length);
				System.out.println("Before adding " + quote);
				receivedQuotes.add(quote);
			}
		}
	}

	@Override
	public void endDocument() throws SAXException {
		System.out.println(receivedQuotes);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if (localName.equals("item")) {
			itemFlag = false;
		}
		lastElement = "";
	}

	@Override
	public void endPrefixMapping(String prefix) throws SAXException {

	}

	@Override
	public void ignorableWhitespace(char[] ch, int start, int length)
			throws SAXException {

	}

	@Override
	public void processingInstruction(String target, String data)
			throws SAXException {

	}

	@Override
	public void setDocumentLocator(Locator locator) {

	}

	@Override
	public void skippedEntity(String name) throws SAXException {

	}

	@Override
	public void startDocument() throws SAXException {

	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {
		if (localName.equals("item")) {
			itemFlag = true;
		}
		lastElement = localName;
	}

	@Override
	public void startPrefixMapping(String prefix, String uri)
			throws SAXException {
		// TODO Auto-generated method stub

	}

	public ArrayList<Quote> getReceivedQuotes() {
		return receivedQuotes;
	}
}
//new Thread() {
//public void run() {
//DefaultHttpClient httpClient = new DefaultHttpClient();
//HttpGet httpGet = new HttpGet(
//"http://feeds.feedburner.com/brainyquote/QUOTEBR");
//try {
//HttpResponse response = httpClient.execute(httpGet);
//System.out.println(response.getStatusLine());
//HttpEntity entity = response.getEntity();
//try {
//Xml.parse(entity.getContent(), Encoding.UTF_8,
//quoteContentHandler);
//} catch (IllegalStateException e) {
//// TODO Auto-generated catch block
//e.printStackTrace();
//} catch (SAXException e) {
//// TODO Auto-generated catch block
//e.printStackTrace();
//}
//quotes.addAll(quoteContentHandler.getReceivedQuotes());
//} catch (ClientProtocolException e) {
//// TODO Auto-generated catch block
//e.printStackTrace();
//} catch (IOException e) {
//// TODO Auto-generated catch block
//e.printStackTrace();
//}
//};
//}.start();
