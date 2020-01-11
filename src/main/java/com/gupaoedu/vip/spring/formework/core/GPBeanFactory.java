package com.gupaoedu.vip.spring.formework.core;

public interface GPBeanFactory {
    /**
     * 根据 beanName 从 IoC 容器中获取一个实例Bean
     * @param beanName
     * @return
     */
    Object getBean(String beanName) throws Exception;

    public Object getBean(Class<?> beanClass) throws Exception;
}
