package com.imcore.common.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
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
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imcore.common.R;
import com.imcore.common.app.XbionicApp;
import com.imcore.common.http.HttpHelper;
import com.imcore.common.http.RequestEntity;
import com.imcore.common.http.ResponseJsonEntity;
import com.imcore.common.image.ImageFetcher;
import com.imcore.common.model.Product;
import com.imcore.common.model.ShopCart;
import com.imcore.common.model.SysColor;
import com.imcore.common.model.SysSize;
import com.imcore.common.util.JsonUtil;
import com.imcore.common.util.TextUtil;
import com.imcore.common.util.ToastUtil;

public class ShoppingCartActivity extends Activity implements OnClickListener {
	private List<ShopCart> listShopCarts;
	private List<Product> listProducts;
	private List<SysColor> listSysColor;
	private List<SysSize> listSysSize;

	private ListView lvData;
	private TextView tvWares;;

	private Button btnPencle;
	private Button btnAccount;

	private int cartId;
	private int positionId;// 库存Id

	private ShopCarAdapter adapter;

	private float wares = 0.0f;
	private int i;

	// private Handler handler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shopping_cart);

		lvData = (ListView) findViewById(R.id.lv_shop_data);
		tvWares = (TextView) findViewById(R.id.wares_product_shop);

		btnPencle = (Button) findViewById(R.id.btn_pencil_shop);
		btnAccount = (Button) findViewById(R.id.btn_accounts);

		// ibRemove = (ImageButton) findViewById(R.id.img_shop_remove);
		btnAccount.setOnClickListener(this);

		new ShoppingCartTask().execute();

		btnPencle.setOnClickListener(this);
		// 返回
		Button btnReturn = (Button) findViewById(R.id.btn_return_shop);
		btnReturn.setOnClickListener(this);

