package com.lym.springmvc;

import com.lym.mvc.test.controller.UserController;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @ClassName ParamTest
 * @Description
 * @Author LYM
 * @Date 2019/4/20 16:20
 * @Version 1.0.0
 */
public class ParamTest {

    public static void main(String[] args) throws NotFoundException {
        UserController demo = new UserController();
        Method[] methods = demo.getClass().getMethods();
        Class clazz = methods[0].getDeclaringClass();
        String methodName = methods[0].getName();
        ClassPool pool = ClassPool.getDefault();
        pool.insertClassPath(new ClassClassPath(clazz));
        CtClass cc = pool.get(clazz.getName());
        CtMethod cm = cc.getDeclaredMethod(methodName);
        MethodInfo methodInfo = cm.getMethodInfo();
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr =
                (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        if (attr == null) {
            System.out.println("params is null");
        }
        String[] paramNames = new String[cm.getParameterTypes().length + 2];
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length + 2; i++) {
            paramNames[i] = attr.variableName(i + pos);
        }
        for (int i = 0; i < paramNames.length + 2; i++) {
            System.out.println(paramNames[i]);
        }
    }

}
