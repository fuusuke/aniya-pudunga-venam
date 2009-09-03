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
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

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
		authorView.setGravity(Gravity.CENTER_VERTICAL|Gravity.RIGHT);
		authorView.setTypeface(Typeface.SANS_SERIF, Typeface.BOLD);
		// CharSequence styledResults =
		// Html.fromHtml("<a href=\"http://www.google.com\">Google</a>");

		// System.out.println(styledResults);
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
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SAXException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					quotes.addAll(quoteContentHandler.getReceivedQuotes());
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			};
		}.start();

		// this.setListAdapter(new ArrayAdapter<String>(this,
		// R.layout.simple_list_item_1, new String[] { "9620927739",
		// "9739479914", "9611827822" }));

		setContentView(quoteView);
		addContentView(authorView, new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT));
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
		// TODO Auto-generated method stub
		handler = new Handler() {
			Animation fadeOutAnimation = AnimationUtils.loadAnimation(
					MyLauncherActivity.this, R.anim.fade_out);
			Animation fadeInAnimation = AnimationUtils.loadAnimation(
					MyLauncherActivity.this, R.anim.fade_in);
			MatchFilter matchFilter = new MatchFilter() {

				@Override
				public boolean acceptMatch(CharSequence s, int start,
						int end) {
					return true;
				}
			};
			TransformFilter transformFilter =  new TransformFilter() {

				@Override
				public String transformUrl(Matcher match, String url) {
					url = url.toLowerCase();
					if(url.length()>0)
						return url.charAt(0)+"/"+url.replace(' ', '_') + ".html";
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
					System.out.println(quote.author+" - "+quote.quote);
					System.out.println("***********");
					quoteView.setText(quote.quote);
					authorView.setText(quote.author);
					quoteView.startAnimation(fadeInAnimation);
					authorView.startAnimation(fadeInAnimation);
					Linkify.addLinks(authorView, Pattern.compile(".*"), "http://www.brainyquote.com/quotes/authors/",
							matchFilter,transformFilter);

				}
			}
		};
	}
}