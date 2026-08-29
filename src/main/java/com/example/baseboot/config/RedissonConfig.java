package com.example.baseboot.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Redisson 分布式客户端配置类
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String host;

    @Value("${spring.data.redis.port:6379}")
    private int port;

    @Value("${spring.data.redis.password:}")
    private String password;

    @Value("${spring.data.redis.database:0}")
    private int database;

    @Value("${spring.data.redis.timeout:3000ms}")
    private String timeout;

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(RedissonClient.class)
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = "redis://" + host + ":" + port;
        SingleServerConfig serverConfig = config.useSingleServer()
                .setAddress(address)
                .setDatabase(database);

        if (StringUtils.hasText(password)) {
            serverConfig.setPassword(password);
        }

        // 解析超时时间
        int timeoutMs = 3000;
        try {
            if (timeout.endsWith("ms")) {
                timeoutMs = Integer.parseInt(timeout.replace("ms", ""));
            } else if (timeout.endsWith("s")) {
                timeoutMs = Integer.parseInt(timeout.replace("s", "")) * 1000;
            } else {
                timeoutMs = Integer.parseInt(timeout);
            }
        } catch (Exception ignored) {
        }
        serverConfig.setTimeout(timeoutMs);

        return Redisson.create(config);
    }
}
