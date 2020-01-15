package com.gupaoedu.vip.spring.formework.aop;

import com.gupaoedu.vip.spring.formework.aop.intercept.GPMethodInvocation;
import com.gupaoedu.vip.spring.formework.aop.support.GPAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.List;

/**
 * 使用 JDK Proxy API 生成代理类
 */
public class GPJdkDynamicAopProxy implements GPAopProxy, InvocationHandler {
    private GPAdvisedSupport config;

    public GPJdkDynamicAopProxy(GPAdvisedSupport config) {
        this.config = config;
    }
    // 把原生对象传进来
    public Object getProxy() {
        return getProxy(this.config.getTargetClass().getClassLoader());
    }

    @Override
    public Object getProxy(ClassLoader classLoader) {
        /**
         * loader: 用哪个类加载器去加载代理对象
         * interfaces:动态代理类需要实现的接口
         * h:动态代理方法在执行时，会调用h里面的invoke方法去执行
         */
        return Proxy.newProxyInstance(classLoader, this.config.getTargetClass().getInterfaces(), this);
    }

    // invoke() 方法是执行代理的关键入口
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 将每一个 JoinPoint 也就是被代理的业务方法（Method）封装成一个拦截器，组合成一个拦截器链
        List<Object> interceptorsAndDynamicMethodMatchers = config.
                getInterceptorsAndDynamicInterceptionAdvice(method, this.config.getTargetClass());
        // 交给拦截器链 MethodInvocation 的 proceed() 方法执行
        GPMethodInvocation invocation = new GPMethodInvocation(proxy, this.config.getTarget(), method, args,
                this.config.getTargetClass(), interceptorsAndDynamicMethodMatchers);
        // proceed 里实际上是递归调用的过程，不是递归自己，而是调用别的 Advice 里的 invoke 方法，形成递归调用链
        return invocation.proceed();
    }
}
