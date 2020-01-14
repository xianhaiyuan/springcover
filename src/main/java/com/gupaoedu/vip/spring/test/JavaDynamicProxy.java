package com.gupaoedu.vip.spring.test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class JavaDynamicProxy {
    public static void main(String[] args) {
        /*这个根据情况自行输入,主要功能是将生成出的代理对象Class文件存到本地.
        我用的idea编辑器,最终生成的文件在 工作空间下的 com\sun\proxy 文件夹中*/
        System.getProperties().put("sun.misc.ProxyGenerator.saveGeneratedFiles", "true");

        //具体类,需要增强的类
        final JavaDeveloper zack = new JavaDeveloper("Zack");
        /*Proxy.newProxyInstance 方法三个参数
         * 1 一个类加载器
         * 2 需要增强的类都实现了哪些接口,或哪些父类.这个很重要,因为最终生成的代理类,是要重写父类的方法的.
         * 3 一个调用处理器.是一个接口,需要实现接口中的方法,就一个invoke().这个方法很重要,你使用代理对象中的方法,
         * 每次都会经过这个方法.这也就是增强的核心.invoke()的三个参数
         *   1 需要增强原始对象.这里需要指定一个原型方法.
         *   2 即将执行的增强方法,这个Method 当你调用代理对象的A方法,这个Method就是A方法的Class对象.
         *   3 方法参数.A方法的参数. 熟悉反射的朋友应该能理解,一个Method方法执行,需要一个实例对象,和方法参数列表.
         * */

        Developer zackProxy = (Developer) Proxy.newProxyInstance(zack.getClass().getClassLoader(),
                zack.getClass().getInterfaces(), new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        /* 我这里分别对两个方法进行了不同的增强.需要注意的是 返回值
                         *  如果你的方法没有返回值可以 return null*/
                        if (method.getName().equals("code")) {
                            System.out.println("正在祈祷.....");
                            method.invoke(zack, args);
                            return "动态的写好了";
                        }
                        if (method.getName().equals("debug")) {
                            System.out.println("已经祈祷了,怎么还有bug");
                            method.invoke(zack, args);
                        }
                        return null;
                    }
                });
        //使用代理对象
        String s = zackProxy.code();
        zackProxy.debug();
        System.out.println(s);

      /*打印结果
        正在祈祷.....
        Zack写代码
        已经祈祷了,怎么还有bug
        Zack调试bug
        动态的写好了*/
    }
}
