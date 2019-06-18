package com.gupao.test;

import com.gupao.context.TApplicationContext;
import com.gupao.demo.service.impl.QueryService;

public class Test {

    public static void main(String [] args){

        TApplicationContext context = new TApplicationContext("classpath:application.properties");

        System.out.println(context);

        try {
            Object object = context.getBean(QueryService.class);

            System.out.println(object);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
