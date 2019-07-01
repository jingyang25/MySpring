package com.gupao.frame.context;

/**
 * @author yangTan
 * @date 2019/6/27
 * IOC 容器的顶层实现
 */
public abstract class YAbstractApplicationContext {

    // 受保护的 只提供给子类重写
    protected void refresh(){}

}
