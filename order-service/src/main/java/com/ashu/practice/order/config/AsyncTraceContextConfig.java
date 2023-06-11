package com.ashu.practice.order.config;

import io.micrometer.context.ContextExecutorService;
import io.micrometer.context.ContextSnapshot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration(proxyBeanMethods = false)
public class AsyncTraceContextConfig implements AsyncConfigurer {

    // NOTE: By design you can only have one AsyncConfigurer, thus only one executor pool is configurable.
    @Autowired
    // @Qualifier("taskExecutor") // if you have more than one task executor pools
    private ThreadPoolTaskExecutor taskExecutor;

    @Override
    public Executor getAsyncExecutor() {
        return ContextExecutorService.wrap(taskExecutor.getThreadPoolExecutor(), ContextSnapshot::captureAll);
    }
}