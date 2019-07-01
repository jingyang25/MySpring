package com.gupao.frame.beans.config;

import org.springframework.beans.BeansException;

/**BeanPostProcessor 接口定义了一个你可以自己实现的回调方法，来实现你自己的实例化逻辑、依赖解决逻辑等
 * @author yangTan
 * @date 2019/7/1
 */
public class YBeanPostProcessor {

    //初始化之前
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }


    //初始化之后
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

}
