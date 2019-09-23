package com.solidstategroup.diagnosisview.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Custom configuration for asynchronous executor
 */
@Configuration
@EnableAsync
public class SpringAsyncConfig {

    @Bean(name = "asyncExecutor")
    public Executor getAsyncExecutor() {
        // keep here if we need more fine tune executor
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(40);
        taskExecutor.setMaxPoolSize(75);
        //taskExecutor.setQueueCapacity(1000); // throws org.springframework.core.task.TaskRejectedException
        taskExecutor.setThreadNamePrefix("async-exec-");
        taskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        taskExecutor.setAllowCoreThreadTimeOut(true);
        // Wait up to 10 seconds for the tasks to exit themselves,
        // after receiving an interrupt from the executor.
        //taskExecutor.setAwaitTerminationSeconds(10);
        taskExecutor.initialize();
        return taskExecutor;
        //return new SimpleAsyncTaskExecutor();
    }
}
