package com.imcore.common.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.imcore.common.R;
import com.imcore.common.frgmt.LeftFragment;
import com.imcore.common.http.HttpHelper;
import com.imcore.common.http.HttpMethod;
import com.imcore.common.http.RequestEntity;
import com.imcore.common.http.ResponseJsonEntity;
import com.imcore.common.image.ImageFetcher;
import com.imcore.common.model.Category;
import com.imcore.common.util.JsonUtil;
import com.imcore.common.util.TextUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class ProductMainActivity extends SlidingFragmentActivity {
	private ExpandableListView mExpanList;
	private List<Integer> groupList;
	private List<Category> list1;
	private List<Category> list2;
	private List<List<Category>> childList;
	private ImageFetcher imageFetcher;
	private ExpandAdapter adapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_main);
		mExpanList = (ExpandableListView) findViewById(R.id.expandableListView1);
		initializeList();

		Button btnProductLeft = (Button) findViewById(R.id.btn_x_product_list);
		btnProductLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggle();

			}
		});
		// 初始化滑动菜单
		initSlidingMenu(savedInstanceState);
	}

	/**
	 * 初始化滑动菜单
	 */
	private void initSlidingMenu(Bundle savedInstanceState) {
		// 设置滑动菜单的视图
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, new LeftFragment()).commit();
		// 实例化滑动菜单对象
		SlidingMenu sm = getSlidingMenu();
		// 设置滑动阴影的宽度
		sm.setShadowWidthRes(R.dimen.shadow_width);
		// 设置滑动阴影的图像资源
		sm.setShadowDrawable(R.drawable.shadow);
		// 设置滑动菜单视图的宽度
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
		// 设置渐入渐出效果的值
		sm.setFadeDegree(0.35f);
		// 设置触摸屏幕的模式
		sm.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	private void initializeList() {
		imageFetcher = new ImageFetcher();
		groupList = new ArrayList<Integer>();
		childList = new ArrayList<List<Category>>();
		mExpanList.setGroupIndicator(null);// 隐藏默认图标
		adapter = new ExpandAdapter();
		groupList.add(R.drawable.upbackground);
		groupList.add(R.drawable.downbackground);

		new CategoryTask().execute(100001);
		new CategoryTask().execute(100002);
	}

	// 产品Category
	private class CategoryTask extends AsyncTask<Integer, Void, String> {
		private int requestCode;

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// 显示通知等待
			dialog = ProgressDialog.show(ProductMainActivity.this, "请稍候",
					"正在加载……");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			requestCode = params[0];
			String url = "/category/list.do";
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("navId", requestCode);

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
				Log.i("allCategory", jsonData);
				if (requestCode == 100001) {
					list1 = JsonUtil.toObjectList(jsonData, Category.class);
					childList.add(list1);
				} else if (requestCode == 100002) {
					list2 = JsonUtil.toObjectList(jsonData, Category.class);
					childList.add(list2);
				}

				mExpanList.setAdapter(adapter);
			}
			super.onPostExecute(result);

		}
	}

	private class ExpandAdapter extends BaseExpandableListAdapter {
		private GridAdapter gridAdapter;

		@Override
		public int getGroupCount() {
			return groupList.size();
		}

		@Override
		public int getChildrenCount(int groupPosition) {
			return 1;
		}

		@Override
		public Object getGroup(int groupPosition) {
			return groupList.get(groupPosition);
		}

		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return childList.get(groupPosition).get(childPosition);
		}

		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}

		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}

		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			View view = convertView;
			GroupViewHolder gvh = null;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.view_product_group,
						null);
				gvh = new GroupViewHolder();
				gvh.imgCategory = (ImageView) view
						.findViewById(R.id.img_product_group);
				view.setTag(gvh);
			} else {
				gvh = (GroupViewHolder) view.getTag();
			}
			gvh.imgCategory.setImageResource(groupList.get(groupPosition));
			return view;
		}

		class GroupViewHolder {
			ImageView imgCategory;
		}

		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			View view = convertView;
			ChildViewHolder cvh = null;
			final int groupIndex = groupPosition;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.view_product_grid,
						null);
				cvh = new ChildViewHolder();
				cvh.gridView = (GridView) view
						.findViewById(R.id.gv_product_data);
				//
				// cvh.gridView.setNumColumns(4);// 设置每行列数
				// cvh.gridView.setGravity(Gravity.CENTER);// 位置居中
				// cvh.gridView.setVerticalSpacing(10);
				// cvh.gridView.setVisibility(View.VISIBLE);
				view.setTag(cvh);
			} else {
				cvh = (ChildViewHolder) view.getTag();
			}

			gridAdapter = new GridAdapter(groupPosition);
			// 设置Child成为gridView视图
			cvh.gridView.setAdapter(gridAdapter);
			// 设置Child中的gridView监听
			cvh.gridView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent intent = new Intent(ProductMainActivity.this,
							ProductListActivity.class);
					if (groupIndex == 0) {
						intent.putExtra("navId", 100001);
						intent.putExtra("subNavId", list1.get(position).id);
					} else if (groupIndex == 1) {
						intent.putExtra("navId", 100002);
						intent.putExtra("subNavId", list2.get(position).id);
					}
					startActivity(intent);
				}
			});
			return view;
		}

		private class ChildViewHolder {
			GridView gridView;
		}

		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}

	}

	private class GridAdapter extends BaseAdapter {
		private int groupPosition;

		public GridAdapter(int groupPosition) {
			this.groupPosition = groupPosition;
		}

		@Override
		public int getCount() {
			return childList.get(groupPosition).size();
		}

		@Override
		public Object getItem(int position) {
			return childList.get(groupPosition).get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder viewHolder = null;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.view_product_item,
						null);
				viewHolder = new ViewHolder();
				viewHolder.imgCategory = (ImageView) view
						.findViewById(R.id.img_product_item);
				viewHolder.tvCategory = (TextView) view
						.findViewById(R.id.tv_product_item);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			Category category = childList.get(groupPosition).get(position);
			String imgPath = "http://bulo2bulo.com:8080/" + category.imageUrl
					+ "_L.png";
			imageFetcher.fetch(imgPath, viewHolder.imgCategory);
			// viewHolder.imgCategory.setImageResource(R.drawable.ic_xactivtypage);
			viewHolder.tvCategory.setText(category.name);
			return view;
		}

	}

	private class ViewHolder {
		public ImageView imgCategory;
		public TextView tvCategory;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product_main, menu);
		return true;
	}

}
