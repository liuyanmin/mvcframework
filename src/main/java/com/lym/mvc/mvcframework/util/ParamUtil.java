package com.lym.mvc.mvcframework.util;

import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @ClassName ParamUtil
 * @Description
 * @Author LYM
 * @Date 2019/4/20 16:37
 * @Version 1.0.0
 */
public class ParamUtil {

    /**
     * 获取方法中形式参数名称
     * @param method
     * @return
     * @throws NotFoundException
     */
    public static String[] paramNames(Method method) throws NotFoundException {
        Class clazz = method.getDeclaringClass();
        String methodName = method.getName();
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
        String[] paramNames = new String[cm.getParameterTypes().length];
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < paramNames.length; i++) {
            paramNames[i] = attr.variableName(i + pos + 2);
        }
        return paramNames;
    }

}
