package com.imcore.common.frgmt;

import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.imcore.common.R;

public class LeftFragment extends Fragment {
	private View mView;
	private ListView lvDataLeft;

	private ArrayList<String> list;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_left, null);
		lvDataLeft = (ListView) mView.findViewById(R.id.lv_data_left);
		initValidata();
		return mView;
	}

	/**
	 * 初始化变量
	 */
	private void initValidata() {
		list = new ArrayList<String>();
		list.add("您的订购");
		list.add("账户设置");
		list.add("达人申请");
		list.add("部落社区");
		list.add("购物车");
		list.add("订阅信息");
		list.add("密码设置");
		list.add("分享设置");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.view_fragm_left_item, R.id.tv_title, list);
		lvDataLeft.setAdapter(adapter);
	}

}
