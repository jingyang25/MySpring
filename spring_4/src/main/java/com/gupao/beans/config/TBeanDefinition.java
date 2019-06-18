package com.gupao.beans.config;

import lombok.Data;

@Data
public class TBeanDefinition {

    private String beanClassName;

    private String factoryBeanName;

    //是否延迟加载
    private boolean lazyInit = false;

    private boolean isSingleton = true;

}
