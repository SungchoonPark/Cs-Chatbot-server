package com.capstone.cschatbot.config.async;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean
    public ThreadPoolTaskExecutor threadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        // 생성해서 사용할 스레드 풀에 속한 기본 스레드 개수
        executor.setCorePoolSize(50);
        // 이벤트 대기 큐 크기
        executor.setQueueCapacity(50);
        // 최대 스레드 개수
        executor.setMaxPoolSize(100);
        executor.setThreadNamePrefix("EVAL-ASYNC-");
        executor.initialize();

        return executor;
    }
}
