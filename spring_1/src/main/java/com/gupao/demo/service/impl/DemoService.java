package com.gupao.demo.service.impl;

import com.gupao.demo.service.IDemoService;
import com.gupao.sprinmvc.annotation.YService;

/**
 * 核心业务逻辑
 */
@YService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name;
	}

}
