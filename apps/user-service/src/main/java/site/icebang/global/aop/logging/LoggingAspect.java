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
}
