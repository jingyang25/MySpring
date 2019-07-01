package com.gupao.demo.action;

import com.gupao.demo.service.IModifyService;
import com.gupao.demo.service.IQueryService;
import com.gupao.frame.annotation.YAutowired;
import com.gupao.frame.annotation.YController;
import com.gupao.frame.annotation.YRequestMapping;
import com.gupao.frame.annotation.YRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


/**
 * 公布接口url
 * @author Tom
 *
 */
@YController
@YRequestMapping("/web")
public class MyAction {

	@YAutowired
    IQueryService queryService;
	@YAutowired
    IModifyService modifyService;

	@YRequestMapping("/query.json")
	public void query(HttpServletRequest request, HttpServletResponse response,
								@YRequestParam("name") String name){
		String result = queryService.query(name);
		out(response,result);
	}
	
	@YRequestMapping("/add*.json")
	public void add(HttpServletRequest request, HttpServletResponse response,
                    @YRequestParam("name") String name, @YRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		out(response,result);
	}
	
	@YRequestMapping("/remove.json")
	public void remove(HttpServletRequest request,HttpServletResponse response,
		   @YRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		out(response,result);
	}
	
	@YRequestMapping("/edit.json")
	public void edit(HttpServletRequest request,HttpServletResponse response,
			@YRequestParam("id") Integer id,
			@YRequestParam("name") String name){
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
