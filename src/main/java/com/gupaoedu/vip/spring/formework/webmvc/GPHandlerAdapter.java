package com.gupaoedu.vip.spring.formework.webmvc;

// 专人干专事，实现请求传递到服务端的参数列表与 Method 实参列表的对应关系，完成参数值的类型转换工作
public class GPHandlerAdapter {
    public boolean supports(Object handler) {
        return (handler instanceof GPHandlerMapping);
    }
    
}
