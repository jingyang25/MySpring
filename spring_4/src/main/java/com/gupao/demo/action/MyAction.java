package com.gupao.demo.action;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.gupao.annotation.TAutowired;
import com.gupao.annotation.TController;
import com.gupao.annotation.TRequestMapping;
import com.gupao.annotation.TRequestParam;
import com.gupao.demo.service.IModifyService;
import com.gupao.demo.service.IQueryService;

/**
 * 公布接口url
 * @author Tom
 *
 */
@TController(value = "myAction")
@TRequestMapping("/web")
public class MyAction {

	@TAutowired
    IQueryService queryService;
	@TAutowired IModifyService modifyService;

	@TRequestMapping("/query.json")
	public void query(HttpServletRequest request, HttpServletResponse response,
								@TRequestParam("name") String name){
		String result = queryService.query(name);
		out(response,result);
	}
	
	@TRequestMapping("/add*.json")
	public void add(HttpServletRequest request,HttpServletResponse response,
			   @TRequestParam("name") String name,@TRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		out(response,result);
	}
	
	@TRequestMapping("/remove.json")
	public void remove(HttpServletRequest request,HttpServletResponse response,
		   @TRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		out(response,result);
	}
	
	@TRequestMapping("/edit.json")
	public void edit(HttpServletRequest request,HttpServletResponse response,
			@TRequestParam("id") Integer id,
			@TRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		out(response,result);
	}
	
	
	
	private void out(HttpServletResponse resp,String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
