package com.fwwb.vehicledetection.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.FactoryBean;

@Component
public class FactoryBeanLogger implements BeanPostProcessor {
    private static final Logger logger = LoggerFactory.getLogger(FactoryBeanLogger.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof FactoryBean) {
            logger.debug("Processing FactoryBean: {} of type {}", beanName, bean.getClass().getName());
            try {
                FactoryBean<?> factoryBean = (FactoryBean<?>) bean;
                logger.debug("FactoryBean object type: {}", factoryBean.getObjectType());
            } catch (Exception e) {
                logger.error("Error inspecting FactoryBean {}: {}", beanName, e.getMessage(), e);
            }
        }
        return bean;
    }
}