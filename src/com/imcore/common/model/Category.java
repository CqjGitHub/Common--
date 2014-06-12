package com.imcore.common.model;

/**
 * 产品列表
 * @author Administrator
 * 
 */
public class Category {
	// 实体类注意事项
	// 1.访问修饰符必须是public
	// 2.字段名称得跟JSON字符串中相应的key一致，包括大小写
	// 3.注意JSON字符串中的value的类型，带双引号
	public int id;
	public int code;
	public String name;
	public String imageUrl;
	public int status;

}
