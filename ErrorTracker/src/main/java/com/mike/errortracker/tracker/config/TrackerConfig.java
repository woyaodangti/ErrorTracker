package com.mike.errortracker.tracker.config;

import com.mike.errortracker.tracker.TrackerBeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TrackerConfig {
    @Bean
    @ConditionalOnMissingBean(TrackerBeanPostProcessor.class)
    public TrackerBeanPostProcessor registryTracker() {
        return new TrackerBeanPostProcessor();
    }

}
