package com.gupaoedu.vip.spring.formework.core;

public interface GPBeanFactory {
    /**
     * 根据 beanName 从 IoC 容器中获取一个实例Bean
     * @param beanName
     * @return
     */
    // 在项目第一次运行的时候 getBean 不进行依赖注入，而是先实例化所有类加入 factoryBeanInstanceCache 后再进行依赖注入
    // 否则会注入失败
    // 而请求页面的时候 getBean 需要进行依赖注入，用 isInstantiation 判定
    Object getBean(String beanName, boolean isInstantiation) throws Exception;

    public Object getBean(Class<?> beanClass) throws Exception;
}
