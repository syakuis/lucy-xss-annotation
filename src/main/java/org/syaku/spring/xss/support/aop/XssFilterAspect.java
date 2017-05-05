package org.syaku.spring.xss.support.aop;

import com.nhncorp.lucy.security.xss.XssFilter;
import com.nhncorp.lucy.security.xss.XssSaxFilter;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.syaku.spring.xss.support.XssFilterConverter;
import org.syaku.spring.xss.support.reflection.ObjectRef;

import java.lang.reflect.Method;

/**
 * @author Seok Kyun. Choi. 최석균 (Syaku)
 * @site http://syaku.tistory.com
 * @since 2017. 4. 13.
 */
@Aspect
public class XssFilterAspect {
	private static final Logger logger = LoggerFactory.getLogger(XssFilterAspect.class);

	@Autowired
	private XssSaxFilter xssSaxFilter;

	@Autowired
	private XssFilter xssFilter;

	@Around("@target(org.springframework.stereotype.Controller) && (execution(public * *(.., @org.syaku.spring.xss.support.Defence (*))) || execution(public * *(@org.syaku.spring.xss.support.Defence (*), ..)) || execution(public * *(.., @org.syaku.spring.xss.support.Defence (*), ..)))")
	public Object xssFilter(ProceedingJoinPoint point) throws Throwable {
		if (logger.isDebugEnabled()) {
			logger.debug(">< >< invoke aspectj");
		}
		MethodSignature signature = (MethodSignature) point.getSignature();
		Method method = signature.getMethod();
		
		return point.proceed(new ObjectRef(new XssFilterConverter(xssFilter, xssSaxFilter)).getMethodParameter(method, point.getArgs()));
	}
}