package com.mike.errortracker.tracker;

import com.mike.errortracker.tracker.annotation.ErrorTracker;
import com.mike.errortracker.tracker.annotation.ErrorTrackerProxy;
import com.mike.errortracker.tracker.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TrackerBeanPostProcessor implements BeanPostProcessor, EnvironmentAware {
    private ExecutorService executor = Executors.newFixedThreadPool(50);
    private Environment environment;

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return null;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (null != bean.getClass().getAnnotation(ErrorTrackerProxy.class)) {
            log.info("begin errors tracker:{}", beanName);
            return Proxy.newProxyInstance(bean.getClass().getClassLoader(), bean.getClass().getInterfaces(), new ErrorTrackerInvocationHandler(bean));
        } else {
            return bean;
        }
    }

    private void trackerError(Throwable e, Object[] args) {
        log.error("error parameters:{},error info:", GsonUtil.toJsonWtihNullField(args));
        String dingTalkEnable = environment.getProperty("tracker.dingTalk.enable");
        if (!StringUtils.isEmpty(dingTalkEnable) && dingTalkEnable.equals("true")) {
            log.debug("发送告警信息到钉钉");
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    sendDingTalkPost(environment.getProperty("tracker.dingTalk.webHook"), wrapMarkDown(getErrorInfoFromException(e)));
                }
            });
        }
    }

    private String wrapMarkDown(String text) {
        return "### 错误消息如下:\n" + text;
    }

    private String getErrorInfoFromException(Throwable e) {
        try {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            return "\r\n" + sw.toString() + "\r\n";
        } catch (Exception e2) {
            return "bad getErrorInfoFromException";
        }
    }

    private void sendDingTalkPost(String webHook, String msgContent) {
        OkHttpClient client = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)// 设置连接超时时间
                .readTimeout(20, TimeUnit.SECONDS)// 设置读取超时时间
                .build();
        MediaType contentType = MediaType.parse("application/json; charset=utf-8");
//        String format = "{msgtype\":\""+environment.getProperty("tracker.dingTalk.msgtype","markdown")+"\",\r\n" + "    \"markdown\":{\r\n"
//                + "        \"title\":\"%s\",\r\n" + "        \"text\":\"%s\"\r\n" + "    }\r\n" + "}";
        String msgType = environment.getProperty("tracker.dingTalk.msgtype", "markdown");
        String title = environment.getProperty("tracker.dingTalk.title", "error report");
        String format = "{msgtype:\"" + msgType + "\",markdown:{title:\"%s\",text:\"%s\"}}";
        final StringBuilder content = new StringBuilder(String.format(format, title, msgContent));
        RequestBody body = RequestBody.create(contentType, content.toString());
        Request request =
                new Request.Builder().url(webHook).post(body).addHeader("cache-control", "no-cache").build();
        try {
            Response r = client.newCall(request).execute();
            log.info(r.toString());
        } catch (IOException e1) {
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    private class ErrorTrackerInvocationHandler implements InvocationHandler {
        private final Object bean;

        public ErrorTrackerInvocationHandler(Object bean) {
            this.bean = bean;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            Method invokeMethod = bean.getClass().getMethod(method.getName(), method.getParameterTypes());
            if (invokeMethod.isAnnotationPresent(ErrorTracker.class)) {
                try {
                    return invokeMethod.invoke(bean, args);
                } catch (Throwable e) {
                    trackerError(e, args);
                    throw e;
                }
            } else {
                return invokeMethod.invoke(bean, args);
            }
        }

    }
}
