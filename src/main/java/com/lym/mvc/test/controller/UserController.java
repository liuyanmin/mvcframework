package com.lym.mvc.test.controller;

import com.lym.mvc.mvcframework.annotation.LAutowired;
import com.lym.mvc.mvcframework.annotation.LController;
import com.lym.mvc.mvcframework.annotation.LRequestParam;
import com.lym.mvc.mvcframework.annotation.LRequestMapping;
import com.lym.mvc.test.service.IUserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @ClassName UserController
 * @Description
 * @Author LYM
 * @Date 2019/4/16 15:38
 * @Version 1.0.0
 */
@LController
@LRequestMapping("/user")
public class UserController {

    @LAutowired
    private IUserService userService;

    @LRequestMapping("/get/name")
    public void getName(HttpServletRequest request, HttpServletResponse response, @LRequestParam String name) {
        try {
            String result = userService.getUser(name);
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @LRequestMapping("/get/age")
    public void getAge(HttpServletRequest request, HttpServletResponse response, @LRequestParam("age") Integer myAge) {
        try {
            String result = userService.getAge(myAge);
            response.getWriter().write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
