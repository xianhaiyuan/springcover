package com.gupaoedu.vip.spring.formework.webmvc;

import com.gupaoedu.vip.spring.formework.annotation.GPRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// 专人干专事，实现请求传递到服务端的参数列表与 Method 实参列表的对应关系，完成参数值的类型转换工作
public class GPHandlerAdapter {
    public boolean supports(Object handler) {
        return (handler instanceof GPHandlerMapping);
    }
    public GPModelAndView handle(HttpServletRequest req, HttpServletResponse resp, Object handler) throws Exception {
        GPHandlerMapping handlerMapping = (GPHandlerMapping) handler;

        // 每个方法都有一个参数列表，这里保存的是形参列表
        Map<String, Integer> paramMapping = new HashMap<String, Integer>();

        // 这里只是给出命名参数
        Annotation[][] pa = handlerMapping.getMethod().getParameterAnnotations();
        for (int i = 0; i < pa.length; i++) {
            for (Annotation a : pa[i]) {
                if (a instanceof GPRequestParam) {
                    String paramName = ((GPRequestParam) a).value();
                    if (!"".equals(paramName.trim())) {
                        paramMapping.put(paramName, i);
                    }
                }
            }
        }

        // 根据用户请求的参数信息，跟 Method 中的参数信息进行动态匹配
        // resp 传进来的目的只有一个：将其赋值给方法的参数，仅此而已

        // 只有当用户传过来的 ModelAndView 为空的时候，才会新建一个默认的

        // 1. 要准备好这个方法的形参列表
        // 方法重载时形参的决定因素：参数的个数，参数的类型，参数的顺序，方法的名字
        // 只处理 Request 和 Response
        Class<?>[] paramTypes = handlerMapping.getMethod().getParameterTypes();
        for (int i = 0; i < paramTypes.length; i++) {
            Class<?> type = paramTypes[i];
            if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                paramMapping.put(type.getName(), i);
            }
        }

        // 2. 得到自定义命名参数所在的位置
        // 用户通过 URL 传过来的参数列表
        Map<String, String[]> reqParameterMap = req.getParameterMap();

        // 3. 构造实参列表
        Object [] paramValues = new Object[paramTypes.length];

        for (Map.Entry<String, String[]> param : reqParameterMap.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", "");
            if (!paramMapping.containsKey(param.getKey())) { continue; }
            int index = paramMapping.get(param.getKey());
            // 因为页面传过来的值都是 String 类型的，而在方法中定义的类型时千变万化的
            // 所以要针对我们传过来的参数进行类型转换
            paramValues[index] = caseStringValue(value, paramTypes[index]);
        }

        if (paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if (paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int resqIndex = paramMapping.get(HttpServletResponse.class.getName());
            paramValues[resqIndex] = resp;
        }

        // 4. 从 handler 中取出 Controller、Method，然后利用反射机制进行调用
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), paramValues);

        if (null == result) { return null; }

        boolean isModelAndView = handlerMapping.getMethod().getReturnType() == GPModelAndView.class;
        if (isModelAndView) {
            return (GPModelAndView) result;
        } else {
            return null;
        }
    }

    private Object caseStringValue(String value, Class<?> clazz) {
        if (clazz == String.class) {
            return value;
        } else if (clazz == Integer.class) {
            return Integer.valueOf(value);
        } else if (clazz == int.class) {
            return Integer.valueOf(value).intValue();
        } else {
            return null;
        }
    }
}
