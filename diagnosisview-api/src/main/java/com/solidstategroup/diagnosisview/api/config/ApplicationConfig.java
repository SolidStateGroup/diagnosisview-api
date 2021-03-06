package com.solidstategroup.diagnosisview.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.transaction.TransactionAwareCacheManagerProxy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * General application configuration.
 */
@Configuration
@ComponentScan(basePackages = {"com.solidstategroup.diagnosisview.*"})
@EnableScheduling
@EnableCaching
@EnableJpaRepositories(basePackages = "com.solidstategroup.diagnosisview.repository")
@EntityScan(basePackages = {"com.solidstategroup.diagnosisview.model"})
public class ApplicationConfig implements WebMvcConfigurer {

    /**
     * Set up in memory caching including listing all caches.
     *
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("getAllCodes"),
                new ConcurrentMapCache("getAllCategories")
        ));
        // manually call initialize the caches as our SimpleCacheManager is not declared as a bean
        cacheManager.initializeCaches();

        // to support transactions with caching
        return new TransactionAwareCacheManagerProxy(cacheManager);
    }
}
