package com.gupaoedu.vip.spring.formework.beans.config;

public class GPBeanDefinition {
    private String beanClassName; // 原生 Bean 的全类名
    private boolean lazyInit = false; // 标记是否延时加载
    private String factoryBeanName; // 保存 beanName，在 IoC 容器中存储的 key


    public boolean isLazyInit() {
        return lazyInit;
    }

    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }
    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
