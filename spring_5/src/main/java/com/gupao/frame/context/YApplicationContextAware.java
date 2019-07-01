package com.gupao.frame.context;

import org.springframework.beans.BeansException;

/**通过解耦方式获得IOC容器的顶层设计
 * 后面将通过一个监听器去扫描所有的类 只要实现了此接口
 * 将自动调用 setApplicationContext() 方法
 * @author yangTan
 * @date 2019/6/27
 */
public interface YApplicationContextAware {

    void setApplicationContext(YApplicationContext applicationContext) throws BeansException;
}
