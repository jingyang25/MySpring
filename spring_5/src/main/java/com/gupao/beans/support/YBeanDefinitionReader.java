package com.gupao.beans.support;

import com.gupao.beans.config.YBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class YBeanDefinitionReader {

    private List<String> registyBeanClasses = new ArrayList<>();

    private Properties config = new Properties();

    //固定配置文件中的key 相当于xml的规范
    private final String SCAN_PACKAGE = "scanPackage";

    public YBeanDefinitionReader(String...locations){
        //通过URL 定位找到对应的文件 然后转化为文件流

        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));

        try {
            config.load(is);
        }catch (Exception e){
            e.printStackTrace();
        }finally {

            if (null!=is){

                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    private void doScanner(String scanPackge) {
        //转换为文件路径 实际上就是将 . 替换为/就OK 了
        URL url = this.getClass().getClassLoader().getResource("/"+scanPackge.replace("\\.","/"));
        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()){
            if (file.isDirectory()){
                doScanner(scanPackge+"."+file.getName());
            }else{
                if (!file.getName().endsWith(".class")){continue;}
                String className = (scanPackge+"."+file.getName().replace(".class",""));
                registyBeanClasses.add(className);
            }
        }


    }

    public Properties getConfig() {
        return  this.config;
    }

    //把配置文件中扫描到的所有配置信息转换为YBeanDefinition 对象 便于IOC操作方便
    public List<YBeanDefinition> loadBeanDefinitions(){
        List<YBeanDefinition> result = new ArrayList<YBeanDefinition>();

        try {
            for (String className :registyBeanClasses){
                Class<?> beanClass = Class.forName(className);
                //如果是一个接口 不能实例化
                if(beanClass.isInterface()){continue;}
                //beanName 有三种情况
                //1、默认是类名首字母小写
                //2、自定义名字
                //3、接口注入
                result.add(doCreateBeanDefinition((String) toLowerFirstCase(beanClass.getSimpleName()),beanClass.getName()));

                Class<?> [] interfaces = beanClass.getInterfaces();
                for (Class<?>i:interfaces){

                    //如果是多个实现类 只能覆盖
                    // 因为Spring 没有那么智能  这个时候可以自定义名字
                    result.add(doCreateBeanDefinition(i.getName(),beanClass.getName()));
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return result;
    }

    private YBeanDefinition doCreateBeanDefinition(String factoryBeanName,String beanClassName) {

        YBeanDefinition beanDefinition = new YBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;

    }

    //如果类名本身是小写字母 确实会出现问题 但是这个方法自用 private
    //传值也是自己穿 类也遵循了驼峰命名法
    // 默认传入的值 存在首字母小写的情况 也不可能出现非字母的情况

    private Object toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);

    }
}
