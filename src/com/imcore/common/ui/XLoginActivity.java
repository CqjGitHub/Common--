package com.imcore.common.ui;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.imcore.common.R;
import com.imcore.common.app.XbionicApp;
import com.imcore.common.data.SharePreferencesUtil;
import com.imcore.common.http.HttpHelper;
import com.imcore.common.http.RequestEntity;
import com.imcore.common.http.ResponseJsonEntity;
import com.imcore.common.model.User;
import com.imcore.common.util.ConnectivityUtil;
import com.imcore.common.util.JsonUtil;
import com.imcore.common.util.TextUtil;
import com.imcore.common.util.ToastUtil;

public class XLoginActivity extends Activity implements OnClickListener {

	private EditText etUserName;
	private EditText etPassword;

	private Button btnLogin;

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_x_login);

		etUserName = (EditText) findViewById(R.id.userpeopleid);
		etPassword = (EditText) findViewById(R.id.userposswdid);
		btnLogin = (Button) findViewById(R.id.getinid);

		SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
		if (sp != null) {
			String username = sp.getString("username", "");
			String password = sp.getString("password", "");
			etUserName.setText(username);
			etPassword.setText(password);
		}

		btnLogin.setOnClickListener(this);

		Button btnX = (Button) findViewById(R.id.btn_back_x);
		btnX.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.getinid:
			doLogin();
			break;
		case R.id.btn_back_x:
			finish();
			break;
		}

	}

	// 登录到Home页面
	private void doLogin() {
		String inputUserName = etUserName.getText().toString();
		String inputPassword = etPassword.getText().toString();

		if (!ConnectivityUtil.isOnline(this)) {
			ToastUtil.showToast(XLoginActivity.this, "没有网络连接");
		}
		// 判断登录用户名和密码是否合法
		if (!validateInput(inputUserName, inputPassword)) {
			return;
		}
		// 通过AsyncTask异步请求网络服务,通过构造函数传递参数数据
		new LoginTask(inputUserName, inputPassword).execute();

	}

	// 判断输入的用户名和密码是否合法
	private boolean validateInput(String userName, String pwd) {
		if (TextUtil.isEmptyString(userName)) {
			ToastUtil.showToast(this, "用户名不能为空");
			etUserName.requestFocus();
			return false;
		} else if (!TextUtil.isNumber(userName)) {
			ToastUtil.showToast(this, "用户名是错误的");
			etUserName.requestFocus();
			return false;
		} else if (!TextUtil.isPhoneNumber(userName)) {
			ToastUtil.showToast(this, "用户名是错误的");
			etUserName.requestFocus();
			return false;
		} else if (TextUtil.isEmptyString(pwd)) {
			ToastUtil.showToast(this, "密码是错误的");
			etPassword.requestFocus();
			return false;
		}
		return true;
	}

	class LoginTask extends AsyncTask<Void, Void, String> {

		private String mUsetName;
		private String mPassword;

		public LoginTask(String userName, String password) {
			mUsetName = userName;
			mPassword = password;
		}

		@Override
		protected void onPreExecute() {
			// 显示登陆通知等待
			dialog = new ProgressDialog(XLoginActivity.this);
			dialog.setTitle("正在登陆");
			dialog.setMessage("数据加载中......");
			dialog.show();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			String url = "/passport/login.do";
			// 把请求参数装到Map中
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("phoneNumber", mUsetName);
			args.put("password", mPassword);
			args.put("device", "消息推送");
			args.put("client", "android");

			// 构造RequestEntity参数(请求实体)
			RequestEntity entity = new RequestEntity(url, args);

			String jsonResponse = null;
			try {
				jsonResponse = HttpHelper.execute(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return jsonResponse;
		}

		@Override
		protected void onPostExecute(String result) {
			dialog.dismiss();
			dialog = null;
			// 响应回来之后构建ResponseEntity(响应实体)
			if (TextUtil.isEmptyString(result)) {
				ToastUtil.showToast(XLoginActivity.this, "输入为空");
				return;
			}
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);

			if (resEntity.getStatus() == 200) {
				String jsonData = resEntity.getData();
				Log.i("user", jsonData);
				String userId = JsonUtil.getJsonValueByKey(jsonData, "userId");
				String token = JsonUtil.getJsonValueByKey(jsonData, "token");
				User user = JsonUtil.toObject(jsonData, User.class);
				XbionicApp.addUser(user);// 添加到application里面
				Log.i("user", user + "");
				Log.i("user", userId);
				Log.i("user", token);
				// 在SharePreferencesUtil中保存
				String psw = etPassword.getText().toString();// 从文本框输入得到密码
				String num = etUserName.getText().toString();// 从文本框输入得到账号
				SharePreferencesUtil sp = new SharePreferencesUtil();
				sp.saveLoginInfo(XLoginActivity.this, num, psw);

				// 跳转到首页
				Intent intent = new Intent(XLoginActivity.this,
						HomeActivity.class);
				startActivity(intent);
				finish();
				super.onPostExecute(result);
			} else {
				ToastUtil.showToast(XLoginActivity.this, "您的输入有误,请重新输入");
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.x_login, menu);
		return true;
	}
}
