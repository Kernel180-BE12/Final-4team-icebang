package site.icebang.global.config.asnyc;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.ContextPropagatingTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean("traceExecutor")
  public ThreadPoolTaskExecutor traceExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(100);
    executor.setTaskDecorator(new ContextPropagatingTaskDecorator()); // 필수
    executor.setThreadNamePrefix("trace-");
    executor.initialize();
    return executor;
  }
}
