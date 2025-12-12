package com.jam.global.aop;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);
	
	@Pointcut("within(com.jam..controller..*) || within(com.jam..service..*)")
	private void applicationPointcut() {}

	@Around("applicationPointcut()")
	public Object logExecution(ProceedingJoinPoint joinPoint) throws Throwable {

		long start = System.currentTimeMillis();
		String methodName = joinPoint.getSignature().toShortString();
		Object[] args = joinPoint.getArgs();

		log.info(">>> START: {} | args={}", methodName, Arrays.toString(args));

		try {
			Object result = joinPoint.proceed();

			long timeTaken = System.currentTimeMillis() - start;
			log.info("<<< END: {} | return={} | time={}ms", methodName, result, timeTaken);

			return result;
		} catch (Exception e) {

			long timeTaken = System.currentTimeMillis() - start;
			log.error("❌ ERROR in {} | time={}ms | message={}", methodName, timeTaken, e.getMessage());

			throw e;
		}
	}
}