		lvData.setOnItemClickListener(new LvDataListener());

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 结算
		case R.id.btn_accounts:
			Intent intent = new Intent(ShoppingCartActivity.this,
					OrderActivity.class);
			startActivity(intent);
			break;
		// 显示隐藏按钮并且操作
		case R.id.btn_pencil_shop:
			lvData.setAdapter(new AllShowAdapter());
			break;
		// 删除购物订单
		case R.id.ib_remove:
			cartId = listShopCarts.get(positionId).id;
			new RemoveShopTask().execute(cartId);
			break;
		// 返回上一级
		case R.id.btn_return_shop:
			finish();
			break;
		default:
			break;
		}

	}

	// • 购物车查询
	private class ShoppingCartTask extends AsyncTask<Void, Void, String> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// 显示通知等待
			dialog = ProgressDialog.show(ShoppingCartActivity.this, "请稍候",
					"数据加载中……");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			String url = "/shoppingcart/list.do";
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", XbionicApp.token);
			args.put("userId", XbionicApp.userId);
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
			listProducts = new ArrayList<Product>();
			listSysColor = new ArrayList<SysColor>();
			listSysSize = new ArrayList<SysSize>();
			// 响应回来之后构建ResponseEntity(响应实体)
			if (TextUtil.isEmptyString(result)) {
				ToastUtil.showToast(ShoppingCartActivity.this, "输入为空");
				return;
			}
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);

			if (resEntity.getStatus() == 200) {
				String jsonData = resEntity.getData();
				listShopCarts = JsonUtil.toObjectList(jsonData, ShopCart.class);
				Log.i("shoppingSize", listShopCarts.size() + "");

				int postage = 0;
				for (ShopCart shopCart : listShopCarts) {
					Product product = JsonUtil.toObject(shopCart.product,
							Product.class);
					listProducts.add(product);
					// 累加邮费
					postage = postage + shopCart.postage
							* listShopCarts.get(i).qty;
				}
				for (Product product : listProducts) {
					SysColor sysColor = JsonUtil.toObject(product.sysColor,
							SysColor.class);
					listSysColor.add(sysColor);
					SysSize sysSize = JsonUtil.toObject(product.sysSize,
							SysSize.class);
					listSysSize.add(sysSize);
				}
				adapter = new ShopCarAdapter();
				lvData.setAdapter(adapter);
				//
				// 邮费
				TextView tvPostage = (TextView) findViewById(R.id.postage_value);
				tvPostage.setText("￥" + postage);
				tvPostage.invalidate();
				// 结算总价
				for (Product product : listProducts) {
					float f = product.price;
					float fq = f * listShopCarts.get(i).qty;
					wares = wares + fq;
				}
				tvWares.setText(wares + postage + "");
				// 消息循环机制
				// handler = new Handler() {
				// @Override
				// public void handleMessage(Message msg) {
				// msg.what = 1;
				// tvWares.invalidate();
				// super.handleMessage(msg);
				// }
				// };
				// tvWares.invalidate();
			}
			super.onPostExecute(result);
		}
	}

	// 设置listView监听
	private class LvDataListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			positionId = position;
		}
	}

	// 自定义适配器显示购物车listview
	private class ShopCarAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listShopCarts.size();
		}

		@Override
		public Object getItem(int position) {
			return listShopCarts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			i = position;
			View view = convertView;
			ViewHolder vh = null;
			if (view == null) {
				view = getLayoutInflater().inflate(
						R.layout.view_shop_cart_item, null);
				vh = new ViewHolder();
				vh.imgPic = (ImageView) view.findViewById(R.id.img_shop_car);
				vh.tvTitle = (TextView) view
						.findViewById(R.id.tv_shop_product_title);
				vh.tvColor = (TextView) view
						.findViewById(R.id.tv_color_product);
				vh.tvSize = (TextView) view.findViewById(R.id.tv_size_product);
				vh.tvPrice = (TextView) view.findViewById(R.id.tv_total_price);
				vh.tvCount = (TextView) view.findViewById(R.id.btn_count);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			Product product = listProducts.get(position);
			String imgPath = "http://bulo2bulo.com/" + product.imageUrl
					+ "_L.jpg";
			new ImageFetcher().fetch(imgPath, vh.imgPic);
			vh.tvTitle.setText(product.name);
			vh.tvColor.setText(listSysColor.get(position).color);
			vh.tvSize.setText(listSysSize.get(position).size);
			vh.tvPrice.setText(product.price * listShopCarts.get(position).qty
					+ "");
			vh.tvCount.setText(listShopCarts.get(position).qty + "");
			//

			return view;

		}

		private class ViewHolder {
			ImageView imgPic;
			TextView tvTitle;
			TextView tvColor;
			TextView tvSize;
			TextView tvPrice;
			TextView tvCount;
		}
	}

	// 自定义适配器显示隐藏的按钮
	private class AllShowAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listShopCarts.size();
		}

		@Override
		public Object getItem(int position) {
			return listShopCarts.get(position);
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
				view = getLayoutInflater().inflate(
						R.layout.view_shop_cart_item, null);
				vh = new ViewHolder();
				vh.imgPic = (ImageView) view.findViewById(R.id.img_shop_car);
				vh.tvTitle = (TextView) view
						.findViewById(R.id.tv_shop_product_title);
				vh.tvColor = (TextView) view
						.findViewById(R.id.tv_color_product);
				vh.tvSize = (TextView) view.findViewById(R.id.tv_size_product);
				vh.tvPrice = (TextView) view.findViewById(R.id.tv_total_price);
				vh.tvCount = (TextView) view.findViewById(R.id.btn_count);

				vh.btnLeft = (ImageButton) view.findViewById(R.id.btn_left);
				vh.btnRight = (ImageButton) view.findViewById(R.id.btn_right);
				vh.ibRemove = (ImageButton) view.findViewById(R.id.ib_remove);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			Product product = listProducts.get(position);
			String imgPath = "http://bulo2bulo.com/" + product.imageUrl
					+ "_L.jpg";
			new ImageFetcher().fetch(imgPath, vh.imgPic);
			vh.tvTitle.setText(product.name);
			vh.tvColor.setText(listSysColor.get(position).color);
			vh.tvSize.setText(listSysSize.get(position).size);
			vh.tvPrice.setText(product.price * listShopCarts.get(position).qty
					+ "");
			vh.tvCount.setText(listShopCarts.get(position).qty + "");

			// 显示
			vh.btnLeft.setVisibility(View.VISIBLE);
			vh.btnRight.setVisibility(View.VISIBLE);
			vh.ibRemove.setVisibility(View.VISIBLE);
			vh.ibRemove.setOnClickListener(ShoppingCartActivity.this);

			return view;

		}

		private class ViewHolder {
			ImageView imgPic;
			TextView tvTitle;
			TextView tvColor;
			TextView tvSize;
			TextView tvPrice;
			TextView tvCount;

			ImageButton btnLeft;
			ImageButton btnRight;
			ImageButton ibRemove;
		}
	}

	private class RemoveShopTask extends AsyncTask<Integer, Void, Integer> {
		private int status;

		@Override
		protected Integer doInBackground(Integer... params) {
			String url = "/shoppingcart/delete.do";
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("token", XbionicApp.token);
			args.put("userId", XbionicApp.userId);
			args.put("cartId", params[0]);
			RequestEntity entity = new RequestEntity(url, args);
			String json = null;
			try {
				json = HttpHelper.execute(entity);
				ResponseJsonEntity responseJsonEntity = ResponseJsonEntity
						.fromJSON(json);
				status = responseJsonEntity.getStatus();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return status;
		}

		@Override
		protected void onPostExecute(Integer result) {
			if (status == 200) {
				if (listShopCarts != null) {
					// 移除更新
					listShopCarts.remove(listShopCarts.get(positionId));
					adapter.notifyDataSetChanged();
					lvData.setAdapter(adapter);
					// 刷新结算
					// Message msg = Message.obtain();
					// msg.what = 1;
					// handler.sendMessage(msg);

				}
			}
			super.onPostExecute(result);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.shopping_cart, menu);
		return true;
	}

}
