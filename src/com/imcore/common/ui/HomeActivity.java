package com.imcore.common.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.imcore.common.R;

public class HomeActivity extends ActionBarActivity implements OnClickListener {

	private ViewPager viewpager = null;
	private ArrayList<View> list = null;
	//
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;

	private String[] mNaviItemText;
	private CharSequence mTitle;
	private CharSequence mDrawerTitle;

	private final static String NAVI_ITEM_TEXT = "text";
	private final static String NAVI_ITEM_ICON = "icon";

	private ImageView imageView1, imageView2, imageView3, imageView4;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		viewpager = (ViewPager) findViewById(R.id.home_viewpager);
		list = new ArrayList<View>();
		View viewHome1 = getLayoutInflater().inflate(R.layout.view_home1, null);
		View viewHome2 = getLayoutInflater().inflate(R.layout.view_home2, null);
		View viewHome3 = getLayoutInflater().inflate(R.layout.view_home3, null);
		View viewHome4 = getLayoutInflater().inflate(R.layout.view_home4, null);
		list.add(viewHome1);
		list.add(viewHome2);
		list.add(viewHome3);
		list.add(viewHome4);

		imageView1 = (ImageView) viewHome1.findViewById(R.id.home_imageView1);
		imageView2 = (ImageView) viewHome2.findViewById(R.id.home_imageView2);
		imageView3 = (ImageView) viewHome3.findViewById(R.id.home_imageView3);
		imageView4 = (ImageView) viewHome4.findViewById(R.id.home_imageView4);

		imageView1.setOnClickListener(this);
		imageView2.setOnClickListener(this);
		imageView3.setOnClickListener(this);
		imageView4.setOnClickListener(this);

		initDrawerLayout();
		selectNaviItem(0);
		viewpager.setAdapter(new ViewPagerAdapter(list));
		viewpager.setOnPageChangeListener(new ViewPagerPageChangeListener());

		getSupportActionBar().setIcon(R.drawable.ic_transparent);
	}

	class ViewPagerAdapter extends PagerAdapter {

		private List<View> list = null;

		public ViewPagerAdapter(List<View> list) {
			this.list = list;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(list.get(position));
			return list.get(position);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(list.get(position));
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}

	class ViewPagerPageChangeListener implements OnPageChangeListener {

		/*
		 * state：网上通常说法：1的时候表示正在滑动，2的时候表示滑动完毕了，0的时候表示什么都没做，就是停在那；
		 * 我的认为：1是按下时，0是松开，2则是新的标签页的是否滑动了
		 * (例如：当前页是第一页，如果你向右滑不会打印出2，如果向左滑直到看到了第二页，那么就会打印出2了)；
		 * 个人认为一般情况下是不会重写这个方法的
		 */
		@Override
		public void onPageScrollStateChanged(int state) {
		}

		/*
		 * page：看名称就看得出，当前页； positionOffset：位置偏移量，范围[0,1]；
		 * positionoffsetPixels：位置像素，范围[0,屏幕宽度)； 个人认为一般情况下是不会重写这个方法的
		 */
		@Override
		public void onPageScrolled(int page, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageSelected(int page) {
			for (int i = 0; i < list.size(); i++) {
				if (page == i) {
					getSupportActionBar().setTitle(mNaviItemText[i]);
				}
			}
		}
	}

	// ///////////////////
	private void initDrawerLayout() {
		mNaviItemText = getResources().getStringArray(R.array.navi_items);
		mDrawerTitle = getResources().getString(R.string.app_name);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

		Map<String, Object> item1 = new HashMap<String, Object>();
		item1.put(NAVI_ITEM_TEXT, mNaviItemText[0]);
		item1.put(NAVI_ITEM_ICON, R.drawable.ic_xactivtypage);

		Map<String, Object> item2 = new HashMap<String, Object>();
		item2.put(NAVI_ITEM_TEXT, mNaviItemText[1]);
		item2.put(NAVI_ITEM_ICON, R.drawable.ic_xactivtypage2);

		Map<String, Object> item3 = new HashMap<String, Object>();
		item3.put(NAVI_ITEM_TEXT, mNaviItemText[2]);
		item3.put(NAVI_ITEM_ICON, R.drawable.ic_xactivtypage3);

		Map<String, Object> item4 = new HashMap<String, Object>();
		item4.put(NAVI_ITEM_TEXT, mNaviItemText[3]);
		item4.put(NAVI_ITEM_ICON, R.drawable.ic_xactivtypage4);

		List<Map<String, Object>> data = new ArrayList<Map<String, Object>>();
		data.add(item1);
		data.add(item2);
		data.add(item3);
		data.add(item4);

		String[] from = new String[] { NAVI_ITEM_TEXT, NAVI_ITEM_ICON };
		int[] to = new int[] { R.id.tv_navi_item_text, R.id.iv_navi_item_icon };
		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerList.setAdapter(new SimpleAdapter(this, data,
				R.layout.view_navi_drawer_item, from, to));
		mDrawerList
				.setOnItemClickListener(new NaviDrawerListItemOnClickListner());

		initialDrawerListener();

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeButtonEnabled(true);

	}

	private void initialDrawerListener() {
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.hello_world,
				R.string.hello_world) {
			@Override
			public void onDrawerOpened(View drawerView) {
				getSupportActionBar().setTitle(mDrawerTitle);
			}

			@Override
			public void onDrawerClosed(View drawerView) {
				getSupportActionBar().setTitle(mTitle);
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);
	}

	private class NaviDrawerListItemOnClickListner implements
			OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			selectNaviItem(position);
		}

	}

	private void selectNaviItem(int position) {
		switch (position) {
		case 0:
			viewpager.setCurrentItem(0);
			break;
		case 1:
			viewpager.setCurrentItem(1);
			break;
		case 2:
			viewpager.setCurrentItem(2);
			break;
		case 3:
			viewpager.setCurrentItem(3);
			break;
		}

		mDrawerList.setItemChecked(position, true);
		setTitle(mNaviItemText[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		super.setTitle(title);
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		//
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.home_imageView1:
			Intent intent1 = new Intent(HomeActivity.this, MainActivity.class);
			startActivity(intent1);
			break;
		case R.id.home_imageView2:
			Intent intent2 = new Intent(HomeActivity.this,
					IntroductionHomeActivity.class);
			startActivity(intent2);
			break;
		case R.id.home_imageView3:
			Intent intent3 = new Intent(HomeActivity.this,
					ProductMainActivity.class);
			startActivity(intent3);
			break;
		case R.id.home_imageView4:
			Intent intent4 = new Intent(HomeActivity.this, MainActivity.class);
			startActivity(intent4);
			break;

		default:
			break;
		}

	}
}