package com.gupao.sprinmvc.v1;

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

public class YDispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();


    //2、IOC 初始化
    private Map<String,Object> ioc = new HashMap<>();

    private List<String> classNames = new ArrayList<>();

    private Map<String,Method> handlerMapping = new HashMap<>();

    /**
     * 运行阶段
     *
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

         doPost(req,resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        try {
            doDispatch(req,resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exection,Detail : " + Arrays.toString(e.getStackTrace()));
        }

    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {

        // 绝对路径 处理成相对路径
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/+","/");

        if (!this.handlerMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found");
            return;
        }

        Method method = this.handlerMapping.get(url);

        //第一个参数：方法所在的实例
        //第二个参数：调用时所需要的实参

        Map<String,String[]> params = req.getParameterMap();

        // 获取方法的形参列表
        Class<?>[] paramTypes = method.getParameterTypes();

        Object [] paramValues =new Object[paramTypes.length];

        for (int i=0;i<paramTypes.length;i++){

            Class paramType = paramTypes[i];

            if (paramType == HttpServletRequest.class){
                paramValues[i]=req;
                continue;
            }else if (paramType == HttpServletResponse.class){
                paramValues[i]=resp;
                continue;
            }else if(paramType ==String.class){
                YRequestParam requestParam = (YRequestParam) paramType.getAnnotation(YRequestParam.class);

                if (params.containsKey(requestParam)){

                    for (Map.Entry<String,String[]> param :params.entrySet()){

                        String value = Arrays.toString(param.getValue()).replaceAll("\\[\\]","").replaceAll("\\s",",");
                        paramValues[i] = value;
                    }
                }
            }

        }


        //投机取巧的方式
        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        method.invoke(ioc.get(beanName),paramValues);


    }

    private Object convert(Class<?> type,String value){
        if (Integer.class ==type){
            return Integer.valueOf(value);
        }

        return value;
    }
    /**-----**/
    //1、初始化
    public void init(ServletConfig config){

        //1、加载配置文件
        doLoadConfig(config.getInitParameter("contextConfigLocation"));


        System.out.println(contextConfig.getProperty("scanPackage"));

        //2、扫描相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        //3、初始化扫描到的类 并将它们放入IOC容器
        doInstance();

        //4、完成依赖注入
        doAutowired();

        //5、初始化HandlerMapping
        initHanlderMapping();

        System.out.println(" GP Spring framework is init");


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
   public void  doInstance()  {

        // 初始化 为DI 做准备
       if(classNames.isEmpty()) return;
       try {
           for (String className:classNames
                ) {

               Class<?> clazz = Class.forName(className);
               //什么样的类才需要初始化呢
               // 加了注解的类 才初始化 怎样判断
               if(clazz.isAnnotationPresent(YController.class)){

                   Object instance = clazz.newInstance();
                   String beanName = toLowerFirstCase(clazz.getSimpleName());
                   ioc.put(beanName,instance);
               }else if(clazz.isAnnotationPresent(YService.class)){
                   // 1、自定义的beanName
                   YService service = clazz.getAnnotation(YService.class);

                   String beanName = service.value();

                   // 2、默认类名首字母小写
                   if ("".equals(beanName.trim())){
                       beanName = toLowerFirstCase(clazz.getSimpleName());
                   }
                   Object instance = clazz.newInstance();
                   ioc.put(beanName,instance);

                   //3、根据类型自动赋值 如果注入的是接口？
                   for (Class<?> i:clazz.getInterfaces()
                        ) {

                       if(ioc.containsKey(i.getName())){
                           throw  new Exception("The "+i.getName()+"is exists!!");
                       }
                       ioc.put(beanName,instance);

                   }
               }else {
                   continue;
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();

        // 大小写字母相差32
        // 在 JAVA 中  对char做算术运算 实际就是对ASCII码做算术运算
        chars[0]+=32;

        return String.valueOf(chars);
    }

    //5、DI --反射 创建对象的权利交给 IOC 容器
    public void doAutowired(){
        if(ioc.isEmpty()) return;

        for (Map.Entry<String,Object> entry:ioc.entrySet()) {

            // Declared 所有的，特定的 字段，包括private/protected/default
            // 正常来说 普通的OOP只能拿到 public的属性
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field:fields) {
                if (!field.isAnnotationPresent(YAutowired.class)) continue;
                YAutowired autowired = field.getAnnotation(YAutowired.class);
                String beanName = autowired.value().trim();
                //类名首字母小写判断

                if ("".equals(beanName)){
                    //获得接口的类型 作为 Key 拿到IOC 中去取值
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


    //6、 初始化 HandleMapping
    // 根据 url 找到 handler  初始化 url 和Method 一对一的关系

  public void initHanlderMapping(){

        if(ioc.isEmpty()) return;
      for (Map.Entry<String,Object> entry:ioc.entrySet()) {

          Class<?> clazz = entry.getValue().getClass();

          if(!clazz.isAnnotationPresent(YController.class)) continue;

          // 保存写在 类上面的 @TRequestMapping("/demo")
          String baseUrl = "";
          if (clazz.isAnnotationPresent(YRequestMapping.class)){

              YRequestMapping requestMapping = clazz.getAnnotation(YRequestMapping.class);

              baseUrl = requestMapping.value().trim();

          }

          for (Method method:clazz.getDeclaredMethods()){
              if (!method.isAnnotationPresent(YRequestMapping.class)) continue;

              YRequestMapping requestMapping = method.getAnnotation(YRequestMapping.class);

              String url = ("/"+baseUrl+"/"+requestMapping.value()).replaceAll("/+","/");

              handlerMapping.put(url,method);

              System.out.println("Mapped:"+url+"-----"+method);

          }



      }
  }


}
