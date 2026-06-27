package com.sm.ratelimiter.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jmx.export.MBeanExporter;

/**
 * Configuration to prevent JedisPool from being exposed as an MBean.
 * This avoids JMX registration conflicts that occur during spring context initialization.
 */
@Configuration
public class JmxConfig {

    @Bean
    public MBeanExporter mBeanExporter() {
        MBeanExporter exporter = new MBeanExporter();
        // Exclude the jedisPool bean from MBean export
        exporter.setExcludedBeans("jedisPool", "getJedisPool");
        return exporter;
    }
}


