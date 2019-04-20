package com.lym.mvc.mvcframework.servlet.v1;

import com.lym.mvc.mvcframework.annotation.LAutowired;
import com.lym.mvc.mvcframework.annotation.LController;
import com.lym.mvc.mvcframework.annotation.LRequestMapping;
import com.lym.mvc.mvcframework.annotation.LService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @ClassName DispatcherServlet
 * @Description 请求主入口
 * @Author LYM
 * @Date 2019/4/16 15:22
 * @Version 1.0.0
 */
public class LDispatcherServlet extends HttpServlet {
    private static final String LOCATION = "contextConfigLocation";

    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<String>();
    private Map<String, Object> ioc = new HashMap<String, Object>();
    private Map<String, Method> handlerMapping = new HashMap<String, Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 匹配逻辑，调度
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception: " + e.getMessage());
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        String url = req.getRequestURI();
        String contextPaht = req.getContextPath();
        url = url.replaceAll(contextPaht, "").replaceAll("/+", "/");

        if (!this.handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Map<String, String[]> params = req.getParameterMap();
        Method method = this.handlerMapping.get(url);
        // 获取方法的参数列表
        Class<?>[] parameterTypes = method.getParameterTypes();
        // 获取请求的参数
        Map<String, String[]> parameterMap = req.getParameterMap();
        // 保存参数值
        Object[] paramValues = new Object[parameterTypes.length];
        // 方法的参数列表
        for (int i = 0; i < parameterTypes.length; i++) {
            // 根据参数名称做某些处理
            Class parameterType = parameterTypes[i];
            if (parameterType == HttpServletRequest.class) {
                paramValues[i] = req;
                continue;
            }
            if (parameterType == HttpServletResponse.class) {
                paramValues[i] = resp;
                continue;
            }
            if (parameterType == String.class) {
                for (Map.Entry<String, String[]> param : parameterMap.entrySet()) {
                    String value = Arrays.toString(param.getValue()).replaceAll("\\[|\\]", "")
                            .replaceAll("\\s", "");
                    paramValues[i] = value;
                }
            }
        }

        try {
            String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
            // 利用反射机制来调用
            method.invoke(this.ioc.get(beanName), paramValues);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 1、加载配置文件
        doLoadConfig(config.getInitParameter(LOCATION));

        // 2、扫描并加载相关的类
        doScanner(contextConfig.getProperty("scanPackage"));

        // 3、实例化所有相关的类，并保存到IOC容器中
        doInstance();

        // 4、DI，以来注入
        doAutowired();

        // 5、初始化HandlerMapper
        initHandlerMapping();

        System.out.println("L Spring MVC init complete");
    }

    private void initHandlerMapping() {
        if (classNames.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if (!clazz.isAnnotationPresent(LController.class)) {
                continue;
            }

            LController controller = clazz.getAnnotation(LController.class);
            String baseUrl = controller.value();
            if ("".equals(baseUrl)) {
                baseUrl = toLowerFirstCase(clazz.getSimpleName());
            }

            if (clazz.isAnnotationPresent(LRequestMapping.class)) {
                LRequestMapping requestMapping = clazz.getAnnotation(LRequestMapping.class);
                baseUrl = requestMapping.value();
            }

            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(LRequestMapping.class)) {
                    continue;
                }
                LRequestMapping requestMapping = method.getAnnotation(LRequestMapping.class);
                String url = ("/" + baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                handlerMapping.put(url, method);
                System.out.println("Mapper " + url + ", " + method);
            }
        }
    }

    private void doAutowired() {
        if (classNames.isEmpty()) {
            return;
        }
        for (Map.Entry<String, Object> entry : ioc.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field :fields) {
                if (!field.isAnnotationPresent(LAutowired.class)) {
                    continue;
                }
                LAutowired autowired = field.getAnnotation(LAutowired.class);
                String beanName = autowired.value().trim();
                if ("".equals(beanName)) {
                    beanName = field.getType().getName();
                }

                // 设置私有属性的访问权限
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
                    Object instance = clazz.newInstance();

                    LController lController = clazz.getAnnotation(LController.class);
                    String beanName = lController.value();

                    if ("".equals(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    if (ioc.containsKey(beanName)) {
                        throw new Exception("The \"" + beanName + "\" is exists!");
                    }
                    ioc.put(beanName, instance);
                } else if (clazz.isAnnotationPresent(LService.class)) {
                    Object instance = clazz.newInstance();

                    LService service = clazz.getAnnotation(LService.class);
                    String beanName = service.value();

                    if ("".equals(beanName)) {
                        beanName = toLowerFirstCase(clazz.getSimpleName());
                    }

                    if (ioc.containsKey(beanName)) {
                        throw new Exception("The \"" + beanName + "\" is exists!");
                    }
                    ioc.put(beanName, instance);

                    // 保存接口对应的实现类实例，在依赖注入的时候使用
                    // key使用接口全类名
                    for (Class<?> aClass : clazz.getInterfaces()) {
                        if (ioc.containsKey(aClass.getName())) {
                            throw new Exception("The \"" + aClass.getName() + "\" is exists!");
                        }
                        ioc.put(aClass.getName(), instance);
                    }
                } else {
                    continue;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doScanner(String scanPackage) {
        // 把包名转换成文件路径，文件扫描递归才能全部扫描
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("[.]", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            if (file.isDirectory()) {
                doScanner(scanPackage + "." + file.getName());
            } else {
                if (!file.getName().endsWith(".class")) {
                    continue;
                }
                String className = scanPackage + "." + file.getName().replace(".class", "");
                classNames.add(className);
            }
        }
    }

    /**
     * 读取配置文件
     * @param contextConfigLocation
     */
    private void doLoadConfig(String contextConfigLocation) {
        InputStream in = null;
        try {
            contextConfigLocation = contextConfigLocation.replace("classpath:", "")
                    .replace("classpath*:", "");
            in = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            contextConfig.load(in);
        } catch (Exception e) {
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

    /**
     * 首字母转小写
     * @param beanName
     * @return
     */
    private String toLowerFirstCase(String beanName) {
        char[] chars = beanName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
