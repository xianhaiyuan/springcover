package com.gupaoedu.vip.spring.formework.aop.aspect;

import com.gupaoedu.vip.spring.formework.aop.intercept.GPMethodInterceptor;
import com.gupaoedu.vip.spring.formework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPMethodBeforeAdvice extends GPAbstractAspectJAdvice implements GPAdvice, GPMethodInterceptor {
    private GPJoinPoint joinPoint;
    public GPMethodBeforeAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }
    public void before(Method method, Object[] args, Object target) throws Throwable {
        invokeAdviceMethod(this.joinPoint, null, null);
    }
    // 在 invoke() 中控制前置通知的调用顺序
    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        // 连接点,即原生方法
        this.joinPoint = mi;
        // 执行要增强的方法
        this.before(mi.getMethod(), mi.getArguments(), mi.getThis());
        // 主要作用是调用所有 interceptorsAndDynamicMethodMatchers 里的 Advice，最后再执行原生方法
        return mi.proceed();
    }
}
