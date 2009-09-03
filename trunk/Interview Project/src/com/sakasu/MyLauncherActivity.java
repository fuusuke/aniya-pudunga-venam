package com.sakasu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.SAXException;

import android.R;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.util.Linkify;
import android.text.util.Linkify.MatchFilter;
import android.text.util.Linkify.TransformFilter;
import android.util.Xml;
import android.util.Xml.Encoding;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * for brainy quotes.
 * 
 * @author sakasu
 * 
 */

public class MyLauncherActivity extends Activity {

	protected static final int UPDATE_QUOTE = 0;

	final static class Quote {
		String quote;
		String author;

		public Quote() {
			quote = new String();
			author = new String();
		}

		@Override
		public String toString() {
			return author + " - " + quote;
		}
	};

	private ArrayList<Quote> quotes = new ArrayList<Quote>();
	private TextView quoteView;
	private TextView authorView;
	private Timer timer;
	private Handler handler;
	private LinearLayout linearLayout;
	private QuoteContentHandler quoteContentHandler = new QuoteContentHandler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize();
		quoteView = new TextView(this);
		quoteView.setGravity(Gravity.CENTER);
		quoteView.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
		quoteView.setClickable(false);
		authorView = new TextView(this);
		authorView.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
		authorView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);

		linearLayout = new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);

		new Thread() {
			public void run() {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(
						"http://feeds.feedburner.com/brainyquote/QUOTEBR");
				try {
					HttpResponse response = httpClient.execute(httpGet);
					System.out.println(response.getStatusLine());
					HttpEntity entity = response.getEntity();
					try {
						Xml.parse(entity.getContent(), Encoding.UTF_8,
								quoteContentHandler);
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (SAXException e) {
						e.printStackTrace();
					}
					quotes.addAll(quoteContentHandler.getReceivedQuotes());
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			};
		}.start();

		linearLayout.addView(quoteView);
		linearLayout.addView(authorView);
		setContentView(linearLayout);

		TimerTask tt = new TimerTask() {
			Random randomizer = new Random();

			@Override
			public void run() {
				int index = randomizer.nextInt(quotes.size());
				Message msg = new Message();
				msg.what = UPDATE_QUOTE;
				msg.arg1 = index;
				msg.setTarget(handler);
				msg.sendToTarget();
			}
		};
		timer = new Timer("Quote Update Timer", true);
		timer.scheduleAtFixedRate(tt, 0L, 5000);
	}

	private void initialize() {
		handler = new Handler() {
			Animation fadeOutAnimation = AnimationUtils.loadAnimation(
					MyLauncherActivity.this, R.anim.fade_out);
			Animation fadeInAnimation = AnimationUtils.loadAnimation(
					MyLauncherActivity.this, R.anim.fade_in);
			MatchFilter matchFilter = new MatchFilter() {

				@Override
				public boolean acceptMatch(CharSequence s, int start, int end) {
					return true;
				}
			};
			TransformFilter transformFilter = new TransformFilter() {

				@Override
				public String transformUrl(Matcher match, String url) {
					url = url.toLowerCase();
					if (url.length() > 0)
						return url.charAt(0) + "/" + url.replace(' ', '_')
								+ ".html";
					return url;
				}
			};

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_QUOTE:
					quoteView.startAnimation(fadeOutAnimation);
					authorView.startAnimation(fadeOutAnimation);
					Quote quote = quotes.get(msg.arg1);
					System.out.println("***********");
					System.out.println(quotes);
					System.out.println(msg.arg1);
					System.out.println(quote.author + " - " + quote.quote);
					System.out.println("***********");
					quoteView.setText(quote.quote);
					authorView.setText("\n\n\n  " + quote.author);
					quoteView.startAnimation(fadeInAnimation);
					authorView.startAnimation(fadeInAnimation);
					Linkify.addLinks(authorView, Pattern.compile(".*"),
							"http://www.brainyquote.com/quotes/authors/",
							matchFilter, transformFilter);

				}
			}
		};
	}
}