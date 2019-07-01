package com.gupao.frame.context;

import com.gupao.frame.annotation.YAutowired;
import com.gupao.frame.annotation.YController;
import com.gupao.frame.annotation.YService;
import com.gupao.frame.beans.YBeanWrapper;
import com.gupao.frame.beans.config.YBeanDefinition;
import com.gupao.frame.beans.config.YBeanPostProcessor;
import com.gupao.frame.beans.support.YDefaultListableBeanFactory;
import com.gupao.frame.context.support.YBeanDefinitionReader;
import com.gupao.frame.core.YBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**功能 实现了基本的bean实现
 * @author yangTan
 * @date 2019/6/27
 */
public class YApplicationContext extends YDefaultListableBeanFactory implements YBeanFactory {

    private String [] configLocations;

    //单例的IOC 同期
    private Map<String,Object> singletonObjects = new ConcurrentHashMap<>();

    //通用的IOC容器
    private Map<String,YBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<>();

    private boolean singleton = true;

    public YApplicationContext(String... configLocations) {
        this.configLocations = configLocations;

        System.out.println(configLocations);
        refresh();
    }

    @Override
    public void refresh(){
        //1、定位 定位配置文件
        YBeanDefinitionReader reader = new YBeanDefinitionReader(this.configLocations) ;

        //2、加载配置文件 扫描相关的类 把它们封装成 BeanDefinition
        List<YBeanDefinition> beanDefinitions = reader.loadDifinition();
        //3、注册 将配置信息放到伪IOC容器里面
        doBeanDefinitionRegistry(beanDefinitions);
        //4、把不是延迟加载的类 提前初始化
        doAutowrited();

    }

    private void doAutowrited() {
        for (Map.Entry<String,YBeanDefinition> beanDefinitionEntry:super.beanDefinitionMap.entrySet()) {

           String beanName = beanDefinitionEntry.getKey();
            if(!beanDefinitionEntry.getValue().isLazyInit()) {
                getBean(beanName);
            }
        }

    }

    private void doBeanDefinitionRegistry(List<YBeanDefinition> beanDefinitions) {

        for (YBeanDefinition beanDefinition:beanDefinitions) {
            this.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
            //这里要一些处理 ？
            
        }

    }


    /**功能描述
    *@author yangTan
    *@date 2019/6/27
    *@param  * @param beanName
    *@return java.lang.Object
    */
    @Override
    public Object getBean(String beanName) {

        YBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);

        Object instance = null;

        YBeanPostProcessor  postProcessor = new YBeanPostProcessor();

        postProcessor.postProcessBeforeInitialization(instance,beanName);


        //1、初始化
         instance = instantiateBean(beanName,beanDefinition);

        YBeanWrapper beanWrapper = new YBeanWrapper(instance);

        // 2、拿到BeanWrapper后 放入IOC 容器
     /*  if(factoryBeanInstanceCache.containsKey(beanName)){
           throw  new Exception("The"+beanName+"is exists!");
       }*/
       this.factoryBeanInstanceCache.put(beanName,beanWrapper);

       postProcessor.postProcessAfterInitialization(instance,beanName);

        //循环依赖

        //3、注入 为什么需要三个参数  保留类的一些原始信息 YBeanWrapper 类 是单例还是多个
        populateBean(beanName,new YBeanDefinition(),beanWrapper);

        return this.factoryBeanInstanceCache.put(beanName,beanWrapper).getWrappedInstance();
    }

    private void populateBean(String beanName, YBeanDefinition yBeanDefinition, YBeanWrapper beanWrapper) {

        Object instance = beanWrapper.getWrappedInstance().getClass();

        Class<?> clazz = beanWrapper.getWrappedClass();

        if(!clazz.isAnnotationPresent(YController.class)||clazz.isAnnotationPresent(YService.class)) {

            return;
        }

        Field[] fields  = clazz.getDeclaredFields();
        for (Field field:fields) {

            if(!field.isAnnotationPresent(YAutowired.class)){
                continue;
            }

            YAutowired autowired = field.getAnnotation(YAutowired.class);
            String autowiredBeanName = autowired.value().trim();
            if("".equals(autowiredBeanName)){
                autowiredBeanName = field.getType().getName();
            }


            try {
                field.set(instance,this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedClass());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }




    }


    private YBeanWrapper instantiateBean(String beanName, YBeanDefinition beanDefinition) {

        // 1、拿到类实例化对象的类名

        String className = beanDefinition.getBeanClassName();

        Object instance = null;




        //singletonObjects

        try {
            if(this.singletonObjects.containsKey(className)){
                instance = this.singletonObjects.get(className);

            }else{

                //2、反射实例化 得到一个对象
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();
                this.singletonObjects.put(className,instance);
                this.singletonObjects.put(beanDefinition.getFactoryBeanName(),instance);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }





        //3、把 这个对象封装到beanWrapper
        YBeanWrapper beanWrapper = new YBeanWrapper(instance);


       // factoryBeanInstanceCache

        return beanWrapper;



    }
}
