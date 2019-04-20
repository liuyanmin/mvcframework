package com.lym.springmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @ClassName Demo
 * @Description
 * @Author LYM
 * @Date 2019/4/20 16:21
 * @Version 1.0.0
 */
public class Demo {

    public void add(HttpServletResponse response, HttpServletRequest request, int param1, int param2) {
        System.out.println(param1 + param2);
    }

}
