package com.gupao.beans.support;

import com.gupao.beans.config.YBeanDefinition;
import com.gupao.context.support.YAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YDefaultListableBeanFactory extends YAbstractApplicationContext {

    //存储注册信息的BeanDefinition

    protected  final Map<String, YBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<String,YBeanDefinition>();

}
