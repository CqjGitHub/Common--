package com.imcore.common.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.imcore.common.R;

public class LoginMainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login_main);
		Button btnHelp = (Button) findViewById(R.id.help);
		Button btnLogin = (Button) findViewById(R.id.loginid);
		Button btnSina = (Button) findViewById(R.id.sinaid);
		Button btnTencen = (Button) findViewById(R.id.tencentid);
		Button btnRegister = (Button) findViewById(R.id.register);

		btnHelp.setOnClickListener(this);
		btnLogin.setOnClickListener(this);
		btnSina.setOnClickListener(this);
		btnTencen.setOnClickListener(this);
		btnRegister.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent intent = new Intent();
		switch (v.getId()) {
		case R.id.help:
			intent.setClass(LoginMainActivity.this, WelcomeActivity.class);
			intent.putExtra("help", 1);
			startActivity(intent);
			break;
		case R.id.loginid:
			intent.setClass(LoginMainActivity.this, XLoginActivity.class);
			startActivity(intent);
			break;
		case R.id.sinaid:
			intent.setClass(LoginMainActivity.this, SinaLoginActivity.class);
			startActivity(intent);
			break;
		case R.id.tencentid:
			intent.setClass(LoginMainActivity.this, TencentActivity.class);
			startActivity(intent);
			break;
		case R.id.register:
			intent.setClass(LoginMainActivity.this, MainActivity.class);
			startActivity(intent);
			break;

		default:
			break;
		}

	}
}
