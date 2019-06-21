package com.gupao.demo.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gupao.demo.service.IDemoService;
import com.gupao.sprinmvc.annotation.YAutowired;
import com.gupao.sprinmvc.annotation.YController;
import com.gupao.sprinmvc.annotation.YRequestMapping;
import com.gupao.sprinmvc.annotation.YRequestParam;


//虽然，用法一样，但是没有功能
@YController
@YRequestMapping("/demo")
public class DemoAction {

  	@YAutowired
    private IDemoService demoService;

	@YRequestMapping("/query.*")
	public void query(HttpServletRequest req, HttpServletResponse resp,
					  @YRequestParam("name") String name){
//		String result = demoService.get(name);
		String result = "My name is " + name;
		try {
			resp.getWriter().write(result);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@YRequestMapping("/add")
	public void add(HttpServletRequest req, HttpServletResponse resp,
                    @YRequestParam("a") Integer a, @YRequestParam("b") Integer b){
		try {
			resp.getWriter().write(a + "+" + b + "=" + (a + b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@YRequestMapping("/sub")
	public void add(HttpServletRequest req, HttpServletResponse resp,
					@YRequestParam("a") Double a, @YRequestParam("b") Double b){
		try {
			resp.getWriter().write(a + "-" + b + "=" + (a - b));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@YRequestMapping("/remove")
	public String  remove(@YRequestParam("id") Integer id){
		return "" + id;
	}

}
