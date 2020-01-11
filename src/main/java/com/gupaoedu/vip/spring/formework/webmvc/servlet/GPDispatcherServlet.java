package com.gupaoedu.vip.spring.formework.webmvc.servlet;

import com.gupaoedu.vip.spring.formework.annotation.GPController;
import com.gupaoedu.vip.spring.formework.annotation.GPRequestMapping;
import com.gupaoedu.vip.spring.formework.context.GPApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.gupaoedu.vip.spring.formework.webmvc.GPHandlerAdapter;
import com.gupaoedu.vip.spring.formework.webmvc.GPHandlerMapping;
import org.apache.log4j.Logger;

public class GPDispatcherServlet extends HttpServlet {
    private static Logger log = Logger.getLogger(GPDispatcherServlet.class);
    private final String LOCATION = "contextConfigLocation";

    // 策略模式的应用，用输入URL间接调用不同的Method
    private List<GPHandlerMapping> handlerMappings = new ArrayList<GPHandlerMapping>();
    // 适配器模型，将 Request 的字符串参数自动适配为 Method 的 Java 实参，主要实现参数列表自动适配和类型转换的功能
    // ViewResolver 也算是一种策略，根据不同的请求选择不同的模板引擎来进行页面渲染
    private Map<GPHandlerMapping, GPHandlerAdapter> handlerAdapters = new HashMap<GPHandlerMapping, GPHandlerAdapter>();
    private List<GPViewResolver> viewResolvers = new ArrayList<GDViewResolver>();
    private GPApplicationContext context;

    @Override
    public void init(ServletConfig config) throws ServletException {
        // 相当于把IoC容器初始化了
        context = new GPApplicationContext((config.getInitParameter(LOCATION)));
        initStrategies(context);
    }
    protected void initStrategies(GPApplicationContext context) {
        // 有九种策略
        // 针对每个用户请求，都会经过一些处理策略处理，最终才能有结果输出
        // 每种策略可以自定义干预，但是最终的结果都一致
        // ==================== 九大组件 ===========================
        initMultipartResolver(context); // 文件上传解析，如果请求类型是 multipart，将通过 MultipartResolver 进行文件上传解析
        initLocaleResolver(context); // 本地化解析
        initThemeResolver(context); // 主题解析
        // 我们自己实现
        initHandlerMappings(context); // 通过 HandlerMapping 将请求映射到处理器
        // 我们自己实现
        initHandlerAdapters(context); // 通过 HandlerAdapter 进行多类型的参数动态匹配

        initHandlerExceptionResolvers(context); // 如果执行过程中遇到异常，将交给 initHandlerExceptionResolvers 解析

        initRequestToViewNameTranslator(context); // 直接将请求解析到视图名

        // 通过 ViewResolvers 实现动态模板的解析
        // 自己解析一套模板语言
        // 我们自己实现
        initViewResolvers(context); // 通过 viewResolver 将逻辑视图解析到具体视图实现

        initFlashMapManager(context); // Flash 映射管理器
    }

    private void initHandlerMappings(GPApplicationContext context) {
        // 按照我们通常的理解应该是一个 Map
        // Map<String, Method> map;
        // map.put(url, Method)

        // 首先从容器中获取所有的实例
        String [] beanNames = context.getBeanDefinitionNames();
        try {
            for (String beanName : beanNames) {
                // 到了 MVC 层，对外提供的方法只有一个 getBean() 方法
                // 返回的对象不是 BeanWrapper， 怎么办？
                Object controller = context.getBean(beanName);
                // Object controller = GPAopUtils.getTargetObject(proxy);
                Class<?> clazz = controller.getClass();
                if (!clazz.isAnnotationPresent(GPController.class)) {
                    continue;
                }
                String baseUrl = "";
                if (clazz.isAnnotationPresent(GPRequestMapping.class)) {
                    GPRequestMapping requestMapping = clazz.getAnnotation(GPRequestMapping.class);
                    baseUrl = requestMapping.value();
                }
                // 扫描所有的 public 类型的方法
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (!method.isAnnotationPresent(GPRequestMapping.class)) {
                        continue;
                    }
                    GPRequestMapping requestMapping = method.getAnnotation(GPRequestMapping.class);
                    String regex = ("/" + baseUrl + requestMapping.value().replaceAll("\\*", ".*"))
                            .replaceAll("/+", "/");
                    Pattern pattern = Pattern.compile(regex);
                    this.handlerMappings.add(new GPHandlerMapping(pattern, controller, method));
                    log.info("Mapping: " + regex + " . " + method);
                }

            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void initHandlerAdapters(GPApplicationContext context) {
        for (GPHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new GPHandlerAdapter());
        }
    }

    private void initViewResolvers(GPApplicationContext context) {
        // 在页面中输入 http://localhost/first.html
        // 解决页面名字和模板文件关联的问题
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File templateRootDir = new File(templateRootPath);

        for (File template : templateRootDir.listFiles()) {
            this.viewResolvers.add(new GPViewResolver(templateRoot));
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            resp.getWriter().write("<font size='25' color='blue'>500 Exception</font><br/>Details:<bt/>" +
                    Arrays.toString(e.getStackTrace()).replaceAll("\\[|\\]", "")
                    .replaceAll("\\s", "\r\n") + "<font color='green'><i>Copyright@GupaoEDU</i></font>");
            e.printStackTrace();
        }
    }
    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 根据用户请求的 URL 来获得一个 Handler
        GPHandlerMapping handler = getHandler(req);
        if (null == handler ) {
            processDispatchResult(req, resp, new GPModelAndView("404"));
            return;
        }
        GPHandlerAdapter ha = getHandlerAdapter(handler);
        // 这一步只是调用方法，得到返回值
        GPModelAndView mv = ha.handle(req, resp, handler);

        // 这一步才是真的输出
        processDispatchResult(req, resp, mv);
    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response, GPModelAndView mv) throws Exception {
        // 调用 viewResolver 的 resolveViewName() 方法
        if (null == mv) { return; }
        if (this.viewResolvers.isEmpty()) { return; }
        if (null != this.viewResolvers) {
            for (GPViewResolver viewResolver : this.viewResolvers) {
                GPView view = viewResolver.resolveViewName(mv.getViewName(), null);
                if (null != view) {
                    view.render(mv.getModel(), request, response);
                    return;
                }
            }
        }
    }

    private GPHandlerAdapter getHandlerAdapter(GPHandlerMapping handler) {
        if (this.handlerAdapters.isEmpty()) { return null; }
        GPHandlerAdapter ha = this.handlerAdapters.get(handler);
        if (ha.supports(handler)) {
            return ha;
        }
        return null;
    }

    // 根据 url 获取对应的 handler
    private GPHandlerMapping getHandler(HttpServletRequest req) {
        if (this.handlerMappings.isEmpty()) { return null; }
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replace(contextPath, "").replaceAll("/+","/");

        for (GPHandlerMapping handler : this.handlerMappings) {
            Matcher matcher = handler.getPattern().matcher(url);
            if (!matcher.matches()) { continue; }
            return handler;
        }
        return null;
    }

    private void initMultipartResolver(GPApplicationContext context) { }

    private void initLocaleResolver(GPApplicationContext context) { }

    private void initThemeResolver(GPApplicationContext context) { }

    private void initHandlerExceptionResolvers(GPApplicationContext context) { }

    private void initRequestToViewNameTranslator(GPApplicationContext context) { }

    private void initFlashMapManager(GPApplicationContext context) { }
}
