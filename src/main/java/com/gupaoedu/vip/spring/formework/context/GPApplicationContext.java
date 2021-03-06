package com.gupaoedu.vip.spring.formework.context;

import com.gupaoedu.vip.spring.formework.annotation.GPAutowired;
import com.gupaoedu.vip.spring.formework.annotation.GPController;
import com.gupaoedu.vip.spring.formework.annotation.GPService;
import com.gupaoedu.vip.spring.formework.aop.GPAopConfig;
import com.gupaoedu.vip.spring.formework.aop.GPAopProxy;
import com.gupaoedu.vip.spring.formework.aop.GPJdkDynamicAopProxy;
import com.gupaoedu.vip.spring.formework.aop.support.GPAdvisedSupport;
import com.gupaoedu.vip.spring.formework.beans.GPBeanWrapper;
import com.gupaoedu.vip.spring.formework.beans.config.GPBeanDefinition;
import com.gupaoedu.vip.spring.formework.beans.config.GPBeanPostProcessor;
import com.gupaoedu.vip.spring.formework.beans.support.GPBeanDefinitionReader;
import com.gupaoedu.vip.spring.formework.context.support.GPDefaultListableBeanFactory;
import com.gupaoedu.vip.spring.formework.core.GPBeanFactory;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class GPApplicationContext extends GPDefaultListableBeanFactory implements GPBeanFactory {
    private String [] configLocations;
    private GPBeanDefinitionReader  reader;

    // 单例的IoC容器缓存
    private Map<String, Object> factoryBeanObjectCache = new ConcurrentHashMap<String, Object>();
    // 通用的IoC容器
    private Map<String, GPBeanWrapper> factoryBeanInstanceCache = new ConcurrentHashMap<String, GPBeanWrapper>();

    public GPApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
        try {
            refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void refresh() throws Exception {
        // 1. 定位，定位配置文件
        reader = new GPBeanDefinitionReader(this.configLocations);

        // 2. 加载配置文件，扫描相关类，把它们封装成 BeanDefinition
        List<GPBeanDefinition> beanDefinitions = reader.loadBeanDefinitions();

        // 3. 注册，把配置信息放到容器里面（伪IoC容器）
        doRegisterBeanDefinition(beanDefinitions);

        // 4. 把不是延时加载的类提前初始化
        instantiation();

        // 依赖注入，会到 factoryBeanInstanceCache IoC 缓存中查找要注入的实例
        // 所以依赖注入基于IoC控制反转
        // 要保证 Service 要在 Action之前加载入IoC容器，否则会出现依赖注入失败的情况
        // 所以必须先把所有的类先实例化放入 factoryBeanInstanceCache 缓存中，在进行依赖注入

    }

    // 只处理非延时加载情况
    // 在项目第一次运行的时候 Action 类不能进行依赖注入，
    // 而是先实例化 除Action 以外的类（Service类），把它们加入 factoryBeanInstanceCache 后再进行依赖注入
    // 否则会注入失败
    private void instantiation() {
        for (Map.Entry<String, GPBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    if (!beanName.contains("Action")) {
                        getBean(beanName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        for (Map.Entry<String, GPBeanDefinition> beanDefinitionEntry : super.beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            if (!beanDefinitionEntry.getValue().isLazyInit()) {
                try {
                    if (beanName.contains("Action")) {
                        getBean(beanName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // 先到 IoC 缓存查看有没有该 beanDefinition，没有则生成并放入
    // 有则将它包装成 GPBeanWrapper，并放入 factoryBeanInstanceCache，最后返回
    public Object getBean(String beanName) throws Exception {
        GPBeanDefinition beanDefinition = super.beanDefinitionMap.get(beanName);
        try {
            // 生成通知事件
            GPBeanPostProcessor beanPostProcessor = new GPBeanPostProcessor();

            Object instance = instantiateBean(beanDefinition);
            if (null == instance) { return null; }
            // 在实例初始化以前调用一次
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);

            GPBeanWrapper beanWrapper = new GPBeanWrapper(instance);
            // 通用IoC容器
            this.factoryBeanInstanceCache.put(beanName, beanWrapper);

            // 实例初始化以后调用一次
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);

            populateBean(beanName, instance);


            return this.factoryBeanInstanceCache.get(beanName).getWrappedInstance();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void doRegisterBeanDefinition(List<GPBeanDefinition> beanDefinitions) throws Exception {
        for (GPBeanDefinition beandefinition : beanDefinitions) {
            if (super.beanDefinitionMap.containsKey(beandefinition.getFactoryBeanName())) {
                throw new Exception("The “" + beandefinition.getBeanClassName() + "” is exists!!");
            }
            super.beanDefinitionMap.put(beandefinition.getFactoryBeanName(), beandefinition);
        }

        // 到此为止， 容器初始化完毕
    }

    private void populateBean(String beanName, Object instance) {
        Class clazz = instance.getClass();

        if (!(clazz.isAnnotationPresent(GPController.class) || clazz.isAnnotationPresent(GPService.class))) {
            return;
        }
        // 获取所有声明字段
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (!field.isAnnotationPresent(GPAutowired.class)) { continue; }

            GPAutowired autowired = field.getAnnotation(GPAutowired.class);
            String autowiredBeanName = autowired.value().trim();

            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }

            field.setAccessible(true);
            try {
                field.set(instance, this.factoryBeanInstanceCache.get(autowiredBeanName).getWrappedInstance());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    // 查看 IoC 缓存里有没有该类的 beanDefinition，没有则实例化并放入
    private Object instantiateBean(GPBeanDefinition beanDefinition) {
        Object instance = null;
        String className = beanDefinition.getBeanClassName();
        try {
            // 因为根据Class才能确定一个类是否有实例
            if (this.factoryBeanInstanceCache.containsKey(className)) {
                instance = this.factoryBeanInstanceCache.get(className);
            } else {
                Class<?> clazz = Class.forName(className);
                instance = clazz.newInstance();

                // AOP 植入代理
                GPAdvisedSupport config = instantionAopConfig(beanDefinition);
                config.setTargetClass(clazz);
                config.setTarget(instance);
                if (config.pointCutMatch()) {
                    instance = createProxy(config).getProxy();
                }
                this.factoryBeanObjectCache.put(className, instance);
                this.factoryBeanObjectCache.put(beanDefinition.getFactoryBeanName(), instance);
            }

            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    // 读取 AOP 设置
    private GPAdvisedSupport instantionAopConfig(GPBeanDefinition beanDefinition) throws Exception {
        GPAopConfig config = new GPAopConfig();
        config.setPointCut(reader.getConfig().getProperty("pointCut"));
        config.setAspectClass(reader.getConfig().getProperty("aspectClass"));
        config.setAspectBefore(reader.getConfig().getProperty("aspectBefore"));
        config.setAspectAfter(reader.getConfig().getProperty("aspectAfter"));
        config.setAspectAfterThrow(reader.getConfig().getProperty("aspectAfterThrow"));
        config.setAspectAfterThrowingName(reader.getConfig().getProperty("aspectAfterThrowingName"));

        return new GPAdvisedSupport(config);
    }
    // AOP 创建代理
    private GPAopProxy createProxy(GPAdvisedSupport config) {
        Class targetClass = config.getTargetClass();
        if (targetClass.getInterfaces().length > 0) {
            return new GPJdkDynamicAopProxy(config);
        }
        return new GPJdkDynamicAopProxy(config);
    }

    public Object getBean(Class<?> beanClass) throws Exception {
        return getBean(beanClass.getName());
    }

    public String [] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }
    public int getBeanDefinitionCount() {
        return this.beanDefinitionMap.size();
    }
    public Properties getConfig() {
        return this.reader.getConfig();
    }
}
