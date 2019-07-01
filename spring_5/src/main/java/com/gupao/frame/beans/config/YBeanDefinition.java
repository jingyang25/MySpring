package com.gupao.frame.beans.config;

import lombok.Data;

/**
 * @author yangTan
 * @date 2019/6/27
 */
@Data
public class YBeanDefinition {

    private String beanClassName;

    private String factoryBeanName;

    private boolean lazyInit = false;


}
