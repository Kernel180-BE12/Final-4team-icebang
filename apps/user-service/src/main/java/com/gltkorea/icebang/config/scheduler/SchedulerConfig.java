package com.gltkorea.icebang.config.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/** 동적 스케줄링을 위한 TaskScheduler Bean을 설정하는 클래스 */
@Configuration
public class SchedulerConfig {

  @Bean
  public TaskScheduler taskScheduler() {
    // ThreadPool 기반의 TaskScheduler를 생성합니다.
    ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

    // 스케줄러가 사용할 스레드 풀의 크기를 설정합니다.
    // 동시에 실행될 수 있는 스케줄 작업의 최대 개수입니다.
    scheduler.setPoolSize(10);

    // 스레드 이름의 접두사를 설정하여 로그 추적을 용이하게 합니다.
    scheduler.setThreadNamePrefix("dynamic-scheduler-");

    // 스케줄러를 초기화합니다.
    scheduler.initialize();
    return scheduler;
  }
}
