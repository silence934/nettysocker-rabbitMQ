package com.beisen.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author silence
 * @date 2020/06/23 16:16
 */
@Component
public class SpringContextUtil implements ApplicationContextAware {
    private static ApplicationContext cxt;

    private final Logger logger = LoggerFactory.getLogger(SpringContextUtil.class);

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        logger.debug("注入ApplicationContext到SpringContextUtil:" + applicationContext);
        if (SpringContextUtil.cxt != null) {
            logger.warn("SpringContextHolder中的ApplicationContext被覆盖, 原有ApplicationContext为:" + SpringContextUtil.cxt);
        }
        SpringContextUtil.cxt = applicationContext;
        logger.info("注入SpringContext成功！");
    }


    @SuppressWarnings("unchecked")
    public static <T> T getBean(String name) {
        if (name == null) {
            return null;
        }
        return (T) cxt.getBean(name);
    }

    public static <T> T getBeanByClass(Class<T> clazz) {
        if (clazz == null) {
            return null;
        }
        return cxt.getBean(clazz);
    }

    public static void autowired(Object bean) {
        AutowireCapableBeanFactory autowireCapableBeanFactory = SpringContextUtil.getBeanByClass(AutowireCapableBeanFactory.class);
        autowireCapableBeanFactory.autowireBean(bean);
    }

    public static List<Object> getAllBean() {
        String[] beanNames = cxt.getBeanDefinitionNames();
        return Stream.of(beanNames).map(beanName -> cxt.getBean(beanName)).collect(Collectors.toList());
    }
}
