package com.gupaoedu.vip.spring.formework.aop.support;

import com.gupaoedu.vip.spring.formework.aop.GPAopConfig;
import com.gupaoedu.vip.spring.formework.aop.aspect.GPAfterReturningAdvice;
import com.gupaoedu.vip.spring.formework.aop.aspect.GPAfterThrowingAdvice;
import com.gupaoedu.vip.spring.formework.aop.aspect.GPMethodBeforeAdvice;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GPAdvisedSupport {
    private Class targetClass;
    private Object target;
    private Pattern pointCutClassPattern;
    // transient 表示不能被序列化持久化保存
    private transient Map<Method, List<Object>> methodCache;
    private GPAopConfig config;

    public GPAdvisedSupport(GPAopConfig config) {
        this.config = config;
    }

    public Class getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(Class targetClass) {
        this.targetClass = targetClass;
        parse();
    }

    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public List<Object> getInterceptorsAndDynamicInterceptionAdvice(Method method, Class<?> targetClass) throws Exception {
        List<Object> cached = methodCache.get(method);

        // 缓存未命中，则进行下一步处理
        if (null == cached) {
            Method m = targetClass.getMethod(method.getName(), method.getParameterTypes());
            cached = methodCache.get(m);
            // 存入缓存
            this.methodCache.put(m, cached);
        }
        return cached;
    }

    public boolean pointCutMatch() {
        return pointCutClassPattern.matcher(this.targetClass.toString()).matches();
    }

    private void parse() {
        // pointCut 表达式
        String pointCut = config.getPointCut()
                .replaceAll("\\.", "\\\\.")
                .replaceAll("\\\\.\\*", ".*")
                .replaceAll("\\(", "\\\\(")
                .replaceAll("\\)", "\\\\)");
        String pointCutForClass = pointCut.substring(0, pointCut.lastIndexOf("\\(") - 4);
        pointCutClassPattern = Pattern.compile("class " + pointCutForClass.substring(pointCutForClass.lastIndexOf(" ") + 1));

        methodCache = new HashMap<Method, List<Object>>();
        Pattern pattern = Pattern.compile(pointCut);

        try {
            Class aspectClass = Class.forName(config.getAspectClass());
            Map<String, Method> aspectMethods = new HashMap<String, Method>();
            for (Method m : aspectClass.getMethods()) {
                aspectMethods.put(m.getName(), m);
            }
            // 在这里得到的方法都是原生方法
            for (Method m : targetClass.getMethods()) {
                String methodString = m.toString();
                if (methodString.contains("throws")) {
                    methodString = methodString.substring(0, methodString.lastIndexOf("throws")).trim();
                }
                Matcher matcher = pattern.matcher(methodString);
                if (matcher.matches()) {
                    // 能满足切面规则的类，添加到 AOP 配置中
                    List<Object> advices = new LinkedList<Object>();
                    // 前置通知
                    if (!(null == config.getAspectBefore() || "".equals(config.getAspectBefore().trim()))) {
                        // 实例化要增强的类，并获取要增强的方法，在 GPMethodBeforeAdvice 中可设计调用顺序
                        advices.add(new GPMethodBeforeAdvice(aspectMethods.get(config.getAspectBefore()),aspectClass.newInstance()));
                    }
                    // 后置通知
                    if (!(null == config.getAspectAfter() || "".equals(config.getAspectAfter().trim()))) {
                        // 实例化要增强的类，并获取要增强的方法，在 GPAfterReturningAdvice 中可设计调用顺序
                        advices.add(new GPAfterReturningAdvice(aspectMethods.get(config.getAspectAfter()),aspectClass.newInstance()));
                    }
                    // 异常通知
                    if (!(null == config.getAspectAfterThrow() || "".equals(config.getAspectAfterThrow().trim()))) {
                        GPAfterThrowingAdvice afterThrowingAdvice = new GPAfterThrowingAdvice(aspectMethods.get(config.getAspectAfterThrow()), aspectClass.newInstance());
                        afterThrowingAdvice.setThrowingName(config.getAspectAfterThrowingName());
                        advices.add(afterThrowingAdvice);
                    }
                    methodCache.put(m, advices);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
