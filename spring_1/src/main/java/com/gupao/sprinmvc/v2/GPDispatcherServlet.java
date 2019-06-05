package com.gupao.sprinmvc.v2;


import com.gupao.sprinmvc.annotation.*;

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
import java.util.*;

public class GPDispatcherServlet extends HttpServlet {



    //初始化
    public void init(ServletConfig config){

        //加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));

        //初始化扫描到的类，兵器将它们放入到ICO容器中
        doScanner(contextConfig.getProperty("scanPackage"));

        //初始化扫描到的类，并且将它们放到IOC容器中
        doInstance();

        //完成依赖注入
        doAutowired();

        //初始化HandlerMapping
        initHandlerMapping();

        System.out.println("Gp Spring framework is init");
    }

    //初始化 url和Method的一对一的关系
    private void initHandlerMapping() {

        if(ioc.isEmpty()){
            return;
        }

        for (Map.Entry<String,Object> entry :ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(YController.class)){continue;}

            String baseUrl = "";
            if (clazz.isAnnotationPresent(YRequestMapping.class)){
                YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);

                baseUrl = requestMapping.value();
            }

            for (Method method :clazz.getMethods()){

                if (!method.isAnnotationPresent(YRequestMapping.class)){
                      continue;
              }

              YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);
                String url = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");
                handlerMapping.put(url,method);
                System.out.println("Mapped:"+url+","+method);
            }
        }
    }

    //自动依赖注入
    private void doAutowired() {
        if (ioc.isEmpty()){
            return;
        }

        for (Map.Entry<String,Object> entry :ioc.entrySet()){

            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields){
                if (!field.isAnnotationPresent(YAutowired.class)){
                    continue;
                }

                YAutowired autowired = field.getAnnotation(YAutowired.class);

                String beanName = autowired.value();

                if ("".equals(beanName)){

                    beanName = field.getType().getName();

                }

                field.setAccessible(true);

                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    private void doInstance() {

        //初始化 为DI做准备
        if(classNames.isEmpty()){
            return;
        }

        try {
            for (String className:classNames){

                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(YController.class)){
                    Object instance = clazz.newInstance();

                    String beanName = toLowerFirstCase(clazz.getSimpleName());

                    ioc.put(beanName,instance);
                }else if(clazz.isAnnotationPresent(YService.class)) {
                    YService service = clazz.getAnnotation(YService.class);

                    String beanName = service.value();

                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();

                    ioc.put(beanName, instance);

                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The" + i.getName() + "" + "is exists");
                        }

                        ioc.put(i.getName(), instance);
                    }
                }else{
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String toLowerFirstCase(String simpleName) {

        char[] chars = simpleName.toCharArray();

        chars[0]+=32;

        return String.valueOf(chars);

    }

    //扫描相关的类
    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader().getResource(scanPackage);

        scanPackage.replaceAll("\\.","/");

        File classPath = new File(url.getFile());

        for (File file :classPath.listFiles()){
            if (file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if (!file.getName().endsWith(".class")){
                    continue;
                }

                String className = scanPackage+"."+file.getName().replace(".class","");

                classNames.add(className);

            }
        }
    }

    //声明全局变量
    Properties contextConfig = new Properties();

    //保存扫描的所有的类名
    private List<String> classNames = new ArrayList<String>();

    //IOC 容器

    private Map<String,Object> ioc = new HashMap<String,Object>();

    //保存 url和Method的对应关系
    private Map<String,Object> handlerMapping = new HashMap<>();


    private void doLoadConfig(String contextConfigLocation){

        //字节流包含两个顶层抽象类：InputStream和OutputStream

        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);

        try {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(null!=fis){

                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    private void doDispath(HttpServletRequest req, HttpServletResponse resp) throws Exception {

        String url = req.getRequestURI();

        String contextPath = req.getContextPath();

        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if (!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found");

            return;
        }

        Method method= (Method) this.handlerMapping.get(url);

        Map<String,String[]> params = req.getParameterMap();

        Class<?> [] parameterTypes = method.getParameterTypes();

        Object[] paramValues = new Object[parameterTypes.length];

        for (int i=0;i<parameterTypes.length;i++){

            Class parameterType = parameterTypes[i];

            if (parameterType == HttpServletRequest.class){
                paramValues[i] = req;
                continue;
            }else if(parameterType ==HttpServletResponse.class){
                paramValues[i] = req;
                continue;
            }else if (parameterType ==String.class){
                YRequestParam requestParam = (YRequestParam) parameterType.getAnnotation(YRequestParam.class);
                if (params.containsKey(requestParam.value())) {
                    for (Map.Entry<String,String[]> param :params.entrySet()){

                        String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","")
                                .replaceAll("\\s",",");
                        paramValues[i] = value;

                    }
                }


            }
        }
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());

        method.invoke(ioc.get(beanName),new Object[]{req,resp,params.get("name")[0]});

    }


}
