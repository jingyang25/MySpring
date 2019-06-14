package com.gupao.beans.config;

import lombok.Data;

/**
 *
 */
@Data
public class YBeanDefinition {

    private String beanClassName;

    private String factoryBeanName;

    private boolean lazyInit = false;

}
