package com.gupao.beans;

public class YBeanWrapper {

    private Object wrappedInstance;
    private Class<?> wrappedClass;

    public YBeanWrapper(){

    }

    public YBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    public Object getWrappedInstance() {
        return this.wrappedInstance;
    }

    //返回代理以后的Class
    //可能会是这个 $Proxy0
    public Class<?> getWrappedClass(){
        return this.wrappedInstance.getClass();
    }
}
