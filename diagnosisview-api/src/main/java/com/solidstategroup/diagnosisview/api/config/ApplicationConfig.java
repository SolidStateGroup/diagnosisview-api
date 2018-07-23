package com.solidstategroup.diagnosisview.api.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.Arrays;

/**
 * General application configuration.
 */
@Configuration
@ComponentScan(basePackages = { "com.solidstategroup.diagnosisview.*" })
@EnableCaching
@EnableJpaRepositories(basePackages = "com.solidstategroup.diagnosisview.repository")
@EntityScan(basePackages = {"com.solidstategroup.diagnosisview.model"})
public class ApplicationConfig extends WebMvcConfigurerAdapter {

    /**
     * Set up in memory caching including listing all caches.
     * @return CacheManager
     */
    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache("projectFindAllDto"),
                new ConcurrentMapCache("projectSummary"),
                new ConcurrentMapCache("sectorFindAllDto")
        ));
        return cacheManager;
    }
}
