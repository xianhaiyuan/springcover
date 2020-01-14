package com.gupaoedu.vip.spring.test;

public class JavaDeveloper implements Developer {
    private String name;

    public JavaDeveloper(String name) {
        this.name = name;
    }

    @Override
    public String code() {
        System.out.println(name + "写代码");
        return "写好了";
    }

    @Override
    public void debug() {
        System.out.println(name + "调试bug");
    }
}
