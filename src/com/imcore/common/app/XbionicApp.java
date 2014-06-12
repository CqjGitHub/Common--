package com.imcore.common.app;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

import com.imcore.common.model.User;

public class XbionicApp extends Application {
	private static List<User> userList = new ArrayList<User>();
	public static int userId;
	public static String token;

	// private static List<String> inforList = new ArrayList<String>();
	// public static int shopNumber = 0;
	// public static String seniority;

	public static void addUser(User user) {
		userList.add(user);
		userId = user.id;
		token = user.token;
	}

	// public static void addInfoImages(String images) {
	// inforList.add(images);
	// }
	//
	// public static String getInfoImgesUrl(int index) {
	// return inforList.get(index);
	// }
	//
	// public static void clearInfoList() {
	// inforList.clear();
	// }
	//
	// public static int getInforListSize() {
	// return inforList.size();
	// }
}