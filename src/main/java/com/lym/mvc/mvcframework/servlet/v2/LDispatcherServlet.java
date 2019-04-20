package com.lym.mvc.mvcframework.servlet.v2;

import com.lym.mvc.mvcframework.annotation.*;
import com.lym.mvc.mvcframework.util.ParamUtil;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @ClassName LDispatchServlet
 * @Description
 * @Author LYM
 * @Date 2019/4/19 19:14
 * @Version 1.0.0
 */
public class LDispatcherServlet extends HttpServlet {
    private static final String CONFIG = "contextConfigLocation";
    private Properties p = new Properties();
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> ioc = new HashMap<String, Object>();
    private List<Handler> handlerMapping = new ArrayList<Handler>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatcher(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception: " + e.getMessage());
        }
    }

    private void doDispatcher(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Handler handler = getHandler(req);

        if (handler == null) {
            resp.getWriter().write("404 Not Found!");
        }

        // 获取方法类型的参数列表
        Class<?>[] paramTypes = handler.method.getParameterTypes();

        // 保存所有需要自动赋值的参数值
        Object[] paramValues = new Object[paramTypes.length];

        Map<String, String[]> params = req.getParameterMap();
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "").replaceAll("\\s", "");

            if (!handler.paramIndexMapping.containsKey(param.getKey())) {
                continue;
            }
            int index = handler.paramIndexMapping.get(param.getKey());
            paramValues[index] = convert(paramTypes[index], value);
        }

        // 设置方法中的 request 和 response 对象
        int reqIndex = handler.paramIndexMapping.get(HttpServletRequest.class.getName());
        paramValues[reqIndex] = req;
        int respIndex = handler.paramIndexMapping.get(HttpServletResponse.class.getName());
        paramValues[respIndex] = resp;

        handler.method.invoke(handler.controller, paramValues);
    }

    private Object convert(Class<?> type, String value) {
        if (type == Integer.class) {
            return Integer.valueOf(value);
        }
        if (type == Byte.class) {
            return Byte.valueOf(value);
        }
        if (type == Short.class) {
            return Short.valueOf(value);
        }
        if (type == Long.class) {
            return Long.valueOf(value);
        }
        if (type == Float.class) {
            return Float.valueOf(value);
        }
        if (type == Double.class) {
            return Double.valueOf(value);
        }
        if (type == Boolean.class) {
            return Boolean.valueOf(value);
        }
        return value;
    }

    private Handler getHandler(HttpServletRequest request) {
        if (handlerMapping.isEmpty()) {
            return null;
        }

        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replaceAll(contextPath, "").replaceAll("/+", "/");

        for (Handler handler : handlerMapping) {
            Matcher matcher = handler.pattern.matcher(url);
            if (!matcher.matches()) {
                continue;
            }
            return handler;
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1、加载配置文件
        doLoadConfig(config.getInitParameter(CONFIG));

        // 2、扫描所有类
        doScanner(p.getProperty("scanPackage"));

        // 3、实例化对象(IOC)
        doInstance();

        // 4、依赖注入(DI)
        doAutowired();

        // 5、初始化HandlerMapping
        initHandlerMapping();
    }

    private void initHandlerMapping() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(LController.class)) {
                continue;
            }

            LController controller = clazz.getAnnotation(LController.class);
            String beanName = controller.value();

            if (clazz.isAnnotationPresent(LRequestMapping.class)) {
                LRequestMapping requestMapping = clazz.getAnnotation(LRequestMapping.class);
                beanName = requestMapping.value();
            }

            if ("".equals(beanName)) {
                beanName = toLowerFirstCase(clazz.getSimpleName());
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(LRequestMapping.class)) {
                    continue;
                }

                LRequestMapping requestMapping = method.getAnnotation(LRequestMapping.class);
                String regex = ("/" + beanName + "/" + requestMapping.value()).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMapping.add(new Handler(entry.getValue(), method, pattern));
                System.out.println("Mapping " + regex + ", " + method);
            }
        }
    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(LAutowired.class)) {
                    continue;
                }

                LAutowired autowired = field.getAnnotation(LAutowired.class);
                String beanName = autowired.value();
                if ("".equals(beanName)) {
                    beanName = field.getName();
                }

                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doInstance() {
        if (classNames.isEmpty()) {
            return;
        }
        try {
            for (String className : classNames) {
                Class<?> clazz = Class.forName(className);

                if (clazz.isAnnotationPresent(LController.class)) {
                    LController controller = clazz.getAnnotation(LController.class);
                    String beanName = controller.value();
                    if (clazz.isAnnotationPresent(LRequestMapping.class)) {
                        LRequestMapping requestMapping = clazz.getAnnotation(LRequestMapping.class);
                        beanName = requestMapping.value();
                    }

                    if ("".equals(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }
                    if (ioc.keySet().contains(beanName)) {
                        throw new Exception("The " + beanName + " is exists!");
                    }

                    beanName = beanName.replaceFirst("/+", "");
                    ioc.put(beanName, clazz.newInstance());
                    continue;
                }

                if (clazz.isAnnotationPresent(LService.class)) {
                    LService service = clazz.getAnnotation(LService.class);
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    if (ioc.keySet().contains(beanName)) {
                        throw new Exception("The " + beanName + " is exists!");
                    }

                    ioc.put(beanName, clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/"
                + scanPackage.replaceAll("\\.", "/").trim());
        File file = new File(url.getFile());
        for (File f : file.listFiles()) {
            if (f.isDirectory()) {
                doScanner(scanPackage + "." + f.getName().trim());
            } else {
                if (!f.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + f.getName().replaceAll(".class", "");
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        contextConfigLocation = contextConfigLocation.replaceAll("classpath:", "")
                .replaceAll("classpath*:", "");
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            p.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String toLowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private class Handler {
        /**
         * 保存对应的实例
         */
        protected Object controller;
        /**
         * 保存对应的方法
         */
        protected Method method;
        /**
         * URL匹配规则
         */
        protected Pattern pattern;
        /**
         * 形式参数列表顺序
         */
        protected Map<String, Integer> paramIndexMapping;

        protected Handler(Object controller, Method method, Pattern pattern) {
            this.controller = controller;
            this.method = method;
            this.pattern = pattern;
            this.paramIndexMapping = new HashMap<String, Integer>();
            putParamIndexMapping(method);
        }

        private void putParamIndexMapping(Method method) {
            // 提取方法中加了注解的参数
            Annotation[][] pa = method.getParameterAnnotations();
            for (int i = 0; i < pa.length; i++) {
                for (Annotation a : pa[i]) {
                    if (!(a instanceof LRequestParam)) {
                        continue;
                    }
                    String paramName = ((LRequestParam) a).value();
                    if ("".equals(paramName)) {
                        try {
                            paramName = ParamUtil.paramNames(method)[i];
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    if (!"".equals(paramName.trim())) {
                        paramIndexMapping.put(paramName, i);
                    }
                }
            }

            // 提取方法中request和response参数
            Class<?>[] paramTypes = method.getParameterTypes();
            for (int i = 0; i < paramTypes.length; i++) {
                Class<?> type = paramTypes[i];
                if (type == HttpServletRequest.class || type == HttpServletResponse.class) {
                    paramIndexMapping.put(type.getName(), i);
                }
            }
        }
    }
}
