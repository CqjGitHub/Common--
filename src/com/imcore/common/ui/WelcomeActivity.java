package com.imcore.common.ui;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.imcore.common.R;

public class WelcomeActivity extends Activity {

	private ViewPager viewpager = null;
	private ArrayList<View> list = null;
	private ImageView[] img = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置不显示顶部标题bar
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_welcome);
		viewpager = (ViewPager) findViewById(R.id.viewpager);
		list = new ArrayList<View>();
		list.add(getLayoutInflater().inflate(R.layout.tab1, null));
		list.add(getLayoutInflater().inflate(R.layout.tab2, null));
		list.add(getLayoutInflater().inflate(R.layout.tab3, null));

		img = new ImageView[list.size()];
		LinearLayout layout = (LinearLayout) findViewById(R.id.viewGroup);
		for (int i = 0; i < list.size(); i++) {
			img[i] = (ImageView) layout.getChildAt(i);
			// 还没有滑动的时候的图标
			if (i == 0) {
				img[i].setBackgroundResource(R.drawable.yes);
			} else {
				img[i].setBackgroundResource(R.drawable.no);
			}
			img[i].setPadding(0, 0, 0, 0);
		}
		viewpager.setAdapter(new ViewPagerAdapter(list));
		viewpager.setOnPageChangeListener(new ViewPagerPageChangeListener());
		try {
			Intent intent = getIntent();
			Bundle args = intent.getExtras();
			int i = args.getInt("help");
		} catch (Exception e) {
			// 启动线程
			MyThread myThread = new MyThread();
			myThread.start();
		}
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
			// 更新图标
			for (int i = 0; i < list.size(); i++) {
				if (page == i) {
					img[i].setBackgroundResource(R.drawable.yes);
				} else {
					img[i].setBackgroundResource(R.drawable.no);
				}
			}
		}
	}

	/*
	 * viewpager自动滑动
	 */

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			viewpager.setCurrentItem(msg.what);
			super.handleMessage(msg);
		}
	};

	public class MyThread extends Thread {

		@Override
		public void run() {// 重写的run方法
			for (int i = 0; i < list.size(); i++) {// 循环
				handler.sendEmptyMessage(i);
				try {
					Thread.sleep(1200);// 睡眠
				} catch (Exception e) {
					e.printStackTrace();// 打印异常信息
				}

			}
			Intent intent = new Intent(WelcomeActivity.this,
					LoginMainActivity.class);
			startActivity(intent);
			finish();
		}
	}
}