package com.imcore.common.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.imcore.common.R;
import com.imcore.common.app.XbionicApp;
import com.imcore.common.frgmt.RightFragment;
import com.imcore.common.http.HttpHelper;
import com.imcore.common.http.HttpMethod;
import com.imcore.common.http.RequestEntity;
import com.imcore.common.http.ResponseJsonEntity;
import com.imcore.common.image.ImageFetcher;
import com.imcore.common.model.CategoryDetail;
import com.imcore.common.model.CategoryImages;
import com.imcore.common.model.ProductColor;
import com.imcore.common.model.ProductLabs;
import com.imcore.common.model.ProductQuantity;
import com.imcore.common.model.ProductSize;
import com.imcore.common.model.ProductSizeDetail;
import com.imcore.common.util.JsonUtil;
import com.imcore.common.util.TextUtil;
import com.imcore.common.util.ToastUtil;
import com.jeremyfeinstein.slidingmenu.lib.SlidingMenu;
import com.jeremyfeinstein.slidingmenu.lib.app.SlidingFragmentActivity;

public class ProductDetailsActivity extends SlidingFragmentActivity {
	private Gallery gallery;
	private List<CategoryImages> listImages;
	private ImageView imgDot;
	private ImageView[] imageViews;
	// 包裹小圆点的LinearLayout
	private LinearLayout linearGroup;

	private List<ProductColor> listColors;
	private List<ProductSize> listSizes;
	private List<ProductSizeDetail> listSizeDetails;

	private List<ProductLabs> listLabs;

	private int id;
	private int colorId;
	private int sizeId;

	private String sizeValue;
	private String colorValue;
	private Bundle args;
	private RightFragment frgmt;

	private EditText etQty;
	private long qtyId; // 产品库存实例ID

