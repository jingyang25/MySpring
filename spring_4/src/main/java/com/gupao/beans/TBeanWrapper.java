package com.gupao.beans;

public class TBeanWrapper {

    private Object wrappedInstance;

    private Class<?> wrappedClass;

    public TBeanWrapper(){

    }

    public TBeanWrapper(Object wrappedInstance) {
        this.wrappedInstance = wrappedInstance;
    }

    /**
     * Return the bean instance wrapped by this object.
     */
   public Object getWrappedInstance(){


       return this.wrappedInstance;
    }

    /**
     * Return the type of the wrapped bean instance.
     */
    public Class<?> getWrappedClass(){

        return this.wrappedClass.getClass();
    }
}
