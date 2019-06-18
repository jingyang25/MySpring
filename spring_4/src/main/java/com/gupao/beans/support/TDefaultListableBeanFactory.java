package com.gupao.beans.support;

import com.gupao.beans.config.TBeanDefinition;
import com.gupao.context.support.TAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


//IOC 容器的默认实现
public class TDefaultListableBeanFactory extends TAbstractApplicationContext {
    //存储注册信息的BeanDefinition

    protected  final Map<String, TBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,TBeanDefinition>();




}
