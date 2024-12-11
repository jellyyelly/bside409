package bsise.server.config;

import bsise.server.common.cache.CacheGroup;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@EnableCaching
@Configuration
public class CaffeineCacheConfig {

    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        for (CacheGroup cacheGroup : CacheGroup.values()) {
            cacheManager.registerCustomCache(cacheGroup.getCacheName(), toCaffeineCache(cacheGroup));
        }

        return cacheManager;
    }

    private Cache<Object, Object> toCaffeineCache(CacheGroup cacheGroup) {
        return Caffeine.newBuilder()
                .expireAfterWrite(cacheGroup.getExpiredAfterWrite())
                .maximumSize(cacheGroup.getMaximumSize())
                .recordStats()
                .build();
    }
}
