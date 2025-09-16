package site.icebang.global.aop.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

  @Pointcut("execution(public * site.icebang..controller..*(..))")
  public void controllerMethods() {}

  @Pointcut("execution(public * site.icebang..service..*(..))")
  public void serviceMethods() {}

  @Pointcut("execution(public * site.icebang..service..mapper..*(..))")
  public void repositoryMethods() {}

  @Pointcut("execution(public * site.icebang.batch.tasklet..*(..))")
  public void taskletMethods() {}

  @Around("controllerMethods()")
  public Object logController(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    log.info("[CONTROLLER] Start: {} args={}", joinPoint.getSignature(), joinPoint.getArgs());
    Object result = joinPoint.proceed();
    long duration = System.currentTimeMillis() - start;
    log.info("[CONTROLLER] End: {} ({}ms)", joinPoint.getSignature(), duration);
    return result;
  }

  @Around("serviceMethods()")
  public Object logService(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    log.info("[SERVICE] Start: {} args={}", joinPoint.getSignature(), joinPoint.getArgs());
    Object result = joinPoint.proceed();
    long duration = System.currentTimeMillis() - start;
    log.info("[SERVICE] End: {} ({}ms)", joinPoint.getSignature(), duration);
    return result;
  }

  @Around("repositoryMethods()")
  public Object logRepository(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    log.debug("[REPOSITORY] Start: {} args={}", joinPoint.getSignature(), joinPoint.getArgs());
    Object result = joinPoint.proceed();
    long duration = System.currentTimeMillis() - start;
    log.debug("[REPOSITORY] End: {} ({}ms)", joinPoint.getSignature(), duration);
    return result;
  }

  @Around("taskletMethods()")
  public Object logTasklet(ProceedingJoinPoint joinPoint) throws Throwable {
    long start = System.currentTimeMillis();
    // Tasklet 이름만으로도 구분이 되므로, 클래스명 + 메서드명으로 로그를 남깁니다.
    log.info(">>>> [TASKLET] Start: {}", joinPoint.getSignature().toShortString());
    Object result = joinPoint.proceed(); // 실제 Tasklet의 execute() 메서드 실행
    long duration = System.currentTimeMillis() - start;
    log.info("<<<< [TASKLET] End: {} ({}ms)", joinPoint.getSignature().toShortString(), duration);
    return result;
  }
}
