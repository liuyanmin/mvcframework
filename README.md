##手写springMVC功能
###描述
我们在工作的过程中都用过springMVC，但是很少有人真正的思考spring到底是怎么实现的，如何将url请求映射到响应的方法上，如何扫描包下的注解并实例化的，如何解决依赖注入的，如何实现请求参数自动赋值的，本项目仅通过`300`行代码实现这些基本功能。

###版本
v1: 实现了mvc的基本功能，不包括参数动态赋值
v2: 在实现mvc的基本功能基础上，实现了请求参数动态赋值功能，并进一步优化v1版本的代码

###v2版本功能
1、加载配置文件
2、扫描指定包下的类
3、实例化对象到IOC容器中
4、依赖注入(DI)
5、初始化HandlerMapping
6、url请求映射，参数动态赋值，反射方法调用

###用到的设计模式
1、工厂模式
2、单利模式
3、委派模式(所有的请求委派给dispatcher处理，即路由功能)
4、策略模式(不同的url调用不同的方法，解决代码臃肿的问题，解耦)
注: 由于是简易版mvc，所以用到的设计模式比较少，实际springMVC用到的设计模式比这多很多。

###实现步骤

<font color='gray'>
####配置阶段
* 配置web.xml
* 设置init-param
* 设定url-pattern
* 自定义Annotation
</font>
<font color='green'>
####初始化阶段
* 调用init方法，加载配置文件
* 初始化IOC容器
* 扫描相关的类
* 创建实例，并保存IOC容器
* 依赖注入(DI)
* 初始化HandlerMapping
</font>
<font color='blue'>
####运行阶段
* 调用doPost()/doGet()
* 匹配HandlerMapping
* 反射调用method.invoke()
* 响应结果返回(response.getWrite().write())
</font>


