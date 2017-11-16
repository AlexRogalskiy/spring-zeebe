package io.zeebe.spring.client.config.processor;

import io.zeebe.client.ZeebeClient;
import io.zeebe.spring.client.bean.ClassInfo;
import io.zeebe.spring.client.config.SpringZeebeClient;
import io.zeebe.spring.client.config.processor.BeanInfoPostProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
public class SubscriptionBuilderPostProcessor implements BeanPostProcessor, Ordered {

    private final List<BeanInfoPostProcessor> processors;

    private final SpringZeebeClient client;

    public SubscriptionBuilderPostProcessor(final List<BeanInfoPostProcessor> processors, final SpringZeebeClient client) {
        this.processors = processors;
        this.client = client;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
        final ClassInfo beanInfo = ClassInfo.builder()
                .bean(bean)
                .beanName(beanName)
                .build();

        for (BeanInfoPostProcessor p : processors) {
            if (!p.test(beanInfo)) {
                continue;
            }

            Consumer<ZeebeClient> c = p.apply(beanInfo);
            client.onStart(c);
        }

        return bean;
    }


    @Override
    public int getOrder() {
        return LOWEST_PRECEDENCE;
    }
}
