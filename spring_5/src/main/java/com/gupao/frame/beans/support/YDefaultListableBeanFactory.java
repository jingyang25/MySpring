package com.gupao.frame.beans.support;

import com.gupao.frame.beans.config.YBeanDefinition;
import com.gupao.frame.context.YAbstractApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author yangTan
 * @date 2019/6/27
 */
public class YDefaultListableBeanFactory extends YAbstractApplicationContext {

    //存储注册信息的BeanDefinition
    protected final Map<String, YBeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>(256);



}
