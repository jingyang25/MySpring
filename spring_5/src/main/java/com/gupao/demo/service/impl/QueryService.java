package com.gupao.demo.service.impl;

import com.gupao.demo.service.IQueryService;
import com.gupao.frame.annotation.YService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 查询业务
 * @author Tom
 *
 */
@YService

public class QueryService implements IQueryService {

	/**
	 * 查询
	 */
	public String query(String name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		String json = "{name:\"" + name + "\",time:\"" + time + "\"}";
		//log.info("这是在业务方法中打印的：" + json);
		return json;
	}

}
