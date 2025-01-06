package org.infraRpcExample.handler.scanner;

import lombok.Getter;
import org.infraRpcExample.service.impl.NatureChatServiceImpl;
import org.infraRpcExample.service.impl.ReportServiceImpl;
import org.infraRpcExample.service.impl.UserDataServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: Sekingme
 * @description: 启动时扫描所有bean，并保存到一个map中
 * @create: 2024-10-10 18:03
 **/
@Component
public class ServiceScanner {

    private final Logger logger = LoggerFactory.getLogger(ServiceScanner.class);

    @Getter
    private static final Map<String, Object> serviceMap = new HashMap<>();

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        initAdditionalValue();
        try {
            String[] beanNames = applicationContext.getBeanDefinitionNames();

            for (String beanName : beanNames) {
                Class<?> beanType = applicationContext.getType(beanName);
                if (beanType != null) {
                    if (AnnotationUtils.findAnnotation(beanType, Service.class) != null ||
                            AnnotationUtils.findAnnotation(beanType, Component.class) != null ||
                            AnnotationUtils.findAnnotation(beanType, Repository.class) != null ||
                            AnnotationUtils.findAnnotation(beanType, Controller.class) != null) {
                        Object beanInstance = applicationContext.getBean(beanName);
                        serviceMap.put(beanType.getName(), beanInstance);

                        Class<?>[] interfaces = beanType.getInterfaces();
                        for (Class<?> iface: interfaces) {
                            serviceMap.put(iface.getName(), beanInstance);
                        }

                    }
                }
            }
        } catch (Exception e) {
            logger.error("Netty ServiceScanner failed.", e);
        }
    }

    /**
     * 如果一些类不是bean，无法被ApplicationContext找到实例，可以在这里额外实例化
     */
    void initAdditionalValue() {
        serviceMap.put("org.infraRpcExample.service.UserDataService", new UserDataServiceImpl());
        serviceMap.put("org.infraRpcExample.service.ReportService", new ReportServiceImpl());
        serviceMap.put("org.infraRpcExample.service.NatureChatService", new NatureChatServiceImpl());
    }

}
