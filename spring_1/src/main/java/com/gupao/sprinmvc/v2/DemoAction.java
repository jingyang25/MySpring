package com.gupao.sprinmvc.v2;

import com.gupao.sprinmvc.annotation.YController;
import com.gupao.sprinmvc.annotation.YRequestMapping;
import com.gupao.sprinmvc.annotation.YRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@YController
@YRequestMapping("/demo")
public class DemoAction {

    private IDemoService iDemoService;


    @YRequestMapping("/query")
    public void query(HttpServletRequest request, HttpServletResponse response, @YRequestParam String name){

        String result = iDemoService.get(name);

        try {
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @YRequestMapping("/add")
    public void add(HttpServletRequest request,HttpServletResponse response,@YRequestParam("a") Integer a,@YRequestParam("b") Integer b){

        try {
            response.getWriter().write(a+"+"+b+"="+(a+b));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    @YRequestMapping("/remove")
    public void remove(HttpServletRequest request,HttpServletResponse response,@YRequestParam("a") Integer a){


    }
}
