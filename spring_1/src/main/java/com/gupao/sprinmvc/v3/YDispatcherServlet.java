package com.gupao.sprinmvc.v3;

import com.gupao.sprinmvc.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 配置web.xml
 * init() 方法
 * 浏览器输入 url 会有 web 容器调用 service
 * doGet deoPost 执行逻辑
 * destory() 销毁
 */
public class YDispatcherServlet extends HttpServlet {

    //将 properties 文件中的内容保存到 contextConfig 中

    private Properties contextConfig = new Properties();

    //将扫描得到的类名 放入集合
    private List<String> classNames = new ArrayList<>();
    //ioc 容器
    private Map<String,Object> ioc =  new HashMap<>();

    // handlerMapping
    private List<Handler> handlerMapping = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        this.doPost(req,resp);
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, IOException {

        // 路径格式校验
        Handler handler = getHandler(req);

        System.out.println(handler);

        if(handler==null){
            resp.getWriter().write("404 Not Found");
            return;
        }

        //获得方法参数形参列列表
        Class<?>[] paramTypes = handler.getParamTypes();

        Object[] paramValues = new Object[paramTypes.length];

        Map<String,String[]> params = req.getParameterMap();

        for (Map.Entry<String,String[]> param: params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s",",");

            if (handler.paramIndexMapping.containsKey(param.getKey())){continue;}

            int index = handler.paramIndexMapping.get(param.getKey());

            paramValues[index] = convert(paramTypes[index],value);

        }

        //获取请求
        if(handler.paramIndexMapping.containsKey(HttpServletRequest.class.getName())){
            int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (handler.paramIndexMapping.containsKey(HttpServletResponse.class.getName())){

            int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());

            paramValues[respIndex] = resp;
        }

        Object returnValue = handler.method.invoke(handler.controller,paramTypes);
        if (returnValue==null||returnValue instanceof Void){ return;}
        resp.getWriter().write(returnValue.toString());


    }

    // url 传过来的参数都是String 类型 的 HTTP 是基于字符串协议
    // 只需要 将String 转换为任意类型就好
    private Object convert(Class<?> paramType, String value) {
        if(Integer.class==paramType){
            return Integer.valueOf(value);
        }else if(Double.class == paramType){
            return Double.valueOf(value);
        }

        return value;
    }

    private Handler getHandler(HttpServletRequest req) {

        if (handlerMapping.isEmpty()){ return null;}
        //绝对路径
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        for (Handler handler: this.handlerMapping) {
            Matcher matcher = handler.getPattern().matcher(url);

            return handler;
        }

        return null;

    }


    public void init(ServletConfig config){
        //加载配置文件


        // 初始化 handlerMapping
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        //创建实例 并保存至容器
        doInstance();
        // 依赖注入
        doAutowired();

        initHandlerMapping();




    }

    private void doLoadConfig(String contextConfigLocation) {

        //为什么这里用字节流 不用字符流
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

    //扫描哪些相关的类

    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {
            // 如果file表示的是一个目录
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) continue;
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }


        }

    }
      //  创建实例 并保存至容器
    private void doInstance(){
        if (!classNames.isEmpty()) return;

        try {
            for (String className:classNames) {

                Class<?> clazz = Class.forName(className);

                if(clazz.isAnnotationPresent(YController.class)){

                    Object instance = clazz.newInstance();
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);

                }else if(clazz.isAnnotationPresent(YService.class)) {
                    //1、自定义的beanName
                    String beanName = toLowerFirstCase(clazz.getAnnotation(YService.class).value());
                    //2、默认类名首字母小写
                    if ("".equals(beanName.trim())) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.newInstance();
                    //根据类型自动赋值 clazz.getInterfaces() 获得这个对象所实现的所有接口
                    for (Class<?> i : clazz.getInterfaces()) {
                        if (ioc.containsKey(i.getName())) {
                            throw new Exception("The" + i.getName() + "is exist!!");
                        }

                        ioc.put(beanName, instance);

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

    //
    private void doAutowired() {
        if (ioc.isEmpty()) return;

        for (Map.Entry<String,Object> entry:ioc.entrySet()) {

            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields) {
                if(!field.isAnnotationPresent(YAutowired.class)){continue;}
                YAutowired autowired = field.getAnnotation(YAutowired.class);
                //获得自定义的名称
                String beanName = autowired.value().trim();

                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }

                field.setAccessible(true);

                try {
                    //用反射机制 动态给字段赋值
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    // 初始化 url 和Method 一一对应的关系
    private void initHandlerMapping() {

        if(ioc.isEmpty()){ return;}

        for (Map.Entry<String,Object> entry:ioc.entrySet()) {

            Class<?> clazz = entry.getValue().getClass();

            if(!clazz.isAnnotationPresent(YRequestMapping.class)){continue;}

            String baseUrl = "";

            if(clazz.isAnnotationPresent(YRequestMapping.class)){
                YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            //获取所有的public 方法

            for (Method method:clazz.getMethods()) {

                YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);

                //优化 //demo // query

                String regex =("/"+ baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");

                Pattern pattern = Pattern.compile(regex);
                this.handlerMapping.add(new Handler(pattern, (Method) entry.getValue(),method));

                System.out.println("Mapped:"+pattern+","+method);


            }

        }


    }


    //保存一个url 和Method 的关系

    public class Handler{

        private Pattern pattern;

        private Method method;

        private Object controller;

        private Class<?>[] paramTypes;

        //形參列表 參數的名字作为 key 参数的顺序 位置作为值
        private Map<String,Integer> paramIndexMapping;


        public Pattern getPattern() {
            return pattern;
        }

        public Method getMethod() {
            return method;
        }

        public Object getController() {
            return controller;
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public Handler(Pattern pattern, Method method, Object controller) {
            this.pattern = pattern;
            this.method = method;
            this.controller = controller;
            this.paramTypes = method.getParameterTypes();

            paramIndexMapping = new HashMap<String,Integer>();

            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method){
            //提取方法中加了注解的参数
            //把方法上的注解拿到 得到的是一个二维数组
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i=0;i<pa.length;i++){
                for (Annotation a:pa[i]){
                    if(a instanceof YRequestParam){
                        String paramName = ((YRequestParam) a).value();
                        if(!"".equals(paramName)){
                            paramIndexMapping.put(paramName,i);
                        }
                    }
                }
            }

            Class<?> [] paramsTypes = method.getParameterTypes();

            for(int i=0;i<paramsTypes.length;i++){

                Class<?> type = paramTypes[i];
                if(type == HttpServletRequest.class||type == HttpServletResponse.class){
                    paramIndexMapping.put(type.getName(),i);
                }

            }
        }


    }





}
