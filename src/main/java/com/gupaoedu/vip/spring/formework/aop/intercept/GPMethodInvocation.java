package com.gupaoedu.vip.spring.formework.aop.intercept;

import com.gupaoedu.vip.spring.formework.aop.aspect.GPJoinPoint;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GPMethodInvocation implements GPJoinPoint {
    private Object proxy; // 代理对象
    private Method method; // 代理的目标方法
    private Object target; // 代理的目标对象
    private Class<?> targetClass; // 代理的目标类
    private Object [] arguments; // 代理的方法的实参列表
    private List<Object> interceptorsAndDynamicMethodMatchers; // 回调方法链

    // 保存自定义属性
    private Map<String, Object> userAttributes;
    private int currentInterceptorIndex = -1;

    public GPMethodInvocation(Object proxy, Object target, Method method, Object[] arguments, Class<?> targetClass, List<Object> interceptorsAndDynamicMethodMatchers) {
        this.proxy = proxy;
        this.method = method;
        this.target = target;
        this.targetClass = targetClass;
        this.arguments = arguments;
        this.interceptorsAndDynamicMethodMatchers = interceptorsAndDynamicMethodMatchers;
    }

    /**
     * 在 proceed()中，先进行判断，如果拦截器链为空，则说明目标方法无需增强，直接调用目标方法并返回
     * 如果拦截器链不为空，则将拦截器链中的方法按顺序执行，直到拦截器链中所有方法全部执行完毕
     */
    public Object proceed() throws Throwable {
        // 如果 Interceptor 执行完了，则执行 joinPoint
        if (this.currentInterceptorIndex == this.interceptorsAndDynamicMethodMatchers.size() - 1) {
            return this.method.invoke(this.target, this.arguments);
        }
        Object interceptorOrInterceptionAdvice = this.interceptorsAndDynamicMethodMatchers.get(++this.currentInterceptorIndex);

        // 如果要动态匹配 joinPoint
        if (interceptorOrInterceptionAdvice instanceof GPMethodInterceptor) {
            GPMethodInterceptor mi = (GPMethodInterceptor) interceptorOrInterceptionAdvice;
            return mi.invoke(this);
        } else {
            // 执行当前 Interceptor
            return proceed();
        }
    }

    @Override
    public Method getMethod() {
        return this.method;
    }

    @Override
    public Object[] getArguments() {
        return this.arguments;
    }

    @Override
    public Object getThis() {
        return this.target;
    }

    public void setUserAttribute(String key, Object value) {
        if (value != null) {
            if (this.userAttributes == null) {
                this.userAttributes = new HashMap<String, Object>();
            }
            this.userAttributes.put(key, value);
        } else {
            if (this.userAttributes != null) {
                this.userAttributes.remove(key);
            }
        }
    }

    public Object getUserAttribute(String key) {
        return (this.userAttributes != null ? this.userAttributes.get(key) : null);
    }
}
