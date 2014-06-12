package com.imcore.common.frgmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.imcore.common.R;
import com.imcore.common.http.HttpHelper;
import com.imcore.common.http.HttpMethod;
import com.imcore.common.http.RequestEntity;
import com.imcore.common.http.ResponseJsonEntity;
import com.imcore.common.image.ImageFetcher;
import com.imcore.common.model.CategoryDetail;
import com.imcore.common.ui.ProductDetailsActivity;
import com.imcore.common.util.JsonUtil;
import com.imcore.common.util.TextUtil;
import com.imcore.common.util.ToastUtil;

public class CategoryListFrgmt extends Fragment implements OnItemClickListener {
	private int navId;
	private int subNavId;
	private int id;
	private int desc;

	private List<CategoryDetail> mCategoryDetails;
	private GridView gvtDetail;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frgmt_product_list_grid, null);
		gvtDetail = (GridView) view.findViewById(R.id.gv_product_list);
		Bundle args = getArguments();

		navId = args.getInt("navId");
		subNavId = args.getInt("subNavId");
		id = args.getInt("id");
		desc = args.getInt("desc");

		new ProductDetailTask().execute(navId, subNavId, id, desc);
		gvtDetail.setOnItemClickListener(this);
		return view;
	}

	// 设置监听
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Intent intent = new Intent(getActivity(), ProductDetailsActivity.class);
		Bundle args = new Bundle();
		CategoryDetail mDetail = mCategoryDetails.get(position);
		int mId = mDetail.id;
		args.putInt("id", mId);
		// args.putInt("navId", navId);
		// args.putInt("subNavId", subNavId);
		intent.putExtras(args);
		startActivity(intent);
	}

	private class ProductDetailTask extends AsyncTask<Integer, Void, String> {

		private ProgressDialog dialog;

		@Override
		protected void onPreExecute() {
			// 显示通知等待
			dialog = ProgressDialog.show(getActivity(), "请稍候", "数据加载中……");
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int nav = params[0];
			int subNab = params[1];
			int id = params[2];
			int desc = params[3];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("navId", nav);
			args.put("subNavId", subNab);
			args.put("id", id);
			args.put("desc", desc);
			args.put("orderBy", "price");
			String url = "/category/products.do";
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
				ToastUtil.showToast(getActivity(), "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);

			if (resEntity.getStatus() == 200) {
				String jsonData = resEntity.getData();
				Log.i("CategoryDetail", jsonData);
				mCategoryDetails = JsonUtil.toObjectList(jsonData,
						CategoryDetail.class);

				gvtDetail.setAdapter(new ProductDetailAdapter());
			} //

			super.onPostExecute(result);
		}
	}

	// 自定义适配器
	public class ProductDetailAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mCategoryDetails.size();
		}

		@Override
		public Object getItem(int position) {
			return mCategoryDetails.get(position);
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
				view = getActivity().getLayoutInflater().inflate(
						R.layout.view_product_list_item, null);
				vh = new ViewHolder();
				vh.imgPic = (ImageView) view
						.findViewById(R.id.img_product_list);
				vh.tvName = (TextView) view.findViewById(R.id.tv_product_name);
				vh.tvPrice = (TextView) view
						.findViewById(R.id.tv_product_price);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			CategoryDetail cd = mCategoryDetails.get(position);
			String imgPath = "http://bulo2bulo.com/" + cd.imageUrl + "_L.jpg";
			new ImageFetcher().fetch(imgPath, vh.imgPic);
			vh.tvName.setText(cd.name);
			vh.tvPrice.setText(cd.price + "");
			return view;
		}

		private class ViewHolder {
			ImageView imgPic;
			TextView tvName;
			TextView tvPrice;
		}
	}

}
