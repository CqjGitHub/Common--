package com.imcore.common.model;

public class User {
	// 实体类注意事项
	// 1.访问修饰符必须是public
	// 2.字段名称得跟JSON字符串中相应的key一致，包括大小写
	// 3.注意JSON字符串中的value的类型，带双引号
	public int id;
	public String token;
	public String email;
	public String displayName;
	public String firstName;
	public String lastName;
	public String birthDate;
	public int statusId;
	public String createDate;
	public int roleId;
	public String isYogaStart;
	public String profileImage;

}
