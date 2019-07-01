package com.gupao;

import com.gupao.frame.context.YApplicationContext;

/**
 * @author yangTan
 * @date 2019/7/1
 */
public class Test {

    public static void main(String[] args){

        YApplicationContext applicationContext = new YApplicationContext("classpath:application.properties");

        System.out.println(applicationContext);

    }
}
