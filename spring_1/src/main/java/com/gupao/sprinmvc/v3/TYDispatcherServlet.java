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
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TYDispatcherServlet extends HttpServlet {

    //保存application.properties 配置文件中的内容
    private Properties contextConfig = new Properties();

    // 保存扫描的所有的类名
    private List<String> classNames = new ArrayList<>();

    private Map<String,Object> ioc = new HashMap<>();

    private List<Handler> handlerMapping = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest request,HttpServletResponse response) throws ServletException, IOException {
        this.doPost(request,response);
    }

    protected void  doPost(HttpServletRequest request,HttpServletResponse response) throws IOException {

        try {
            doDispatch(request,response);
        } catch (Exception e) {
            e.printStackTrace();

            response.getWriter().write("500 Exection,Detail:"+Arrays.toString(e.getStackTrace()));
        }

    }

    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws IOException {

        Handler handler = getHandler(request);

        if (handler==null){
            response.getWriter().write("404 not Found");
            return;
        }

        //获得方法的形参列表

        Class<?>[] paramTypes = handler.getParamTypes();
        Object[] paramValues = new Object[paramTypes.length];

        Map<String,String[]> params = request.getParameterMap();

        for (Map.Entry<String,String[]> param :params.entrySet()){
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s",",");
            if (!handler.paramIndexMapping.containsKey(param.getKey())){continue;}
             
            int index = handler.paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(paramTypes[index],value);


        }


    }

    private Object convert(Class<?> type, String value) {

        if (Integer.class ==type){
            return Integer.valueOf(value);
        }else if (Double.class==type){

            return Integer.valueOf(value);
        }

        return value;
    }

    public void init(ServletConfig config){
        //加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        
        //扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));
        
        doInstance();
        
        doAutowired();
        
        initHanlderMapping();
        
        System.out.println("GP Spring framework is init");
        
        
    }

    private void initHanlderMapping() {

        if (ioc.isEmpty()){ return;}

        for (Map.Entry<String,Object> entry :ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();

            if (!clazz.isAnnotationPresent(YController.class)){continue;}

            String baseUrl = "";
            if (clazz.isAnnotationPresent(YRequestMapping.class)){
                YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            for (Method method:clazz.getMethods()){
                if(!method.isAnnotationPresent(YRequestMapping.class)){continue;}

                YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);

                String regex = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");

                Pattern pattern = Pattern.compile(regex);
                this.handlerMapping.add(new Handler(pattern, (Method) entry.getValue(),method));
            }
        }

    }

    private void doAutowired() {

        if (ioc.isEmpty()){
            return;
        }
        for (Map.Entry<String,Object> entry:ioc.entrySet()){

            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field :fields){
                if (!field.isAnnotationPresent(YAutowired.class)){continue;}

                YAutowired autowired = field.getAnnotation(YAutowired.class);

                String beanName = autowired.value().trim();
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
        if (classNames.isEmpty()){
            return;
        }

        for (String className :classNames){

            try {
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(YController.class)){
                    Object instance = clazz.newInstance();

                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(beanName,instance);
                }else if (clazz.isAnnotationPresent(YService.class)){
                    YService service = clazz.getAnnotation(YService.class);

                    String beanName = service.value();

                    if ("".equals(beanName.trim())){
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    Object instance = clazz.getInterfaces();
                    ioc.put(beanName,instance);

                    for (Class<?> i:clazz.getInterfaces()){
                        if (ioc.containsKey(i.getName())){

                            throw new Exception("The " + i.getName() + " is exists!!");
                        }

                        ioc.put(i.getName(),instance);
                    }
                }else{
                    continue;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }



    }

    private String toLowerFirstCase(String simpleName) {

        char[] chars = simpleName.toCharArray();
        
        chars[0]+=32;
        return String.valueOf(chars);

    }

    private void doScanner(String scanPackage) {

        URL url = this.getClass().getClass().getResource("/"+scanPackage.replaceAll("\\.","/"));

        File classPath = new File(url.getFile());

        for (File file:classPath.listFiles()){
            if (file.isDirectory()){
                doScanner(scanPackage+"."+file.getName());
            }else{
                if (!file.getName().endsWith(".class")){continue;}
                String className = (scanPackage+"."+file.getName().replace(".class",""));
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        //直接从类路径下找到Spring主配置文件所在的路径
        //并且将其读取出来放到Properties对象中
        //相对于scanPackage = com.gupaodu.demo 从文件中保存到了内存中

        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);


        try {
            contextConfig.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (null!=fis){

                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private Handler getHandler(HttpServletRequest request) {

        if (handlerMapping.isEmpty()){
            return null;
        }

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","");

        for (Handler handler:this.handlerMapping){

            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()){continue;}
            return handler;
        }
        return null;
    }


    public class Handler{

        private Pattern pattern;//正则
        private Method method;
        private Object controller;
        private Class<?>[] paramTypes;

        private Map<String,Integer> paramIndexMapping;

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
            //把方法上的注解拿到，得到的是一个二维数组
            //因为一个参数可以有多个注解，而一个方法又有多个参数

            Annotation [] [] pa = method.getParameterAnnotations();
            for (int i=0;i<pa.length;i++){
                for (Annotation a:pa[i]){
                    if (a instanceof YRequestParam){

                        String paramName = ((YRequestParam)a).value();

                        if (!"".equals(paramName.trim())){
                            paramIndexMapping.put(paramName,i);
                        }
                    }
                }
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            for(int i=0;i<paramTypes.length;i++){
                Class<?> type = paramTypes[i];
                if (type==HttpServletRequest.class||
                    type== HttpServletResponse.class){

                    paramIndexMapping.put(type.getName(),i);
                }
            }

        }

        public Pattern getPattern() {
            return pattern;
        }

        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
        }

        public Method getMethod() {
            return method;
        }

        public void setMethod(Method method) {
            this.method = method;
        }

        public Object getController() {
            return controller;
        }

        public void setController(Object controller) {
            this.controller = controller;
        }

        public Class<?>[] getParamTypes() {
            return paramTypes;
        }

        public void setParamTypes(Class<?>[] paramTypes) {
            this.paramTypes = paramTypes;
        }
    }
}
