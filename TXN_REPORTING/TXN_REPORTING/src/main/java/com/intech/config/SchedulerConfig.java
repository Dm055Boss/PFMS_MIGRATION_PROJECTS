package com.intech.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Scheduler thread pool configuration.
 *
 * Gives predictable scheduling behavior in production and ensures exceptions are logged.
 */
@Configuration
public class SchedulerConfig {

    private static final Logger log = LoggerFactory.getLogger(SchedulerConfig.class);

    @Bean
    public TaskScheduler taskScheduler(SchedulerProperties schedulerProperties) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(Math.max(1, schedulerProperties.getPoolSize()));
        scheduler.setThreadNamePrefix("txn-scheduler-");
        scheduler.setErrorHandler(t -> log.error("Unhandled scheduler error", t));
        scheduler.initialize();
        return scheduler;
    }
}
