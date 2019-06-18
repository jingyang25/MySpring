package com.gupao.context.support;


//抽象必须要实现的方法
public abstract class TAbstractApplicationContext  {

    //只提供给子类重写
    public void refresh() throws Exception{};

}
