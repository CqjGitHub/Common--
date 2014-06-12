package com.imcore.common.frgmt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.imcore.common.R;
import com.imcore.common.http.HttpHelper;
import com.imcore.common.http.HttpMethod;
import com.imcore.common.http.RequestEntity;
import com.imcore.common.http.ResponseJsonEntity;
import com.imcore.common.image.ImageFetcher;
import com.imcore.common.model.Comment;
import com.imcore.common.model.Honor;
import com.imcore.common.model.News;
import com.imcore.common.ui.ShoppingCartActivity;
import com.imcore.common.util.JsonUtil;
import com.imcore.common.util.TextUtil;
import com.imcore.common.util.ToastUtil;

public class RightFragment extends Fragment implements OnClickListener {
	private int id;
	private String colorValue;
	private String sizeValue;
	private View mView;
	private List<Comment> listComments;
	private List<News> listNews;
	private List<Honor> listHonors;
	private ListView lvData;
	private Button btnComment;
	private Button btnNew;
	private Button btnHonor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_right, null);
		lvData = (ListView) mView.findViewById(R.id.lv_product_detail_data);
		btnComment = (Button) mView.findViewById(R.id.commnet);
		btnNew = (Button) mView.findViewById(R.id.news);
		btnHonor = (Button) mView.findViewById(R.id.honor);
		Button btnBuyCar = (Button) mView.findViewById(R.id.buy_car);
		btnBuyCar.setOnClickListener(this);
		initValidata();
		new CommnentTask().execute(id);
		new NewsTask().execute(id);
		new HonorTask().execute(id);
		return mView;
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.buy_car) {
			Intent intent = new Intent(getActivity(),
					ShoppingCartActivity.class);
			startActivity(intent);
		}

	}

	/**
	 * 获取数值
	 */
	private void initValidata() {
		Bundle args = getArguments();
		id = args.getInt("id");
		colorValue = args.getString("colorValue");
		sizeValue = args.getString("sizeValue");
	}

	/*
	 * • 客户评价(可测)
	 */
	private class CommnentTask extends AsyncTask<Integer, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int id = params[0];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", id);
			String url = "/product/comments/list.do";
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
				ToastUtil.showToast(getActivity(), "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);
			if (resEntity.getStatus() == 200) {
				String jsonDataComment = resEntity.getData();
				listComments = JsonUtil.toObjectList(jsonDataComment,
						Comment.class);
				lvData.setAdapter(new CommentAdapter());
				btnComment.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						lvData.setAdapter(new CommentAdapter());
						btnComment
								.setBackgroundResource(R.drawable.table_host_press);
						btnNew.setBackgroundResource(R.drawable.table_host);
						btnHonor.setBackgroundResource(R.drawable.table_host);
					}
				});
			}
			super.onPostExecute(result);
		}
	}

	/*
	 * • 产品新闻(可测)
	 */
	private class NewsTask extends AsyncTask<Integer, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int id = params[0];
			Map<String, Object> args = new HashMap<String, Object>();
			args.put("id", id);
			String url = "/news/list.do";
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
				ToastUtil.showToast(getActivity(), "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);
			if (resEntity.getStatus() == 200) {
				String jsonDataNews = resEntity.getData();
				listNews = JsonUtil.toObjectList(jsonDataNews, News.class);
				btnNew.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						initValidata();
						lvData.setAdapter(new NewsAdapter());
						btnComment.setBackgroundResource(R.drawable.table_host);
						btnNew.setBackgroundResource(R.drawable.table_host_press);
						btnHonor.setBackgroundResource(R.drawable.table_host);
					}
				});
			}
			super.onPostExecute(result);
		}
	}

	/*
	 * • 科技和荣耀(可测)
	 */

	private class HonorTask extends AsyncTask<Integer, Void, String> {
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Integer... params) {
			int id = params[0];
			Map<String, Object> args = new HashMap<String, Object>();
			String url = "/honor/list.do";
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
			if (TextUtil.isEmptyString(result)) {
				ToastUtil.showToast(getActivity(), "内容为空");
				return;
			}
			// 将json纯文本拿到ResponseJsonEntity.fromJSON(result)进行解析，然后放入其实体里
			ResponseJsonEntity resEntity = ResponseJsonEntity.fromJSON(result);
			if (resEntity.getStatus() == 200) {
				String jsonDataHonor = resEntity.getData();
				listHonors = JsonUtil.toObjectList(jsonDataHonor, Honor.class);
				btnHonor.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						lvData.setAdapter(new HonorAdapter());
						btnComment.setBackgroundResource(R.drawable.table_host);
						btnNew.setBackgroundResource(R.drawable.table_host);
						btnHonor.setBackgroundResource(R.drawable.table_host_press);
					}
				});
			}
			super.onPostExecute(result);
		}
	}

	// 设置commnet适配器
	class CommentAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listComments.size();
		}

		@Override
		public Object getItem(int position) {
			return listComments.get(position);
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
						R.layout.view_item_comment, null);
				vh = new ViewHolder();
				vh.tvComment = (TextView) view
						.findViewById(R.id.tv_comment_content);
				vh.tvCommentDate = (TextView) view
						.findViewById(R.id.tv_comment_date);
				vh.tvAuthor = (TextView) view
						.findViewById(R.id.tv_comment_author);
				vh.tvColor = (TextView) view
						.findViewById(R.id.tv_comment_color);
				vh.tvSize = (TextView) view.findViewById(R.id.tv_comment_size);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			Comment c = listComments.get(position);
			vh.tvComment.setText(c.comment);
			vh.tvCommentDate.setText(c.commentDate);
			vh.tvAuthor.setText(c.lastName + c.firstName);
			vh.tvColor.setText(colorValue);
			vh.tvSize.setText(sizeValue);
			return view;
		}

		private class ViewHolder {
			TextView tvAuthor;
			TextView tvCommentDate;
			TextView tvComment;
			TextView tvColor;
			TextView tvSize;
		}
	}

	// 设置news适配器
	class NewsAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listNews.size();
		}

		@Override
		public Object getItem(int position) {
			return listNews.get(position);
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
						R.layout.view_item_news, null);
				vh = new ViewHolder();
				vh.imgPic = (ImageView) view.findViewById(R.id.img_new_pic);
				vh.tvTitle = (TextView) view.findViewById(R.id.tv_new_title);
				vh.tvDate = (TextView) view.findViewById(R.id.tv_new_date);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			News n = listNews.get(position);
			String imgPath = "http://bulo2bulo.com/" + n.imageUrl + "_S.jpg";
			new ImageFetcher().fetch(imgPath, vh.imgPic);
			vh.tvTitle.setText(n.title);
			vh.tvDate.setText(n.newsDate);
			return view;
		}

		private class ViewHolder {
			ImageView imgPic;
			TextView tvTitle;
			TextView tvDate;
		}
	}

	// 设置honor适配器
	class HonorAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return listHonors.size();
		}

		@Override
		public Object getItem(int position) {
			return listHonors.get(position);
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
						R.layout.view_item_honor, null);
				vh = new ViewHolder();
				vh.imgPic = (ImageView) view.findViewById(R.id.img_honor_pic);
				vh.tvTitle = (TextView) view.findViewById(R.id.tv_honor_title);
				view.setTag(vh);
			} else {
				vh = (ViewHolder) view.getTag();
			}
			Honor h = listHonors.get(position);
			String imgPath = "http://bulo2bulo.com/" + h.imageUrl + "_S.jpg";
			new ImageFetcher().fetch(imgPath, vh.imgPic);
			vh.tvTitle.setText(h.title);
			return view;
		}

		private class ViewHolder {
			ImageView imgPic;
			TextView tvTitle;
		}
	}

}
