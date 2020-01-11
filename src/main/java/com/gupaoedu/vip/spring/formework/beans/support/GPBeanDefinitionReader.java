package com.gupaoedu.vip.spring.formework.beans.support;

import com.gupaoedu.vip.spring.formework.beans.config.GPBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 主要完成对 application.properties 配置文件的解析工作，通过构造方法获取从 ApplicationContext 传过来的 locations 配置文件
 * 路径， 然后解析，扫描并保存所有相关的类，并提供统一的访问入口
 */

// 对配置文件进行查找、读取、解析
public class GPBeanDefinitionReader {
    private List<String> registyBeanClasses = new ArrayList<String>();
    private Properties config = new Properties();

    // 固定配置文件中的key, 相对于 XML 的规范
    private final String SCAN_PACKAGE = "scanPackage";

    public GPBeanDefinitionReader(String... locations) {
        // .getClassLoader() 使用类加载器从classPath下获取资源的，参数不以"/"开头
        InputStream is = this.getClass().getClassLoader().getResourceAsStream(locations[0].replace("classpath:",""));
        try {
            config.load(is);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (null != is) {
                try {
                    is.close();
                }catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        doScanner(config.getProperty(SCAN_PACKAGE));
    }

    // 把 scanPackage 下的所有类添加到registyBeanClasses 列表里
    private void doScanner(String scanPackage) {
        // 转换为文件路径，实际上就是把.替换成/
        URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));
        File classPath = new File(url.getFile());
        for (File file : classPath.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = (scanPackage+"."+file.getName().replace(".class",""));
                registyBeanClasses.add(className);
            }
        }
    }

    public Properties getConfig() {
        return this.config;
    }

    // 把配置文件中扫描到的所有配置信息转换为 GPBeanDefinition 对象，以便于之后的IoC操作
    public List<GPBeanDefinition> loadBeanDefinitions() {
        List<GPBeanDefinition> result = new ArrayList<GPBeanDefinition>();
        try {
            for (String className : registyBeanClasses) {
                // 获得初始化的类实例
                Class<?> beanClass = Class.forName(className);
                if (beanClass.isInterface()) { continue; }

                result.add(doCreateBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClass.getName()));

                Class<?> [] interfaces = beanClass.getInterfaces();
                for (Class<?> i : interfaces) {
                    result.add(doCreateBeanDefinition(i.getName(), beanClass.getName()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // 把每一个配置信息解析成一个 BeanDefinition
    private GPBeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        GPBeanDefinition beanDefinition = new GPBeanDefinition();
        beanDefinition.setBeanClassName(beanClassName);
        beanDefinition.setFactoryBeanName(factoryBeanName);
        return beanDefinition;
    }

    // 将类名首字母改为小写
    private String toLowerFirstCase(String simpleName) {
        char [] chars = simpleName.toCharArray();
        // 大小写字母ASCII码相差32， 并且大写字母ASCII码小于小写字母
        // 在Java中，对char做算术运算，实际上就是对ASCII码做算术运算
        chars[0] += 32;
        return String.valueOf(chars);
    }
}
