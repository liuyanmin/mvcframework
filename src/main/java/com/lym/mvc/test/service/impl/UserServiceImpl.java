package com.lym.mvc.test.service.impl;

import com.lym.mvc.mvcframework.annotation.LService;
import com.lym.mvc.test.service.IUserService;

/**
 * @ClassName UserServiceImpl
 * @Description
 * @Author LYM
 * @Date 2019/4/16 15:41
 * @Version 1.0.0
 */
@LService("userService")
public class UserServiceImpl implements IUserService {

    public String getUser(String name) {
        return "My name is " + name;
    }

    public String getAge(Integer age) {
        return "My age is " + age;
    }
}
