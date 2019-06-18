package com.gupao.beans.support;

import com.gupao.beans.config.TBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

//定位核心文件
public class TBeanDefinitionReader {

    private List<String> registyBeanClasses = new ArrayList<>();

    private Properties config = new Properties();



    private final String SCAN_PACKAGE = "scanPackage";

    public Properties getConfig() {

        return this.config;
    }



    public TBeanDefinitionReader(String... locations) {
        InputStream in =  this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));

        try {
            config.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            if (in!=null){
                try {
                    in.close();
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

    public List<TBeanDefinition> loadBeanDefinitions(){
        List<TBeanDefinition> result = new ArrayList<>();

        for(String className:registyBeanClasses){
         TBeanDefinition definition=  doCreateBeanDifinition(className);

           if(null==definition){continue;}
           result.add(definition);

        }


        return result;
    }


    private TBeanDefinition doCreateBeanDifinition(String className) {
        try {
            Class<?> beanClass = Class.forName(className);

            //有可能是接口 用他的实现类作为 beanClassName
            if (beanClass.isInterface()) {
                return null;
            }

            TBeanDefinition beanDefinition = new TBeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(beanClass.getSimpleName());
            return beanDefinition;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


    private Object toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0]+=32;
        return String.valueOf(chars);

    }
}
