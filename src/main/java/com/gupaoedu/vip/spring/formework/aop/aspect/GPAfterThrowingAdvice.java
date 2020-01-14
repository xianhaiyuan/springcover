package com.gupaoedu.vip.spring.formework.aop.aspect;

import com.gupaoedu.vip.spring.formework.aop.intercept.GPMethodInterceptor;
import com.gupaoedu.vip.spring.formework.aop.intercept.GPMethodInvocation;

import java.lang.reflect.Method;

public class GPAfterThrowingAdvice extends GPAbstractAspectJAdvice implements GPAdvice, GPMethodInterceptor {
    private String throwingName;
    private GPMethodInvocation mi;
    public GPAfterThrowingAdvice(Method aspectMethod, Object aspectTarget) {
        super(aspectMethod, aspectTarget);
    }
    public void setThrowingName(String name) {
        this.throwingName = name;
    }
    @Override
    public Object invoke(GPMethodInvocation mi) throws Throwable {
        try {
            return mi.proceed();
        } catch(Throwable ex) {
            invokeAdviceMethod(mi, null, ex.getCause());
            throw ex;
        }
    }
}
