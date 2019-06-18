package com.gupao.sprinmvc.v1;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

public class YDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();


    //2、IOC 初始化
    private Map<String,Object> ioc = new HashMap<>();

    private List<String> classNames = new ArrayList<>();

    //1、初始化
    public void init(ServletConfig config){

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("context"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scankage"));





    }

    //加载配置文件
    private void doLoadConfig(String contextConfigLocation) {

        //直接从类路径下 找到Spring主配置文件所在的路径
        //并且将其读取出来放到 Properties
        //相对于scanPackage 从文件中保存到了内存中
        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }


    //3、扫描相关的类

    public  void doScanner(String scankage){

        // scankage = com.gupao.demo 存储的是包路径
        // 转换为文件路径 实际上就是把 . 替换为/ 就OK了
        // classPath
        URL url = this.getClass().getClassLoader().getResource("/"+scankage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for (File file:classPath.listFiles()) {

            if (file.isDirectory())
            doScanner(scankage+"."+file.getName());
            else{
                if (!file.getName().endsWith(".class")){ continue;}
                String className = (scankage +"."+file.getName().replace(".class",""));
                classNames.add(className);

            }
        }

    }

    //4、创建实例并保存至容器 HandlerMapping
   public void  doInstance(){

   }

    //5、DI
    public void doAutowired(){

    }


    //6、 初始化 HandleMapping
  public void initHanlderMapping(){

  }


}
