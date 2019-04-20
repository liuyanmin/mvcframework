<h2>手写springMVC功能</2>
<h3>描述</h3>
我们在工作的过程中都用过springMVC，但是很少有人真正的思考spring到底是怎么实现的，如何将url请求映射到响应的方法上，如何扫描包下的注解并实例化的，如何解决依赖注入的，如何实现请求参数自动赋值的，本项目仅通过`300`行代码实现这些基本功能。

<h3>版本</h3>
v1: 实现了mvc的基本功能，不包括参数动态赋值
v2: 在实现mvc的基本功能基础上，实现了请求参数动态赋值功能，并进一步优化v1版本的代码

<h3>v2版本功能</h3>
  <ol>
    <li> 加载配置文件
    <li> 扫描指定包下的类
    <li> 实例化对象到IOC容器中
    <li> 依赖注入(DI)
    <li> 初始化HandlerMapping
    <li> url请求映射，参数动态赋值，反射方法调用
  </ol>

<h3>用到的设计模式</h3>
  <ol>
    <li> 工厂模式
    <li> 单利模式
    <li> 委派模式(所有的请求委派给dispatcher处理，即路由功能)
    <li> 策略模式(不同的url调用不同的方法，解决代码臃肿的问题，解耦)
  </ol>
注: 由于是简易版mvc，所以用到的设计模式比较少，实际springMVC用到的设计模式比这多很多。

<h3>实现步骤</h3>
<h4>配置阶段</h4>
  <ul>
    <li> 配置web.xml
    <li> 设置init-param
    <li> 设定url-pattern
    <li> 自定义Annotation
  </ul>
<h4>初始化阶段</h4>
  <ul>
    <li> 调用init方法，加载配置文件
    <li> 初始化IOC容器
    <li> 扫描相关的类
    <li> 创建实例，并保存IOC容器
    <li> 依赖注入(DI)
    <li> 初始化HandlerMapping
  </ul>
<h4>运行阶段</h4>
  <ul>
    <li> 调用doPost()/doGet()
    <li> 匹配HandlerMapping
    <li> 反射调用method.invoke()
    <li> 响应结果返回(response.getWrite().write())
  </ul>h3
<h3>项目依赖</h3>
&lt;dependency&gt;<br/>
  &nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId><b>javax.servlet</b>&lt;/groupId&gt;<br/>
  &nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId><b>servlet-api</b>&lt;/artifactId&gt;<br/>
  &nbsp;&nbsp;&nbsp;&nbsp;&lt;version><b>2.5</b>&lt;/version&gt;<br/>
&lt;/dependency&gt;<br/>
&lt;dependency&gt;<br/>
  &nbsp;&nbsp;&nbsp;&nbsp;&lt;groupId><b>org.javassist</b>&lt;/groupId&gt;<br/>
  &nbsp;&nbsp;&nbsp;&nbsp;&lt;artifactId><b>javassist</b>&lt;/artifactId&gt;<br/>
  &nbsp;&nbsp;&nbsp;&nbsp;&lt;version><b>3.24.1-GA</b>&lt;/version&gt;<br/>
&lt;/dependency&gt;<br/>
