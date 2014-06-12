package com.imcore.common.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBar.TabListener;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.imcore.common.R;
import com.imcore.common.frgmt.CategoryListFrgmt;
import com.imcore.common.http.HttpHelper;
import com.imcore.common.http.HttpMethod;
import com.imcore.common.http.RequestEntity;
import com.imcore.common.http.ResponseJsonEntity;
import com.imcore.common.model.CategoryList;
import com.imcore.common.util.JsonUtil;
import com.imcore.common.util.TextUtil;

public class ProductListActivity extends ActionBarActivity {
	private int navId;
	private int subNavId;

	private ViewPager mViewPager;
	private List<CategoryList> list;

	private ActionBar actionBar;
	private Spinner mSpinner;
	private int mPosition = 0;

	private CategoryListFrgmt frgmt;
	private ProductListAdapter pListAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_product_list);

		Intent intent = getIntent();
		navId = intent.getIntExtra("navId", 0);
		subNavId = intent.getIntExtra("subNavId", 0);
		// Toast.makeText(this, navId + "    " + subNavId, Toast.LENGTH_LONG)
		// .show();
		// 设定ActionBar导航模式Tab导航
		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		// 隐藏系统图标为透明
		actionBar.setIcon(R.drawable.ic_transparent);

		initSpinner();
		new ProductListTask().execute(navId, subNavId);

	}

	private void initSpinner() {
		// 将ActionBar的操作模型设置为NAVIGATION_MODE_LIST
		// getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		// 初始化控件
		View viewSpinner = LayoutInflater.from(this).inflate(
				R.layout.spinner_sort, null);
		// 在ActionBar上放置定制view
		getSupportActionBar().setCustomView(viewSpinner);
		// 使自定义的普通View能在title栏显示, actionBar.setCustomView能起作用.
		getSupportActionBar().setDisplayShowCustomEnabled(true);

		mSpinner = (Spinner) viewSpinner.findViewById(R.id.spinner);
		// 建立数据源
		String[] mItems = getResources().getStringArray(R.array.sort);
		// 建立Adapter并且绑定数据源
		ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this,
				R.layout.support_simple_spinner_dropdown_item, mItems);
		// 绑定 Adapter到控件
		mSpinner.setAdapter(spinnerAdapter);

		mSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mPosition = position;
				frgmt = new CategoryListFrgmt();
				Bundle args = new Bundle();
				args.putInt("navId", navId);
				args.putInt("subNavId", subNavId);
				// args.putInt("id", list.get(position).id);// /??
				args.putInt("desc", mPosition);
				frgmt.setArguments(args);
				if (pListAdapter != null) {
					pListAdapter.notifyDataSetChanged();
				}

			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
			}
		});

	}

	private class ProductListTask extends AsyncTask<Integer, Void, String> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// 显示通知等待
			dialog = ProgressDialog.show(ProductListActivity.this, "请稍候",
					"数据加载中……");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int nav = params[0];
			int subNab = params[1];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("navId", nav);
			args.put("subNavId", subNab);
			String url = "/category/list.do";
			// 将请求实体存到一个对象中
			RequestEntity entity = new RequestEntity(url, HttpMethod.GET, args);
			String jsonResponse = null;
			try {
				// 得到请求的网络返回的结果
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
			if (TextUtil.isEmptyString(result)) {
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);

			if (resEntity.getStatus() == 200) {
				String jsonData = resEntity.getData();
				Log.i("CategoryList", jsonData);
				list = JsonUtil.toObjectList(jsonData, CategoryList.class);
			} //
			pListAdapter = new ProductListAdapter();
			mViewPager = (ViewPager) findViewById(R.id.viewpager_product_list);
			mViewPager.setAdapter(pListAdapter);
			addTab();
			mViewPager.setOnPageChangeListener(pageChangeListener);

			super.onPostExecute(result);
		}
	}

	private void addTab() {
		for (int i = 0; i < list.size(); i++) {
			Tab tab = actionBar.newTab();
			tab.setText(list.get(i).categoryName);
			tab.setTabListener(tabListener);
			actionBar.addTab(tab);
		}
	}

	// 设置ViewPager适配器productList
	private class ProductListAdapter extends FragmentStatePagerAdapter {

		public ProductListAdapter() {
			super(getSupportFragmentManager());
		}

		@Override
		public int getCount() {
			return list.size();
		}

		// 重新刷新按价格排序
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public Fragment getItem(int arg0) {
			frgmt = new CategoryListFrgmt();
			Bundle args = new Bundle();
			args.putInt("navId", navId);
			args.putInt("subNavId", subNavId);
			args.putInt("id", list.get(arg0).id);
			args.putInt("desc", mPosition);
			frgmt.setArguments(args);
			return frgmt;
		}
	}

	// ViewPager切换页面时的监听器
	private OnPageChangeListener pageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			getSupportActionBar().setSelectedNavigationItem(arg0);
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO Auto-generated method stub
		}
	};
	// Tab监听器
	private TabListener tabListener = new TabListener() {

		@Override
		public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onTabSelected(Tab arg0, FragmentTransaction arg1) {
			mViewPager.setCurrentItem(arg0.getPosition());

		}

		@Override
		public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
			// TODO Auto-generated method stub

		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product_list, menu);
		return true;
	}

}
