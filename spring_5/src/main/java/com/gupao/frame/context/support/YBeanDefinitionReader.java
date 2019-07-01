package com.gupao.frame.context.support;

import com.gupao.frame.beans.config.YBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author yangTan
 * @date 2019/6/27
 */
public class YBeanDefinitionReader {

    private Properties config = new Properties();

    private List<String> registryBeanClasses = new ArrayList<>();

    //固定配置文件中的Key 相对于xml的规范
    private final String SCAN_PACKAGE = "scanPackage";

    public YBeanDefinitionReader(String... locations) {

        InputStream fis = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));

        try {
            config.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    private void doScanner(String scanPackage) {

        //以根部件开始的路径是绝对路径，比如/(Linux系统中)或者C:\(Windows系统中)。
        //默认文件系统的路径分隔符，类Unix文件系统是/，Windows是\
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));

        System.out.println(this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/")));

        File classPath = new File(url.getFile());

        for (File file : classPath.listFiles()) {
            // 如果file表示的是一个目录
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) continue;
                String className = scanPackage + "." + file.getName().replace(".class", "");
                registryBeanClasses.add(className);
            }


        }
    }

    public Properties getConfig(){
        return this.config;
    }


    /**功能描述 把配置文件中的配置信息转化为 一个 beanDifinition 对象
    *@author yangTan
    *@date 2019/6/27
    *@param  * @param
    *@return java.util.List<com.gupao.frame.beans.config.YBeanDefinition>
    */
    public List<YBeanDefinition> loadDifinition(){

        List<YBeanDefinition> result = new ArrayList<>();


        try {
            for (String className :registryBeanClasses){

                Class<?> beanClass =  Class.forName(className);
                //如果是一个接口 是不能实例化的
                //用它实现类来实例化
                if(beanClass.isInterface()){
                    continue;
                }
               YBeanDefinition beanDefinition = doCreateBeanDifinition(beanClass.getSimpleName(),beanClass.getName());

                result.add(beanDefinition);
                Class<?> [] interfaces = beanClass.getInterfaces();

                for(Class<?> i:interfaces){
                    result.add(doCreateBeanDifinition(i.getName(),beanClass.getName()));
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return result;

    }


    public YBeanDefinition doCreateBeanDifinition(String factoryBeanName,String beanClassName){


            YBeanDefinition beanDefinition = new YBeanDefinition();
            beanDefinition.setFactoryBeanName(toLowerFirstCase(factoryBeanName));
            beanDefinition.setBeanClassName(beanClassName);
            return beanDefinition;

    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);
    }

}
