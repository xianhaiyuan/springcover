package com.gupaoedu.vip.spring.formework.aop.aspect;

import com.gupaoedu.vip.spring.formework.aop.intercept.GPMethodInterceptor;
import com.gupaoedu.vip.spring.formework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPAfterReturningAdvice extends GPAbstractAspectJAdvice implements GPAdvice, GPMethodInterceptor {
    private GPJoinPoint joinPoint;
    public GPAfterReturningAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }
    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        // 调用原生方法，并获取返回值
        Object retVal = mi.proceed();
        this.joinPoint = mi;
        this.afterReturning(retVal, mi.getMethod(), mi.getArguments(), mi.getThis());
        return retVal;
    }

    public void afterReturning(Object returnValue, Method method, Object[] args, Object target) throws Throwable {
        invokeAdviceMethod(joinPoint, returnValue, null);
    }
}