	private int qty;// 库存数量

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_product_details);
		Intent intent = getIntent();
		Bundle args = intent.getExtras();
		id = args.getInt("id");

		gallery = (Gallery) findViewById(R.id.gallery_image);

		new ImageTask().execute(id);
		new DetailTask().execute(id);
		new ProductSizeTask().execute(id);
		new ProductLabsTask().execute(id);
		// new ProductQuantityTask().execute(267, 2, 1);
		gallery.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// 更新图标
				for (int i = 0; i < listImages.size(); i++) {
					if (position == i) {
						imageViews[i].setBackgroundResource(R.drawable.yes);
					} else {
						imageViews[i].setBackgroundResource(R.drawable.no);
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		Button btnProductRight = (Button) findViewById(R.id.btn_product_more);
		btnProductRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				toggle();

			}
		});
		// 初始化滑动菜单
		initSlidingMenu(savedInstanceState);

		etQty = (EditText) findViewById(R.id.tv_qty);

		Button btnBuy = (Button) findViewById(R.id.btn_buy);
		btnBuy.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String str = etQty.getText().toString();
				if (str == null || "".equals(str)) {
					ToastUtil.showToast(ProductDetailsActivity.this,
							"购买数量不能为空！");
					return;
				}
				int qtyCount = Integer.parseInt(str);
				if (qtyCount < 0 || qtyCount > qty) {
					ToastUtil.showToast(ProductDetailsActivity.this, "库存不足");
					return;
				}
				new ShoppingAddTask().execute(qtyCount);
				Toast.makeText(ProductDetailsActivity.this, "购买",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * 初始化滑动菜单
	 */
	private void initSlidingMenu(Bundle savedInstanceState) {
		frgmt = new RightFragment();
		args = new Bundle();
		args.putInt("id", id);
		args.putString("colorValue", colorValue);
		args.putString("sizeValue", sizeValue);
		frgmt.setArguments(args);
		// 设置滑动菜单的视图
		setBehindContentView(R.layout.menu_frame);
		getSupportFragmentManager().beginTransaction()
				.replace(R.id.menu_frame, frgmt).commit();
		// 实例化滑动菜单对象
		SlidingMenu sm = getSlidingMenu();
		sm.setMode(SlidingMenu.RIGHT);
		// 设置滑动菜单视图的宽度
		sm.setBehindOffsetRes(R.dimen.slidingmenu_offset);
	}

	/*
	 * • 列出产品图片（可测）
	 */
	private class ImageTask extends AsyncTask<Integer, Void, String> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// 显示通知等待
			dialog = ProgressDialog.show(ProductDetailsActivity.this, "请稍候",
					"数据加载中……");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int id = params[0];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", id);
			String url = "/product/images/list.do";
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
				ToastUtil.showToast(ProductDetailsActivity.this, "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);

			if (resEntity.getStatus() == 200) {
				String jsonDataImg = resEntity.getData();
				Log.i("Productimages", jsonDataImg);
				listImages = JsonUtil.toObjectList(jsonDataImg,
						CategoryImages.class);

				gallery.setAdapter(new GalleryAdapter());
				// ////////
				imageViews = new ImageView[listImages.size()];
				linearGroup = (LinearLayout) findViewById(R.id.view_group);
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
						12, 12);
				// layoutParams.gravity = Gravity.CENTER_VERTICAL;
				layoutParams.leftMargin = 6;
				for (int i = 0; i < listImages.size(); i++) {
					imgDot = new ImageView(ProductDetailsActivity.this);
					imgDot.setLayoutParams(layoutParams);
					// imgDot.setPadding(0, 0, 0, 0);
					imageViews[i] = imgDot;
					if (i == 0) {
						// 默认选中第一张图片
						imageViews[i].setBackgroundResource(R.drawable.yes);
					} else {
						imageViews[i].setBackgroundResource(R.drawable.no);
					}
					linearGroup.addView(imageViews[i]);
				}
			} //

			super.onPostExecute(result);
		}
	}

	// 自定义gallery适配器
	private class GalleryAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listImages.size();
		}

		@Override
		public Object getItem(int position) {
			return listImages.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder vh = null;
			if (view == null) {
				view = getLayoutInflater().inflate(R.layout.view_gallery_image,
						null);
				vh = new ViewHolder();
				vh.ivPic = (ImageView) view.findViewById(R.id.img_info_gallery);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			CategoryImages ci = listImages.get(position);

			String imgUrl = "http://bulo2bulo.com" + ci.image + "_L.jpg";
			new ImageFetcher().fetch(imgUrl, vh.ivPic);

			return view;
		}

		private class ViewHolder {
			ImageView ivPic;
		}
	}

	/*
	 * • 获取产品详情（可测）详情内容包含：售价、可选颜色、标题、详细描述、
	 */
	private class DetailTask extends AsyncTask<Integer, Void, String> {

		private ProgressDialog dialog;
		private ImageView imgColor;

		@Override
		protected void onPreExecute() {
			// 显示通知等待
			dialog = ProgressDialog.show(ProductDetailsActivity.this, "请稍候",
					"数据加载中……");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			String url = "/product/get.do";
			int id = params[0];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", id);
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
				ToastUtil.showToast(ProductDetailsActivity.this, "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);

			if (resEntity.getStatus() == 200) {
				String jsonData = resEntity.getData();
				Log.i("ProductDetails", jsonData);
				CategoryDetail categoryDetail = JsonUtil.toObject(jsonData,
						CategoryDetail.class);

				String jsonColor = JsonUtil.getJsonValueByKey(jsonData,
						"sysColorList");
				listColors = JsonUtil.toObjectList(jsonColor,
						ProductColor.class);

				TextView tvTitle = (TextView) findViewById(R.id.tv_product_title);
				TextView tvPrice = (TextView) findViewById(R.id.tv_price_value);
				tvTitle.setText(categoryDetail.name);
				tvPrice.setText("￥" + categoryDetail.price);
				//
				final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_color); //

				for (ProductColor pc : listColors) {

					imgColor = new ImageView(ProductDetailsActivity.this);
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
							52, 52);
					layoutParams.gravity = Gravity.CENTER_VERTICAL;
					layoutParams.leftMargin = 18;
					layoutParams.topMargin = 6;
					imgColor.setLayoutParams(layoutParams);
					String imgPath = "http://www.bulo2bulo.com" + pc.colorImage
							+ ".jpg";
					new ImageFetcher().fetch(imgPath, imgColor);
					// 添加imgColor到布局
					linearLayout.addView(imgColor);

				}// for//

				// 设置imageview边框点击等
				for (int i = 1; i <= listColors.size(); i++) {
					final ImageView iv = (ImageView) linearLayout.getChildAt(i);
					final int m = i;
					iv.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							frgmt = new RightFragment();
							colorValue = listColors.get(m - 1).color;// 传值
							args.putString("colorValue", colorValue);
							args.putString("sizeValue", sizeValue);
							frgmt.setArguments(args);
							getSupportFragmentManager().beginTransaction()
									.replace(R.id.menu_frame, frgmt).commit();
							// //////////
							colorId = listColors.get(m - 1).id;
							for (int j = 1; j < linearLayout.getChildCount(); j++) {
								if (iv == linearLayout.getChildAt(j)) {
									iv.setBackgroundResource(R.drawable.image_color_bg);
									Toast.makeText(
											ProductDetailsActivity.this,
											"点击了" + id + "  " + colorId
													+ "    " + sizeId
													+ colorValue,
											Toast.LENGTH_SHORT).show();
								} else {
									linearLayout
											.getChildAt(j)
											.setBackgroundResource(
													R.drawable.image_no_color_bg);
								}
							}
							new ProductQuantityTask().execute(id, colorId,
									sizeId);
						}
					});
				}
			}

			super.onPostExecute(result);
		}
	}

	/*
	 * •列出产品尺码（可测）
	 */
	private class ProductSizeTask extends AsyncTask<Integer, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int id = params[0];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", id);
			String url = "/product/size/list.do";
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
			if (TextUtil.isEmptyString(result)) {
				ToastUtil.showToast(ProductDetailsActivity.this, "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);
			if (resEntity.getStatus() == 200) {
				String jsonDataSize = resEntity.getData();
				String jsonSize = JsonUtil.getJsonValueByKey(jsonDataSize,
						"sysSizeList");
				String jsonSizeDetail = JsonUtil.getJsonValueByKey(
						jsonDataSize, "sizeStandardDetailList");
				listSizes = JsonUtil.toObjectList(jsonSize, ProductSize.class);
				listSizeDetails = JsonUtil.toObjectList(jsonSizeDetail,
						ProductSizeDetail.class);
				final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.layout_size); //
				for (ProductSize pc : listSizes) {
					Button btnSize = new Button(ProductDetailsActivity.this);
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					layoutParams.gravity = Gravity.CENTER_VERTICAL;
					layoutParams.leftMargin = 12;
					layoutParams.rightMargin = 18;
					layoutParams.topMargin = 6;
					btnSize.setLayoutParams(layoutParams);
					String size = pc.size;
					btnSize.setText(size);
					btnSize.setBackgroundResource(R.drawable.sizebutton_selector);
					// 添加imgColor到布局
					linearLayout.addView(btnSize);
				}// for
					// //////尺码详情
				final LinearLayout linearLayout1 = (LinearLayout) findViewById(R.id.layout_size_detail);
				final LinearLayout linearLayout2 = (LinearLayout) findViewById(R.id.layout_p1_detail);
				final LinearLayout linearLayout3 = (LinearLayout) findViewById(R.id.layout_p2_detail);
				final LinearLayout linearLayout4 = (LinearLayout) findViewById(R.id.layout_p4_detail);
				final LinearLayout linearLayout5 = (LinearLayout) findViewById(R.id.layout_p5_detail);
				final LinearLayout linearLayout6 = (LinearLayout) findViewById(R.id.layout_p6_detail);
				for (ProductSizeDetail psd : listSizeDetails) {
					TextView tvSize = new TextView(ProductDetailsActivity.this);
					TextView tvP1 = new TextView(ProductDetailsActivity.this);
					TextView tvP2 = new TextView(ProductDetailsActivity.this);
					TextView tvP4 = new TextView(ProductDetailsActivity.this);
					TextView tvP5 = new TextView(ProductDetailsActivity.this);
					TextView tvP6 = new TextView(ProductDetailsActivity.this);
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT);
					LinearLayout.LayoutParams layoutParamLine = new LinearLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT);
					layoutParams.gravity = Gravity.CENTER;
					layoutParams.leftMargin = 6;
					layoutParams.topMargin = 12;
					// 表格线条
					ImageView imgLine1 = new ImageView(
							ProductDetailsActivity.this);
					ImageView imgLine2 = new ImageView(
							ProductDetailsActivity.this);
					ImageView imgLine3 = new ImageView(
							ProductDetailsActivity.this);
					ImageView imgLine4 = new ImageView(
							ProductDetailsActivity.this);
					ImageView imgLine5 = new ImageView(
							ProductDetailsActivity.this);
					ImageView imgLine6 = new ImageView(
							ProductDetailsActivity.this);
					imgLine1.setBackgroundResource(R.drawable.horizontal_line);
					imgLine2.setBackgroundResource(R.drawable.horizontal_line);
					imgLine3.setBackgroundResource(R.drawable.horizontal_line);
					imgLine4.setBackgroundResource(R.drawable.horizontal_line);
					imgLine5.setBackgroundResource(R.drawable.horizontal_line);
					imgLine6.setBackgroundResource(R.drawable.horizontal_line);
					imgLine1.setLayoutParams(layoutParamLine);
					imgLine2.setLayoutParams(layoutParamLine);
					imgLine3.setLayoutParams(layoutParamLine);
					imgLine4.setLayoutParams(layoutParamLine);
					imgLine5.setLayoutParams(layoutParamLine);
					imgLine6.setLayoutParams(layoutParamLine);
					//
					tvSize.setLayoutParams(layoutParams);
					tvP1.setLayoutParams(layoutParams);
					tvP2.setLayoutParams(layoutParams);
					tvP4.setLayoutParams(layoutParams);
					tvP5.setLayoutParams(layoutParams);
					tvP6.setLayoutParams(layoutParams);
					// 设置表格内容颜色
					tvSize.setTextColor(Color.WHITE);
					tvP1.setTextColor(Color.WHITE);
					tvP2.setTextColor(Color.WHITE);
					tvP4.setTextColor(Color.WHITE);
					tvP5.setTextColor(Color.WHITE);
					tvP6.setTextColor(Color.WHITE);
					String size = psd.size;
					String p1 = psd.p1;
					String p2 = psd.p2;
					String p4 = psd.p4;
					String p5 = psd.p5;
					String p6 = psd.p6;
					tvSize.setText(size);
					tvP1.setText(p1);
					tvP2.setText(p2);
					tvP4.setText(p4);
					tvP5.setText(p5);
					tvP6.setText(p6);
					linearLayout1.addView(tvSize);
					linearLayout2.addView(tvP1);
					linearLayout3.addView(tvP2);
					linearLayout4.addView(tvP4);
					linearLayout5.addView(tvP5);
					linearLayout6.addView(tvP6);
					// 添加进线条
					linearLayout1.addView(imgLine1);
					linearLayout2.addView(imgLine2);
					linearLayout3.addView(imgLine3);
					linearLayout4.addView(imgLine4);
					linearLayout5.addView(imgLine5);
					linearLayout6.addView(imgLine6);
				}
				// 设置size按钮点击事件
				for (int i = 1; i < linearLayout.getChildCount(); i++) {
					final Button bSize = (Button) linearLayout.getChildAt(i);
					final int n = i;
					bSize.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							frgmt = new RightFragment();
							sizeValue = listSizes.get(n - 1).size;// 通过传值随时更新
							args.putString("colorValue", colorValue);
							args.putString("sizeValue", sizeValue);
							frgmt.setArguments(args);
							getSupportFragmentManager().beginTransaction()
									.replace(R.id.menu_frame, frgmt).commit();

							sizeId = listSizes.get(n - 1).id;
							for (int j = 1; j < linearLayout.getChildCount(); j++) {
								if (bSize == linearLayout.getChildAt(j)) {
									bSize.setBackgroundResource(R.drawable.sizeselectbuttondown);
									Toast.makeText(
											ProductDetailsActivity.this,
											"点击了" + "id+:" + id + " "
													+ "colorId+:" + colorId
													+ " " + "sizeId+:" + sizeId
													+ sizeValue,
											Toast.LENGTH_SHORT).show();
								} else {
									linearLayout
											.getChildAt(j)
											.setBackgroundResource(
													R.drawable.sizeselectbuttonup);
								}
							}
							new ProductQuantityTask().execute(id, colorId,
									sizeId);
						}
					});

				}
			} //

			super.onPostExecute(result);
		}
	}

	/*
	 * • 获取产品尺码的库存(可测)
	 */
	private class ProductQuantityTask extends AsyncTask<Integer, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int id = params[0];
			int colorId = params[1];
			int sizeId = params[2];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", id);
			args.put("colorId", colorId);
			args.put("sizeId", sizeId);
			String url = "/product/quantity/get.do";
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
			if (TextUtil.isEmptyString(result)) {
				ToastUtil.showToast(ProductDetailsActivity.this, "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);
			if (resEntity.getStatus() == 200) {
				String jsonDataQty = resEntity.getData();
				// System.out.println("rs :" + jsonDataQty);
				ProductQuantity pQty = JsonUtil.toObject(jsonDataQty,
						ProductQuantity.class);
				TextView tvQty = (TextView) findViewById(R.id.tv_qty_value);
				// ////////////////////////////////////////////////////////////
				try {
					qtyId = pQty.id;// ////
					qty = pQty.qty;
					tvQty.setText("（库存" + qty + "件）");
				} catch (Exception e) {
					tvQty.setText("（库存" + 0 + "件）");
				}
			}
			super.onPostExecute(result);
		}
	}

	/*
	 * •列出产品科技点（可测）
	 */
	private class ProductLabsTask extends AsyncTask<Integer, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int id = params[0];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", id);
			String url = "/product/labs/list.do";
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
			if (TextUtil.isEmptyString(result)) {
				ToastUtil.showToast(ProductDetailsActivity.this, "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);
			if (resEntity.getStatus() == 200) {
				String jsonDataLab = resEntity.getData();
				listLabs = JsonUtil
						.toObjectList(jsonDataLab, ProductLabs.class);
				ListView lvLabsData = (ListView) findViewById(R.id.lv_product_labs);
				lvLabsData.setAdapter(new LabsAdapter());
			}
			super.onPostExecute(result);
		}
	}

	// 产品科技点list适配器
	public class LabsAdapter extends BaseAdapter {

		@Override
		public int getCount() {

			return listLabs.size();
		}

		@Override
		public Object getItem(int position) {
			return listLabs.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			ViewHolder vh = new ViewHolder();
			if (view == null) {
				view = getLayoutInflater().inflate(
						R.layout.view_product_labs_item, null);
				vh.imgLab = (ImageView) view.findViewById(R.id.img_product_lab);
				vh.tvLab = (TextView) view.findViewById(R.id.tv_product_lab);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			ProductLabs pl = listLabs.get(position);
			String imgPath = "http://www.bulo2bulo.com" + pl.imageUrl
					+ "_S.jpg";
			new ImageFetcher().fetch(imgPath, vh.imgLab);
			vh.tvLab.setText(pl.title);
			return view;
		}

		// 常用做法，提供子项视图中的控件集
		private class ViewHolder {
			ImageView imgLab;
			TextView tvLab;
		}
	}

	/*
	 * • 购物车添加/更新
	 */
	private class ShoppingAddTask extends AsyncTask<Integer, Void, String> {
		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// 显示通知等待
			dialog = ProgressDialog.show(ProductDetailsActivity.this, "请稍候",
					"正在请求购买");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			// 购买数量
			int qc = params[0];
			String url = "/shoppingcart/update.do";
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", XbionicApp.token);
			args.put("userId", XbionicApp.userId);
			args.put("productQuantityId", qtyId);
			args.put("qty", qc);
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
				ToastUtil.showToast(ProductDetailsActivity.this, "输入为空");
				return;
			}
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);

			if (resEntity.getStatus() == 200) {
				String jsonData = resEntity.getData();
				Log.i("buyData", jsonData);
				Toast.makeText(ProductDetailsActivity.this, "您已将产品成功添加到购物车",
						Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.product_details, menu);
		return true;
	}

}
