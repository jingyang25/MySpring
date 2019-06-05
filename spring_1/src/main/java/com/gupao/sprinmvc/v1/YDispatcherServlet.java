package com.gupao.sprinmvc.v1;

import com.gupao.sprinmvc.annotation.YAutowired;
import com.gupao.sprinmvc.annotation.YController;
import com.gupao.sprinmvc.annotation.YRequestMapping;
import com.gupao.sprinmvc.annotation.YService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class YDispatcherServlet extends HttpServlet {

    private Map<String,Object> mapping = new HashMap<>();




    public void init(ServletConfig config){

        InputStream is = null;

        try {
            Properties configContext = new Properties();

            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));

            configContext.load(is);

            String scanPackage = configContext.getProperty("scanPackage");

            doScanner(scanPackage);

            for (String className:mapping.keySet()){

                if (!className.contains(".")){
                    continue;
                }
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(YRequestMapping.class)){

                    mapping.put(className,clazz.newInstance());

                    String baseUrl = "";

                    YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);

                    baseUrl = requestMapping.value();

                    Method[] methods = clazz.getMethods();

                    for (Method method :methods) {
                        if (!method.isAnnotationPresent(YRequestMapping.class)) {
                            continue;
                        }

                        YRequestMapping reqMapping = method.getAnnotation(YRequestMapping.class);

                        String url = (baseUrl + "/" + reqMapping.value()).replaceAll("/+", "/");
                        mapping.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                }else if(clazz.isAnnotationPresent(YRequestMapping.class)){

                    YService service = clazz.getAnnotation(YService.class);

                        String beanName = service.value();

                        if ("".equals(beanName)) {
                            beanName = clazz.getName();
                        }

                        Object instance = clazz.newInstance();
                        mapping.put(beanName,instance);

                        for (Class<?> i:clazz.getInterfaces()){
                            mapping.put(i.getName(),instance);

                        }

                    }else{
                        continue;
                    }

                    for (Object object:mapping.values()){

                        if (object==null){
                            continue;
                        }

                        Class clazz2 = object.getClass();
                        if (clazz2.isAnnotationPresent(YController.class)){
                            Field[] fields = clazz2.getDeclaredFields();

                            for (Field field:fields){
                                if(!field.isAnnotationPresent(YAutowired.class)){
                                    continue;
                                }
                                YAutowired autowired = field.getAnnotation(YAutowired.class);
                                String beanName = autowired.value();

                                if("".equals(beanName)){
                                    beanName = field.getType().getName();
                                }
                                try {
                                    field.set(mapping.get(clazz2.getName()),mapping.get(beanName));
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                }


                            }
                        }
                    }




            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {

            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

      System.out.println("Y MVC  Framework is init");
    }

    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader().getResource("/"+scanPackage.replaceAll("\\.","/"));

        File classDir  = new File(url.getFile());

        for (File file :classDir.listFiles()){
            if (file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if (!file.getName().endsWith(".class")){
                    continue;
                }

                String clazzName = scanPackage+"."+file.getName().replace(".class","");
                mapping.put(clazzName,null);
            }
        }

    }

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        this.doPost(req,resp);

    }


    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispath(req,resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


    }


        public void doDispath(HttpServletRequest request,HttpServletResponse response) throws IOException, InvocationTargetException, IllegalAccessException {

        String url = request.getRequestURI();

        String contextPath = request.getContextPath();

        url = url.replace(contextPath,"").replaceAll("/+","/");

        if (!this.mapping.containsKey(url)){
            response.getWriter().write("404 Not Found");
            return;
        }

            Method method = (Method) this.mapping.get(url);

            Map<String,String[]> params = request.getParameterMap();
            method.invoke(this.mapping.get(method.getDeclaringClass().getName()), new Object[]{request,response,params.get("name")[0]});


        }

}
