package com.imcore.common.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ProgressBar;

import com.imcore.common.R;

public class LoadingActivity extends Activity {
	private ProgressBar bar;

	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);
		setContentView(R.layout.activity_loading);
		init();
	}

	public void init() {
		bar = ((ProgressBar) findViewById(R.id.progressbarid));
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					if (bar.getProgress() >= 100) {
						SharedPreferences sp = getSharedPreferences("config",
								MODE_PRIVATE);
						String username = sp.getString("username", "");
						if (username == null || username == "") {
							Intent intent = new Intent(LoadingActivity.this,
									WelcomeActivity.class);
							startActivity(intent);
						} else {
							Intent intent = new Intent(LoadingActivity.this,
									LoginMainActivity.class);
							startActivity(intent);

						}
						finish();
						return;
					}
					try {
						Thread.sleep(388L);
						bar.incrementProgressBy(16);
					} catch (InterruptedException e) {
						while (true)
							e.printStackTrace();
					}
				}
			}
		}).start();
	}

}
