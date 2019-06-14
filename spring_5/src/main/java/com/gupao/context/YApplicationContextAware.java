package com.gupao.context;

/**
 * 通过解耦方式获得IOC 容器的顶层设计
 * 后面将通过一个监听器去扫描所有的类，只要实现了此接口
 *
 */
public interface YApplicationContextAware {

    void setApplicationContext(YApplicationContext applicationContext);

}
