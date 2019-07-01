package com.gupao.frame.beans;

/**
 * @author yangTan
 * @date 2019/6/27
 */
public class YBeanWrapper {

    private Object wrappedInstance;

    private Class<?> wrappedClass;

    public YBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    public Class<?> getWrappedClass() {
        return this.wrappedInstance.getClass();
    }
}
