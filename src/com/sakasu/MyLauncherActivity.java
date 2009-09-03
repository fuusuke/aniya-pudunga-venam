package com.sakasu;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.R;
import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class MyLauncherActivity extends Activity {

	protected static final int UPDATE_QUOTE = 0;

	private ArrayList<String> quotes = new ArrayList<String>();
	private TextView quoteView;
	private Timer timer;
	private Handler handler;
	private TimerTask quoteUpdateTask;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initialize();
		quoteView = new TextView(this);
		quoteView.setGravity(Gravity.CENTER);
		quoteView.setTypeface(Typeface.SANS_SERIF, Typeface.ITALIC);
		quoteView.setClickable(false);
		new Thread() {
			public void run() {
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpGet = new HttpGet(
						"http://search.twitter.com/search.json?q=from%3AHisHoliness");
				try {
					HttpResponse response = httpClient.execute(httpGet);
					System.out.println(response.getStatusLine());
					HttpEntity entity = response.getEntity();
					ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
					entity.writeTo(byteArrayOutputStream);
					entity.consumeContent();
					String jsonString = new String(byteArrayOutputStream
							.toByteArray());
					System.out.println(jsonString);
					JSONObject results = new JSONObject(jsonString);
					JSONArray resultsArray = results.getJSONArray("results");
					for (int i = 0; i < resultsArray.length(); i++) {
						JSONObject singleTweet = resultsArray.getJSONObject(i);
						String quote = singleTweet.getString("text");
						quotes.add(quote);
					}
					timer.scheduleAtFixedRate(quoteUpdateTask, 0L, 20000);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		}.start();
		setContentView(quoteView);
	}

	private void initialize() {
		timer = new Timer("Quote Update Timer", true);
		handler = new Handler() {
			Animation fadeOutAnimation = AnimationUtils.loadAnimation(
					MyLauncherActivity.this, R.anim.fade_out);
			Animation fadeInAnimation = AnimationUtils.loadAnimation(
					MyLauncherActivity.this, R.anim.fade_in);

			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case UPDATE_QUOTE:
					quoteView.startAnimation(fadeOutAnimation);
					String quote = quotes.get(msg.arg1);
					System.out.println("***********");
					System.out.println(quotes);
					System.out.println(msg.arg1);
					System.out.println(quote);
					System.out.println("***********");
					quoteView.setText(quote);
					quoteView.startAnimation(fadeInAnimation);
				}
			}
		};
		quoteUpdateTask = new TimerTask() {
			int index = new Random().nextInt(15);
			@Override
			public void run() {
				index %= quotes.size();
				Message msg = new Message();
				msg.what = UPDATE_QUOTE;
				msg.arg1 = index;
				msg.setTarget(handler);
				msg.sendToTarget();
				index++;
			}
		};
	
	}
}