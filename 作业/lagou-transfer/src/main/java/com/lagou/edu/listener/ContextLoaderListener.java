package com.lagou.edu.listener;

import com.alibaba.druid.util.StringUtils;
import com.lagou.edu.factory.BeanFactory;
import com.lagou.edu.factory.ProxyFactory;
import com.lagou.edu.myAnnotation.Autowired;
import com.lagou.edu.myAnnotation.Service;
import com.lagou.edu.myAnnotation.Transactional;
import org.reflections.Reflections;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ContextLoaderListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        //初始化ioc容器
        BeanFactory.initialized();
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }
}
