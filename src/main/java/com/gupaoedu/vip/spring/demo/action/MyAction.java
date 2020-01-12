package com.gupaoedu.vip.spring.demo.action;

import com.gupaoedu.vip.spring.demo.service.IModifyService;
import com.gupaoedu.vip.spring.demo.service.IQueryService;
import com.gupaoedu.vip.spring.formework.annotation.GPAutowired;
import com.gupaoedu.vip.spring.formework.annotation.GPController;
import com.gupaoedu.vip.spring.formework.annotation.GPRequestMapping;
import com.gupaoedu.vip.spring.formework.annotation.GPRequestParam;
import com.gupaoedu.vip.spring.formework.webmvc.GPModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 接口：
 * http://localhost:8080/springcover/first.html?teacher=Tom
 * http://localhost:8080/springcover/web/query.json?name=Tom
 * http://localhost:8080/springcover/web/addTom.json?name=tom&addr=Beijing
 * http://localhost:8080/springcover/web/remove.json?id=66&name=Tom
 */
@GPController
@GPRequestMapping("/web")
public class MyAction {
    @GPAutowired
    IQueryService queryService;
    @GPAutowired
    IModifyService modifyService;

    @GPRequestMapping("/query.json")
    public GPModelAndView query(HttpServletRequest request, HttpServletResponse response, @GPRequestParam("name") String name) {
        String result = queryService.query(name);
        return out(response, result);
    }
    @GPRequestMapping("/add*.json")
    public GPModelAndView add(HttpServletRequest request, HttpServletResponse response, @GPRequestParam("name") String name,
                              @GPRequestParam("addr") String addr) {
        String result = modifyService.add(name, addr);
        return out(response, result);
    }
    @GPRequestMapping("/remove.json")
    public GPModelAndView remove(HttpServletRequest request, HttpServletResponse response, @GPRequestParam("id") Integer id) {
        String result = modifyService.remove(id);
        return out(response, result);
    }
    @GPRequestMapping("/edit.json")
    public GPModelAndView edit(HttpServletRequest request, HttpServletResponse response, @GPRequestParam("name") String name,
                               @GPRequestParam("id") Integer id) {
        String result = modifyService.edit(id,name);
        return out(response, result);
    }
    private GPModelAndView out(HttpServletResponse resp, String str) {
        try {
            resp.getWriter().write(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
