package com.gupaoedu.vip.spring.demo.service.impl;

import com.gupaoedu.vip.spring.demo.service.IModifyService;
import com.gupaoedu.vip.spring.formework.annotation.GPService;
import lombok.extern.slf4j.Slf4j;

@GPService
@Slf4j
public class ModifyService implements IModifyService {
    public String add(String name, String addr) {
        return "modifyService add,name=" + name + " ,addr=" + addr;
    }

    public String edit(Integer id, String name) {
        return "modifyService edit,id=" + id + " ,name=" + name;
    }

    public String remove(Integer id) {
        return "modifyService id=" + id;
    }
}
