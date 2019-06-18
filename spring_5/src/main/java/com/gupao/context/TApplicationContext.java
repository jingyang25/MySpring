package com.gupao.context;

import com.gupao.beans.TBeanWrapper;
import com.gupao.beans.config.TBeanDefinition;
import com.gupao.beans.support.TBeanDefinitionReader;
import com.gupao.beans.support.TDefaultListableBeanFactory;
import com.gupao.core.TBeanFactory;

import java.util.List;
import java.util.Map;

//上下文加载
public class TApplicationContext extends TDefaultListableBeanFactory implements TBeanFactory {

    private String [] configLocation;

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


    public Object getBean(String beanName) {
        //1、初始化

        instantiateBean(beanName,new TBeanDefinition());

        //2、注入
        populateBean(beanName,new TBeanDefinition(),new TBeanWrapper());







        return null;
    }

    private void populateBean(String beanName, TBeanDefinition tBeanDefinition, TBeanWrapper tBeanWrapper) {


    }

    private void instantiateBean(String beanName, TBeanDefinition tBeanDefinition) {


    }
}
