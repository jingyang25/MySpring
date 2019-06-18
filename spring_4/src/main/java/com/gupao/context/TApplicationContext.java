package com.gupao.context;

import com.gupao.annotation.TAutowired;
import com.gupao.annotation.TController;
import com.gupao.annotation.TService;
import com.gupao.beans.TBeanWrapper;
import com.gupao.beans.config.TBeanDefinition;
import com.gupao.beans.support.TBeanDefinitionReader;
import com.gupao.beans.support.TDefaultListableBeanFactory;
import com.gupao.core.TBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//上下文加载
public class TApplicationContext extends TDefaultListableBeanFactory implements TBeanFactory {

    private String [] configLocation;

    // 单例的IOC 容器
    private Map<String,Object> singletonObjects = new ConcurrentHashMap<>();

    //通用的IOC 容器
    private Map<String,TBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();



    public TApplicationContext(String... configLocation){
          this.configLocation = configLocation;
    }


    @Override
    public void refresh(){
        //1、定位 定位配置文件
        TBeanDefinitionReader reader=  new TBeanDefinitionReader();


        //2、加载 加载配置文件 扫描相关的类 把他们封装成BeanDefinition
       List<TBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();


        //3、注册 把配置信息放到容器里面（伪IOC容器）

        doRegisterBeanDefinition(beanDefinitions);
        //4、把不是延时加载的类 提前初始化
        doAutowrited();


    }

    //只处理非延时加载的情况
    private void doAutowrited() {

        for(Map.Entry<String,TBeanDefinition> beanDefinitionEntry :super.beanDefinitionMap.entrySet()){
            String beanName = beanDefinitionEntry.getKey();

            if(!beanDefinitionEntry.getValue().isLazyInit()){
                getBean(beanName);
            }

        }

    }

    //将bean 放入IOC容器
    private void doRegisterBeanDefinition(List<TBeanDefinition> beanDefinitions) {

        for (TBeanDefinition beanDefinition :beanDefinitions) {
            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }

    }
    public Object getBean(Class<?> beanClass) {
        return getBean(beanClass.getName());

    }

    public Object getBean(String beanName) {
        //1、初始化

        TBeanDefinition tBeanDefinition = this.beanDefinitionMap.get(beanName);
        TBeanWrapper  beanWrapper = instantiateBean(beanName,tBeanDefinition);

        if (this.factoryBeanInstanceCache.containsKey(beanName)){

        }
        //2、拿到beanWrapper 之后 把 BeanWrapper保存到IOC容器中
        this.factoryBeanInstanceCache.put(beanName,beanWrapper);


        //3、注入
        populateBean(beanName,new TBeanDefinition(),new TBeanWrapper());



        return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
    }

    private void populateBean(String beanName, TBeanDefinition tBeanDefinition, TBeanWrapper tBeanWrapper) {

        Object instance = tBeanWrapper.getWrappedInstance();


        // 判断只有加了注解的类 才会实现依赖注入

       Class<?> clazz = tBeanWrapper.getWrappedClass();

       if (clazz.isAnnotationPresent(TController.class)||clazz.isAnnotationPresent(TService.class)){
           return;
       }
        //获得所有的fileds
       Field[] fields = clazz.getDeclaredFields();

        for (Field field:fields
             ) {

            if(!field.isAnnotationPresent(TAutowired.class)){continue;}

           TAutowired autowired = field.getAnnotation(TAutowired.class);

           String autowiredBeanName = autowired.value().trim();

           if ("".equals(autowiredBeanName)){
               autowiredBeanName = field.getType().getName();
           }
           field.setAccessible(true);

            try {
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedClass());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }






    }

    private TBeanWrapper instantiateBean(String beanName, TBeanDefinition tBeanDefinition) {

        //1、拿到实例化对象的类名
        String className =  tBeanDefinition.getBeanClassName();

        //2、反射实例化 得到一个对象

        tBeanDefinition.getFactoryBeanName();
        Object  instance = null;

        if(this.singletonObjects.containsKey(className)){

            instance = this.singletonObjects.get(className);
        }else{

            try {
                Class<?> clazz = Class.forName(className);

                instance = clazz.newInstance();
                this.singletonObjects.put(className,instance);
                this.singletonObjects.put(tBeanDefinition.getBeanClassName(),instance);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }


        //3、把这个对象封装到beanWrapper

        TBeanWrapper beanWrapper = new TBeanWrapper();

        //singletonObjects

        //factoryBeanInstanceCache

        //4、把 BeanWrapper 存到 IOC 容器


        return beanWrapper;
    }
}
