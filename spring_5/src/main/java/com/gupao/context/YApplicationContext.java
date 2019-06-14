package com.gupao.context;

import com.gupao.beans.YBeanWrapper;
import com.gupao.core.YBeanFactory;
import com.gupao.beans.config.YBeanDefinition;
import com.gupao.beans.support.YBeanDefinitionReader;
import com.gupao.beans.support.YDefaultListableBeanFactory;

import java.util.List;

//IOC、DI、MVC、AOP

/**
 * 它的产生是为了解决什么样的问题，以及如何解决这些问题？
 * 让你脱离对依赖对象的维护，只需要随用随取，不需要关心依赖对象的任何过程
 *
 * 对象的构建（即把饭做好），对象的绑定（即把饭端到我面前）
 *
 * SPring IOC
 * spring来负责控制对象的生命周期和对象间的关系
 *
 * 用transient关键字标记的成员变量不参与序列化过程
 *
 */
public class YApplicationContext extends YDefaultListableBeanFactory implements YBeanFactory {

    private String [] configLocations;

    private YBeanDefinitionReader reader;

    public YApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void refresh() throws Exception {
        //定位配置文件
    reader = new YBeanDefinitionReader(this.configLocations);

        //加载配置文件，扫描相关的类 封装成BeanDefinition
        List<YBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();
        //注册 把配置信息放到容器里面
        doRegisterBeanDefinition(beanDefinitions);

        // 把不是延迟加载的类 提前初始化
        doAutowrited();

    }

    private void doAutowrited() {
    }

    private void doRegisterBeanDefinition(List<YBeanDefinition> beanDefinitions) throws Exception {
        for (YBeanDefinition beanDefinition:beanDefinitions){
            if (super.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                throw new Exception("The "+beanDefinition.getFactoryBeanName()+ "is exists!!");
            }

            super.beanDefinitionMap.put(beanDefinition.getFactoryBeanName(),beanDefinition);
        }
        //到这里为止，容器初始化完毕
    }

    // 依赖注入从这里开始 通过读取BeanDefinition 中的信息
    //然后 通过反射机制创建一个实例 并返回
    // Spring做法是 不会把最原始的对象放出去 会用一个BeanWrapper 来进行一次包装
    //装饰器模式
    //1、保留原来的OOP 关系
    // 2、需要对它进行扩展 增强


    @Override
    public Object getBean(String beanName) {

        //1、初始化
        instantiateBean(beanName,new YBeanDefinition());
        //注入
        populateBean(beanName,new YBeanDefinition(),new YBeanWrapper());
        return null;
    }

    private void instantiateBean(String beanName, YBeanDefinition yBeanDefinition) {

    }

    private void populateBean(String beanName, YBeanDefinition yBeanDefinition, YBeanWrapper yBeanWrapper) {

    }
}
